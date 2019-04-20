package nl.sikken.bertrik.caloriecounter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration class.
 */
/**
 * @author bertrik
 *
 */
public final class CalorieCounterConfig implements ICalorieCounterConfig {
    
    /**
     * One enumeration item per configuration item.
     */
    private enum EConfigItem {
    	MQTT_SOURCE_URL("mqtt.source.url", "tcp://revspace.nl", "MQTT source URL"),
    	MQTT_SOURCE_TOPIC("mqtt.source.topic", "revspace/bank/sale", "MQTT source topic"),
    	
    	MQTT_DEST_URL("mqtt.dest.url", "tcp://aliensdetected.com", "MQTT destination URL"),
    	MQTT_DEST_TOPIC("mqtt.dest.topic", "revspace/bar/energy", "MQTT destination topic"),
    	
    	OFF_URL("off.url", "https://world.openfoodfacts.org", "OpenFoodFacts API URL"),
    	OFF_TIMEOUT("off.timeout", "3000", "OpenFoodFacts API timeout"), 
    	OFF_STORAGE("off.storage", ".", "OpenFoodFacts storage root");
        
        private final String key;
        private final String def;
        private final String comment;

        EConfigItem(String key, String def, String comment) {
            this.key = key;
            this.def = def;
            this.comment = comment;
        }
    }
    
    private final Map<EConfigItem, String> props = new HashMap<>();
    
    /**
     * Constructor.
     * 
     * Configures all settings to their default value.
     */
    public CalorieCounterConfig() {
        for (EConfigItem e : EConfigItem.values()) {
            props.put(e, e.def);
        }
    }
    
    /**
     * Load settings from stream.
     * 
     * @param is input stream containing the settings
     * @throws IOException in case of a problem reading the file
     */
    public void load(InputStream is) throws IOException {
        final Properties properties = new Properties();
        properties.load(is);
        for (EConfigItem e : EConfigItem.values()) {
            String value = properties.getProperty(e.key);
            if (value != null) {
                props.put(e, value);
            }
        }
    }
    
    /**
     * Save settings to stream.
     * 
     * @param os the output stream
     * @throws IOException in case of a file problem
     */
    public void save(OutputStream os) throws IOException {
        try (Writer writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII)) {
            for (EConfigItem e : EConfigItem.values()) {
                // comment line
                writer.append("# " + e.comment + "\n");
                writer.append(e.key + "=" + e.def + "\n");
                writer.append("\n");
            }
        }
    }

    @Override
    public String getMqttSourceUrl() {
    	return props.get(EConfigItem.MQTT_SOURCE_URL);
    }
    
    @Override
    public String getMqttSourceTopic() {
    	return props.get(EConfigItem.MQTT_SOURCE_TOPIC);
    }
    
    @Override
    public String getMqttDestUrl() {
    	return props.get(EConfigItem.MQTT_DEST_URL);
    }
    
    @Override
    public String getMqttDestTopic() {
    	return props.get(EConfigItem.MQTT_DEST_TOPIC);
    }

	@Override
	public String getOpenFoodFactsUrl() {
		return props.get(EConfigItem.OFF_URL);
	}
	
	@Override
	public int getOpenFoodFactsTimeout() {
		return Integer.parseInt(props.get(EConfigItem.OFF_TIMEOUT));
	}

	@Override
	public String getOpenFoodFactsStorage() {
		return props.get(EConfigItem.OFF_STORAGE);
	}
    
}