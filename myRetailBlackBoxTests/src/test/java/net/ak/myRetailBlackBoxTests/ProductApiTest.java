package net.ak.myRetailBlackBoxTests;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;


import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Blackbox test for myRetail Product API.
 */
public class ProductApiTest

{
    private String testTargetURL;
    private Client client;
    private WebTarget webTarget;
    private Cluster cluster;
    private Session session;


    @Before
    public void setUp() {
        testTargetURL = TestConfig.getInstance().getTestUrl()+"/product";
        Properties p = TestConfig.getInstance().getProperties();
        String connectHost = p.getProperty("PricingDAO.connectHost");
        String keyspaceName = p.getProperty("PricingDAO.keyspaceName");
        int connectPort = Integer.parseInt(p.getProperty("PricingDAO.connectPort"));
        //logTestExecutionDetail(String.format("\tConnection parameters to database (%s,%s,%s)",connectHost,connectPort,keyspaceName));
        cluster = Cluster.builder().addContactPoint(connectHost).
                withPort(connectPort).withRetryPolicy(DefaultRetryPolicy.INSTANCE).build();
        session = cluster.connect(keyspaceName);

        //logTestExecutionDetail("\tTest execution using Service at URL : "+testTargetURL);
        client = ClientBuilder.newClient();
        webTarget = client.target(testTargetURL);
    }

    @After
    public void tearDown() {
        cluster.closeAsync();
        client.close();
    }

