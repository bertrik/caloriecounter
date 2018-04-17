package nl.sikken.bertrik.caloriecounter;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

/**
 * @author bertrik
 *
 */
public final class CalorieCounterTest {

	/**
	 * @throws MqttException 
	 * 
	 */
	@Test
	public void testCalorieCounter() throws MqttException {
		ICalorieCounterConfig config = new CalorieCounterConfig(); 
		CalorieCounter counter = new CalorieCounter(config);
		counter.start();
		try {
			counter.handleMessage("revspace/bank/sale", "4251097400792");
		} finally {
			counter.stop();
		}
	}
	
	
}
