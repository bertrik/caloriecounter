package nl.sikken.bertrik.caloriecounter;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bertrik
 *
 */
public final class MqttPublisher {
	
    private static final Logger LOG = LoggerFactory.getLogger(MqttPublisher.class);
    private static final long DISCONNECT_TIMEOUT_MS = 3000;
	
	private final String clientId;
	private final String url;
	private final String topic;

	private MqttClient mqttClient;

	/**
	 * Constructor.
	 * 
	 * @param url the MQTT URL
	 * @param topic the MQTT topic
	 */
	public MqttPublisher(String url, String topic) {
        this.clientId = MqttClient.generateClientId();
        this.url = url;
        this.topic = topic;
	}
	
	/**
     * Starts this module.
     * 
     * @throws MqttException in case something went wrong with MQTT 
     */
    public void start() throws MqttException {
        LOG.info("Starting MQTT publisher");
        
        // connect
        LOG.info("Connecting to MQTT server {}", url);
        this.mqttClient = new MqttClient(url, clientId, new MemoryPersistence());
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        mqttClient.connect(options);
        
        LOG.info("Publishing to topic '{}'", topic);
    }
    
    /**
     * Stops this module.
     */
    public void stop() {
        LOG.info("Stopping MQTT publisher");
        try {
            mqttClient.disconnect(DISCONNECT_TIMEOUT_MS);
        } catch (MqttException e) {
            // don't care, just log
            LOG.warn("Caught exception on disconnect: {}", e.getMessage());
        } finally {
            try {
                mqttClient.close();
            } catch (MqttException e) {
                // don't care, just log
                LOG.warn("Caught exception on close: {}", e.getMessage());
            }
        }
    }

	public void publish(String text) {
		LOG.info("Publishing '{}' to topic '{}'", text, topic);

		try {
			mqttClient.publish(topic, text.getBytes(StandardCharsets.US_ASCII), 1, true);
		} catch (MqttException e) {
			LOG.trace("Caught", e);
			LOG.warn("Caught {}", e.getMessage());
		}
	}

}
