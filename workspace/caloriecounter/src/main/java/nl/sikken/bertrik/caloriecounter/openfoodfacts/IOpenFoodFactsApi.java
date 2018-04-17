package nl.sikken.bertrik.caloriecounter.openfoodfacts;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST API towards world.openfoodfacts.org
 * 
 * Example URL:
 * https://world.openfoodfacts.org/api/v0/product/737628064502.json
 */
@Path("/api/v0")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IOpenFoodFactsApi {

	@Path("/product/{barcode}.json")
	@GET
	JsonNode getProductInfo(@PathParam("barcode") String barCode);

}
