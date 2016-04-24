package net.ak.myRetailBlackBoxTests;


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


import static org.junit.Assert.*;

/**
 * Blackbox test for myRetail Product API.
 */
public class ProductApiTest

{
    private String testTargetURL;
    private Client client;
    private WebTarget webTarget;


    @Before
    public void setUp() {
        testTargetURL = TestConfig.getInstance().getTestUrl()+"/product";
        System.out.println("Using URL : "+testTargetURL);
        client = ClientBuilder.newClient();
        webTarget = client.target(testTargetURL);
    }

    @After
    public void tearDown() {
        client.close();

    }

    @Test
    public void testAProductWithPrice() throws Exception {
        String productId="15117729";

        webTarget = webTarget.path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SUCCESSFUL)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));


        JSONObject jsonObject = readJSON(response);

        JSONObject currentPrice = (JSONObject)jsonObject.get("current_price");
        org.junit.Assert.assertTrue(String.format("Unexpected id found %s",jsonObject.get("id")),jsonObject.get("id").equals(productId));
        org.junit.Assert.assertTrue(String.format("Unexpected name found %s",jsonObject.get("name")),jsonObject.get("name").equals("product 1"));
        org.junit.Assert.assertTrue(String.format("Unexpected price found %s",currentPrice.get("value")),currentPrice.get("value").equals(new Double("11.01")));
        org.junit.Assert.assertTrue(String.format("Unexpected currency code found %s",currentPrice.get("currency_code")),currentPrice.get("currency_code").equals("USD"));
    }

    @Test
    public void testNonExistantProduct() throws Exception {
        String productId="123";

        webTarget = webTarget.path(productId);
        Response response = getProduct(productId, webTarget);
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }

    @Test
    public void testNonExistantPrice() throws Exception {
        String productId="12345678";

        webTarget = webTarget.path(productId);
        Response response = getProduct(productId, webTarget);

        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }


    @Test
    public void testException() throws Exception {
        String productId="-1";

        webTarget = webTarget.path(productId);

        Response response = getProduct(productId, webTarget);
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SERVER_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }

    @Test
    public void testPriceUpdateApi() throws Exception {
        String productId="15643793";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);



        webTarget = webTarget.path(productId);
        Response response = getProduct(productId,webTarget);
        JSONObject jsonObject = readJSON(response);
        String jsonString = jsonObject.toJSONString();
        Double currentPrice = readPrice(jsonObject);
        String currentPriceAsString = currentPrice.toString();
        System.out.println("Current : "+jsonString);

        response = performPriceUpdate(updatePayload, webTarget);
        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL));

        Double price = readPrice(response);

        String priceAsString = price.toString();

        org.junit.Assert.assertTrue( String.format("unexpected price after update %s", priceAsString),
                updatedPrice.equals(priceAsString));

        response = performPriceUpdate(jsonString, webTarget);
        price = readPrice(response);
        priceAsString = price.toString();
         org.junit.Assert.assertTrue( String.format("could not restore the price after test.  %s", priceAsString),
                currentPriceAsString.equals(priceAsString));
    }

    @Test
    public void testPriceUpdateApiWithBadPath() throws Exception {
        String productId="156437931";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);



        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        System.out.println(response.readEntity(String.class));

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.BAD_REQUEST));
    }

    @Test
    public void testPriceUpdateApiWithInvalidPrice() throws Exception {
        String productId="156437931";
        String updatedPrice = "-11.0512";
        String updatePayload = String.format (
                "{\"id\":\"15643793\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);


        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        System.out.println(response.readEntity(String.class));

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.BAD_REQUEST));
    }

    @Test
    public void testPriceUpdateApiForNonExistantProduct() throws Exception {
        String productId="156437931a";
        String updatedPrice = "11.0512";
        String updatePayload = String.format (
                "{\"id\":\"156437931a\",\"name\":\"product 5\",\"current_price\":{\"value\":%s,\"currency_code\":\"USD\"}}",updatedPrice);


        webTarget = webTarget.path(productId);
        Response response = performPriceUpdate(updatePayload,webTarget);
        System.out.println(response.readEntity(String.class));

        org.junit.Assert.assertTrue( String.format("Response has unexpected status %s", response.getStatus()),
                response.getStatusInfo().equals(Response.Status.NOT_FOUND));
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
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(json);
        return jsonObject;
    }


    private Response performPriceUpdate(String s, WebTarget webTarget) {
        Invocation.Builder invokationBuilder = webTarget.request();
        Entity<String> entity = Entity.entity(s, MediaType.APPLICATION_JSON);

        return invokationBuilder.put(entity);
    }
}

