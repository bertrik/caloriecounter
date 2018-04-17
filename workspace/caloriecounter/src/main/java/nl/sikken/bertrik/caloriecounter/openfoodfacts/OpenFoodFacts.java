package nl.sikken.bertrik.caloriecounter.openfoodfacts;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Retriever/cacher for OpenFoodFacts.org
 */
public final class OpenFoodFacts {

	private static final Logger LOG = LoggerFactory.getLogger(OpenFoodFacts.class);

	private final ObjectMapper mapper = new ObjectMapper();

	private final IOpenFoodFactsApi restClient;
	private final File cacheDir;
	private final File rejectDir;

	/**
	 * Constructor.
	 * 
	 * @param restClient the REST client
	 * @param storageRoot the file storage location (e.g. for cache)
	 */
	public OpenFoodFacts(IOpenFoodFactsApi restClient, File storageRoot) {
		this.restClient = restClient;
		this.cacheDir = new File(storageRoot, "/cache");
		this.rejectDir = new File(storageRoot, "/reject");
	}

	/**
	 * Creates a new REST client.
	 * 
	 * @param url
	 *            the URL of the server, e.g. "https://api.luftdaten.info"
	 * @param timeout
	 *            the timeout (ms)
	 * @return a new REST client.
	 */
	public static IOpenFoodFactsApi newRestClient(String url, int timeout) {
		final WebTarget target = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, timeout)
				.property(ClientProperties.READ_TIMEOUT, timeout).target(url);
		LOG.info("Creating new REST client for URL '{}' with timeout {}", url, timeout);
		return WebResourceFactory.newResource(IOpenFoodFactsApi.class, target);
	}

	/**
	 * Processes a bar code and returns the amount of nutritional energy.
	 * 
	 * @param barCode the bar code
	 * @return the energy (kJ)
	 * @throws IOException in case of a problem accessing the data
	 */
	public double processBarCode(String barCode) throws IOException {
		// get product info
		JsonNode json = getProductInfo(barCode);
		
		// calculate energy
		try {
			return getEnergy(json);
		} catch (IllegalArgumentException e) {
			LOG.warn("No energy determined for item '{}': {}", barCode, e.getMessage());
			// save in reject directory
			saveReject(json, barCode);
		}

		return 0.0;
	}
	
	private JsonNode getProductInfo(String barCode) throws IOException {
		// first look in the cache
		String name = barCode + ".json";
		File file = new File(cacheDir, name);
		JsonNode json;
		if (file.exists()) {
			// use the file
			LOG.info("Using cached JSON in {}", file.getName());
			json = mapper.readTree(file);
		} else {
			// retrieve it
			LOG.info("Retrieving JSON for {}", barCode);
			try {
				json = restClient.getProductInfo(barCode);
			} catch (WebApplicationException e) {
				throw new IOException(e);
			}
			// cache it
			LOG.info("Caching JSON in {}", file.getName());
			mapper.writeValue(file, json);
		}
		return json;
	}
	
	private void saveReject(JsonNode node, String barCode) throws IOException {
		File file = new File(rejectDir, barCode + ".json");
		LOG.warn("Saving unusable JSON in {}", file.getName());
		mapper.writeValue(file, node);
	}
	
	/**
	 * Parses the energy per serving from the JSON.
	 * 
	 * @param json the root JSON node
	 * @return the energy per serving (kJ)
	 * @throws IllegalArgumentException
	 */
	private Double getEnergy(JsonNode json) throws IllegalArgumentException {
		if ((json != null) && json.at("/status").intValue() != 0) {
			String energyServing = json.at("/product/nutriments/energy_serving").textValue();
			LOG.info("Energy: serving = '{}' kJ", energyServing);
			return Double.parseDouble(energyServing);
		}
		// not found
		throw new IllegalArgumentException("Could not determine energy");
	}
	
	public void start() {
		LOG.info("Starting OpenFoodFacts retriever");
		cacheDir.mkdirs();
		rejectDir.mkdirs();
	}

	public void stop() {
		LOG.info("Stopping OpenFoodFacts retriever");
	}

}
