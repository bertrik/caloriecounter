package nl.sikken.bertrik.caloriecounter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.sikken.bertrik.caloriecounter.openfoodfacts.IOpenFoodFactsApi;
import nl.sikken.bertrik.caloriecounter.openfoodfacts.OpenFoodFacts;

/**
 * Calorie counter for revbank.
 * 
 * TODO:
 * - clear cache once in a while (e.g. based on file timestamp)
 * - reset sum once per day
 */
public final class CalorieCounter {

    private static final Logger LOG = LoggerFactory.getLogger(CalorieCounter.class);
    private static final String CONFIG_FILE = "caloriecounter.properties";

    private final MqttListener mqttListener;
    private final MqttPublisher mqttPublisher;
	private final OpenFoodFacts off;
	
	private double summedEnergy;

    /**
     * Main application entry point.
     * 
     * @param arguments application arguments (none taken)
     * @throws IOException in case of a problem reading a config file
     * @throws MqttException in case of a problem starting MQTT client
     */
    public static void main(String[] arguments) throws IOException, MqttException {
        final ICalorieCounterConfig config = readConfig(new File(CONFIG_FILE));
        final CalorieCounter app = new CalorieCounter(config);

        Thread.setDefaultUncaughtExceptionHandler(app::handleUncaughtException);

        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    /**
     * Constructor.
     * 
     * @param config the application configuration
     */
    CalorieCounter(ICalorieCounterConfig config) {
        this.mqttListener = new MqttListener(this::handleMessage, config.getMqttSourceUrl(), config.getMqttSourceTopic());
        this.mqttPublisher = new MqttPublisher(config.getMqttDestUrl(), config.getMqttDestTopic());
        
        IOpenFoodFactsApi api = 
        		OpenFoodFacts.newRestClient(config.getOpenFoodFactsUrl(), config.getOpenFoodFactsTimeout());
        File storageRoot = new File(config.getOpenFoodFactsStorage());
        this.off = new OpenFoodFacts(api, storageRoot);
        this.summedEnergy = 0.0;
    }

    /**
     * Starts the application.
     * 
     * @throws MqttException in case of a problem starting MQTT client
     */
    void start() throws MqttException {
        LOG.info("Starting application");

        // start sub-modules
        off.start();
	    mqttPublisher.start();
        mqttListener.start();

        LOG.info("Started application");
    }

    /**
	 * Stops the application.
	 * 
	 * @throws MqttException
	 */
	void stop() {
	    LOG.info("Stopping application");

	    mqttListener.stop();
	    mqttPublisher.stop();
	    off.stop();

	    LOG.info("Stopped application");
	}

	/**
     * Handles an incoming MQTT message
     * 
     * @param topic the topic on which the message was received
     * @param textMessage the message contents
     */
    void handleMessage(String topic, String textMessage) {
    	// verify bar code
    	if (!isPossiblyBarCode(textMessage)) {
    		LOG.info("Item '{}' is probably not a bar code, skipping", textMessage);
    		return;
    	}

    	// get the energy from openfoodfacts
    	String barCode = textMessage;
		try {
			double energy = off.processBarCode(barCode);

			// keep count
			summedEnergy += energy;
			
			// publish to MQTT
			String message = String.format(Locale.US, "%.0f kJ", summedEnergy);
			mqttPublisher.publish(message);
		} catch (IOException e) {
			LOG.warn("Caught IOException: {}", e.getMessage());
		}
    	
    }

    private boolean isPossiblyBarCode(String code) {
    	return (code.length() >= 8) && code.matches("\\d++");
    }

    /**
     * Handles uncaught exceptions: log it and stop the application.
     * 
     * @param t the thread
     * @param e the exception
     */
    private void handleUncaughtException(Thread t, Throwable e) {
        LOG.error("Caught unhandled exception, application will be stopped ...", e);
        stop();
    }
    
    private static ICalorieCounterConfig readConfig(File file) throws IOException {
        final CalorieCounterConfig config = new CalorieCounterConfig();
        try (FileInputStream fis = new FileInputStream(file)) {
            config.load(fis);
        } catch (IOException e) {
            LOG.warn("Failed to load config {}, writing defaults", file.getAbsoluteFile());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                config.save(fos);
            }
        }
        return config;
    }

}
