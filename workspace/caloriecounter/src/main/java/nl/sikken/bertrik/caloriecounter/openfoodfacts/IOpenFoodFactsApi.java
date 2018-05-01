package nl.sikken.bertrik.caloriecounter.openfoodfacts;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * REST API towards world.openfoodfacts.org
 * 
 * Example URL:
 * https://world.openfoodfacts.org/api/v0/product/737628064502.json
 */
public interface IOpenFoodFactsApi {

	@GET("/api/v0/product/{barcode}.json")
	Call<JsonNode> getProductInfo(@Path("barcode") String barCode);

}
