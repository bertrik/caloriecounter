package nl.sikken.bertrik.caloriecounter;

/**
 * Configuration interface for the application.
 */
public interface ICalorieCounterConfig {

	String getMqttSourceUrl();

	String getMqttSourceTopic();

	String getMqttDestUrl();

	String getMqttDestTopic();
	
	String getOpenFoodFactsUrl();

	int getOpenFoodFactsTimeout();

	String getOpenFoodFactsStorage();

}
