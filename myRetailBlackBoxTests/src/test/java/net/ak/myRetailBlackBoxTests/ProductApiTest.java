package net.ak.myRetailBlackBoxTests;


import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;


import static org.junit.Assert.*;

/**
 * Blackbox test for myRetail Product API.
 */
public class ProductApiTest

{
    private String testTargetURL = "http://localhost:8080/myRetailApi/product/";
    private Client client;
    private WebTarget webTarget;


    @Before
    public void setUp() {
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

        String json = response.readEntity(String.class);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(json);
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

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }

    @Test
    public void testNonExistantPrice() throws Exception {
        String productId="12345678";

        webTarget = webTarget.path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.CLIENT_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }


    @Test
    public void testException() throws Exception {
        String productId="-1";

        webTarget = webTarget.path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokationBuilder.get();
        org.junit.Assert.assertTrue(String.format("Response had unexpected status code %s",response.getStatus()),
                response.getStatusInfo().getFamily().equals((Response.Status.Family.SERVER_ERROR)));
        org.junit.Assert.assertTrue( String.format("Response has unexpected media type %s", response.getMediaType().toString()),
                response.getMediaType().toString().equals("application/json"));

    }

}