    @Test
    public void testGetProductWithPrice() throws Exception {

        logTestExecutionDetail("\nTest             | testGetProductWithPrice");
        logTestExecutionDetail("Test Description | This runs a positive case scenario where the catalog and price information is available for a product");
        String productId="15117729";
        logTestExecutionDetail("Test Data        | productId="+productId);

        webTarget = webTarget.path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SUCCESSFUL)));
        logTestExecutionDetail("Response validated "+response.getStatus());
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));
        logTestExecutionDetail("MediaType validated "+response.getMediaType());


        JSONObject jsonObject = readJSON(response);

        JSONObject currentPrice = (JSONObject)jsonObject.get("current_price");
        org.junit.Assert.assertTrue(String.format("Unexpected id found %s",jsonObject.get("id")),jsonObject.get("id").equals(productId));
        org.junit.Assert.assertTrue(String.format("Unexpected name found %s",jsonObject.get("name")),jsonObject.get("name").equals("Apple iPad Air 2 16GB Wi-Fi - Gold"));
        org.junit.Assert.assertTrue(String.format("Unexpected price found %s",currentPrice.get("value")),currentPrice.get("value").equals(new Double("11.01")));
        org.junit.Assert.assertTrue(String.format("Unexpected currency code found %s",currentPrice.get("currency_code")),currentPrice.get("currency_code").equals("USD"));
        logTestExecutionDetail("Price validations are successful");
        logTestExecutionDetail("\n");
    }

    @Test
    public void testGetProductNonExistantProduct() throws Exception {
        logTestExecutionDetail("\nTest             | testGetProductNonExistantProduct");
        logTestExecutionDetail("Test Description | This runs a scenario where we try to get the product information when the catalog information for a product is missing");
        String productId="123";
        logTestExecutionDetail("Test Data        | productId="+productId);

        webTarget = webTarget.path(productId);
        Response response = getProduct(productId, webTarget);
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        logTestExecutionDetail("Response validated "+response.getStatus());
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));
        logTestExecutionDetail("MediaType validated "+response.getMediaType());
        logTestExecutionDetail("\n");
    }

    @Test
    public void testGetProductNonExistantPrice() throws Exception {

        logTestExecutionDetail("\nTest             | testGetProductNonExistantPrice");
        logTestExecutionDetail("Test Description | This runs a scenario where we try to get the product information when the catalog information for a product is available but the price is missing");
        String productId="12345678";
        logTestExecutionDetail("Test Data        | productId="+productId);

        webTarget = webTarget.path(productId);
        Response response = getProduct(productId, webTarget);

        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        logTestExecutionDetail("Response validated "+response.getStatus());
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));
        logTestExecutionDetail("MediaType validated "+response.getMediaType());
        logTestExecutionDetail("\n");
    }


    @Test
    public void testGetProductWhenException() throws Exception {
        String productId="-1";

        logTestExecutionDetail("\nTest             | testGetProductWhenException");
        logTestExecutionDetail("Test Description | This runs a scenario where we try to get the product information when the catalog service throws a 500 internal server error");
        webTarget = webTarget.path(productId);
        logTestExecutionDetail("Test Data        | productId="+productId);

        Response response = getProduct(productId, webTarget);
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SERVER_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));
        logTestExecutionDetail("Response validated "+response.getStatus());
        logTestExecutionDetail("MediaType validated "+response.getMediaType());
        logTestExecutionDetail("\n");
    }

    @Test
    public void testPriceUpdateApi() throws Exception {
        logTestExecutionDetail("\nTest             | testPriceUpdateApi");
        logTestExecutionDetail("Test Description | This test validates the price update when the product and price are valid");
        String productId="15643793";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);
        logTestExecutionDetail("Test Data        | updatePayload="+updatePayload);

        webTarget = webTarget.path(productId);
        Response response = getProduct(productId,webTarget);
        JSONObject jsonObject = readJSON(response);
        String jsonString = jsonObject.toJSONString();
        Double currentPrice = readPrice(jsonObject);
        String currentPriceAsString = currentPrice.toString();

        response = performPriceUpdate(updatePayload, webTarget);
        logTestExecutionDetail("Price update complete");
        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL));
        logTestExecutionDetail("Response validated "+response.getStatus());
        Double price = readPrice(response);

        String priceAsString = price.toString();

        org.junit.Assert.assertTrue( String.format("unexpected price after update %s", priceAsString),
                updatedPrice.equals(priceAsString));
        logTestExecutionDetail("Price validated after update "+priceAsString);

        response = performPriceUpdate(jsonString, webTarget);
        price = readPrice(response);
        priceAsString = price.toString();
         org.junit.Assert.assertTrue( String.format("could not restore the price after test.  %s", priceAsString),
                currentPriceAsString.equals(priceAsString));
        logTestExecutionDetail("Restored price to its original value "+priceAsString);
        logTestExecutionDetail("\n");
    }

    @Test
    public void testPriceUpdateApiWithBadPath() throws Exception {
        logTestExecutionDetail("\nTest             | testPriceUpdateApiWithBadPath");
        logTestExecutionDetail("Test Description | This test executes a scenario where we try to update price when product id in the path and payload don't match");
        String productId="156437931";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);
        logTestExecutionDetail("Test Data        | updatePayload="+updatePayload);
        logTestExecutionDetail("Test Data        | id (in path)="+productId);


        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        readJSON(response);

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.BAD_REQUEST));
        logTestExecutionDetail("Response code validated "+response.getStatus());
        logTestExecutionDetail("\n");
    }

    @Test
    public void testPriceUpdateApiWithInvalidPrice() throws Exception {
        logTestExecutionDetail("\nTest             | testPriceUpdateApiWithInvalidPrice");
        logTestExecutionDetail("Test Description | This test executes a scenario where we try to update price when the price is < 0");
        String productId="156437931";
        String updatedPrice = "-11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);
        logTestExecutionDetail("Test Data        | updatePayload="+updatePayload);

        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        readJSON(response);

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.BAD_REQUEST));
        logTestExecutionDetail("Response code validated "+response.getStatus());
        logTestExecutionDetail("\n");
    }

    @Test
    public void testPriceUpdateApiForNonExistantProduct() throws Exception {
        logTestExecutionDetail("\nTest             | testPriceUpdateApiForNonExistantProduct");
        logTestExecutionDetail("Test Description | This test executes a scenario where the price update is done for a non-existing product");
        String productId="156437931a";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"156437931a\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);
        logTestExecutionDetail("Test Data        | updatePayload="+updatePayload);
        logTestExecutionDetail("Test Data        | id (in path)="+productId);

        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        readJSON(response);

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.NOT_FOUND));

        logTestExecutionDetail("Response code validated "+response.getStatus());
        logTestExecutionDetail("\n");
    }

    @Test
    public void testPriceUpdateApiForNonExistantPrice() throws Exception {
        logTestExecutionDetail("\nTest             | testPriceUpdateApiForNonExistantPrice");
        logTestExecutionDetail("Test Description | This test executes a scenario where the price update is done for a product which does not have price but has catalog information ");
       String productId="12345678";
        String updatedPrice = "0.01";
        String updatePayload = String.format (
       "{\"id\":\"%s\",\"name\":\"test product\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",productId,updatedPrice);

        logTestExecutionDetail("Test Data        | updatePayload="+updatePayload);
        logTestExecutionDetail("Test Data        | id (in path)="+productId);

       webTarget = webTarget.path(productId);
       Response response = getProduct(productId, webTarget);

        org.junit.Assert.assertTrue(String.format("Cannot execute test. Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

        logTestExecutionDetail(String.format("Pre-update tests were successful. Response code %s",response.getStatus()));

        response = performPriceUpdate(updatePayload, webTarget);
        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL));
        logTestExecutionDetail(String.format("Price update was complete. Response code validated %s",response.getStatus()));

        Double price = readPrice(response);

        String priceAsString = price.toString();

        org.junit.Assert.assertTrue( String.format("unexpected price after update %s", priceAsString),
                updatedPrice.equals(priceAsString));

        logTestExecutionDetail(String.format("Price in the product detail is accurate %s",priceAsString));


        deletePriceForTestProduct();


    }

    @Test
    public void testGetProductWithPriceandXRequestHeader() throws Exception {

        logTestExecutionDetail("\nTest             | testGetProductWithPrice");
        logTestExecutionDetail("Test Description | This runs a positive case scenario where the catalog and price information is available for a product with x-request-id header");
        String productId="15117729";
        logTestExecutionDetail("Test Data        | productId="+productId);

        webTarget = webTarget.path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        String requestId= "test-"+System.currentTimeMillis();
        System.out.println("x-request-id :"+requestId);
        invokationBuilder.header("x-request-id",requestId);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SUCCESSFUL)));
        logTestExecutionDetail("Response validated "+response.getStatus());
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));
        logTestExecutionDetail("MediaType validated "+response.getMediaType());


        JSONObject jsonObject = readJSON(response);

        JSONObject currentPrice = (JSONObject)jsonObject.get("current_price");
        org.junit.Assert.assertTrue(String.format("Unexpected id found %s",jsonObject.get("id")),jsonObject.get("id").equals(productId));
        org.junit.Assert.assertTrue(String.format("Unexpected name found %s",jsonObject.get("name")),jsonObject.get("name").equals("Apple iPad Air 2 16GB Wi-Fi - Gold"));
        org.junit.Assert.assertTrue(String.format("Unexpected price found %s",currentPrice.get("value")),currentPrice.get("value").equals(new Double("11.01")));
        org.junit.Assert.assertTrue(String.format("Unexpected currency code found %s",currentPrice.get("currency_code")),currentPrice.get("currency_code").equals("USD"));
        logTestExecutionDetail("Price validations are successful");
        logTestExecutionDetail("\n");
    }

    private Response getProduct(String productId, WebTarget webTarget) {
       Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        return response;
    }

    private Double readPrice(Response response) throws Exception{
        JSONObject jsonObject = readJSON(response);
        return readPrice(jsonObject);
    }

    private Double readPrice(JSONObject jsonObject) {
        JSONObject currentPrice = (JSONObject)jsonObject.get("current_price");
        return (Double)currentPrice.get("value");
    }

    private JSONObject readJSON(Response response) throws Exception {
        String json = response.readEntity(String.class);
        logTestExecutionDetail("JSON : "+json);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(json);
        return jsonObject;
    }


    private Response performPriceUpdate(String s, WebTarget webTarget) {
        Invocation.Builder invokationBuilder = webTarget.request();
        Entity<String> entity = Entity.entity(s, MediaType.APPLICATION_JSON);
        String requestId = UUID.randomUUID().toString();
        System.out.println("Request id "+requestId);
        invokationBuilder.header("x-request-id",requestId );

        return invokationBuilder.put(entity);
    }

    private void deletePriceForTestProduct() {
        session.execute("delete from product_price where product_id='12345678'");
    }

    private void logTestExecutionDetail(String message) {
       System.out.println(message);
    }
}

