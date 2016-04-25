package net.ak.app5;


import java.util.Map;
import java.util.HashMap;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by koneria on 4/17/16.
 */
@Path("products/v3/")
@Singleton
public class ProductResource {

    private Map<String,String> products = new HashMap<String, String>();

    public ProductResource() {
        products.put("15117729", "Apple iPad Air 2 16GB Wi-Fi - Gold");
        products.put("16483589", "iPhone 6 Plus - AT&T");
        products.put("16696652", "Beats Solo 2 Wireless Headphones - Assorted Colors");
        products.put("16752456", "Legos Super Heroes The Tumbler 76023");
        products.put("15643793", "Darley 4 Shelf Bookcase - Vintage Oak");
        products.put("18643793", "Millsboro Bookcase with Storage - Threshold");
        products.put("12345678", "test product (for test)");

    }
    @GET @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInfo getProductInfo(@PathParam("id")String id) {

        String name = products.get(id);
        System.out.println("For id "+id+" got name "+name);
        if(id.startsWith("-")) {
            throw new ServerErrorException(String.format("Failed to get info for product id %s",id), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if(name == null) {
            throw new NotFoundException(String.format("Id (%s) is not found",id));
        }

        try{
            Thread.sleep(1000);
        }catch(Exception e) {}

        ProductInfo pi = new ProductInfo(id,name);
        return pi;

    }
}
