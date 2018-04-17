package nl.sikken.bertrik.caloriecounter.openfoodfacts;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for OpenFoodFacts.java
 */
public final class OpenFoodFactsTest {

	private static final Logger LOG = LoggerFactory.getLogger(OpenFoodFactsTest.class);
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	@Ignore
	public void testRetrieve() throws IOException {
		String url = "https://world.openfoodfacts.org";
		IOpenFoodFactsApi api = OpenFoodFacts.newRestClient(url, 3000);
		OpenFoodFacts off = new OpenFoodFacts(api, tempFolder.getRoot());
		off.start();
		try {
			Double d1 = off.processBarCode("4029764001401");
			LOG.info("result = {}", d1);
		} finally {
			off.stop();
		}
	}
	
	@Test
	public void testParse() throws IOException {
		// load value
		InputStream is = getClass().getClassLoader().getResourceAsStream("737628064502.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(is);
		
		JsonNode node = root.at("/product/nutriments");
		Assert.assertNotNull(node);
	}
	
	@Test
	public void testDouble() {
		Double d = Double.parseDouble("529");
		Assert.assertEquals(529,  d, 0.1);
	}
	
}
