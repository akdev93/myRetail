package com.myRetail.product.resources;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.myRetail.product.aggregator.ProductInfoAggregator;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.Error;
import com.myRetail.product.model.ProductInfo;

import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * Created by koneria on 4/17/16.
 */
@Path("product")
public class ProductResource {

    @Inject
    private ProductInfoAggregator productInfoAggregator;

    private static final Logger logger = LogManager.getLogger(ProductResource.class);

    public ProductResource() {
    }

    public ProductResource(ProductInfoAggregator productInfoAggregator) {
        this.productInfoAggregator = productInfoAggregator;
    }

    @GET @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInfo getProductInfo(@PathParam("id")String id) {

        Optional<ProductInfo> optional;
        try {
            logger.warn("Defaulting curency to USD");
            logger.info(String.format("Starting Product and Price aggregation for %s ",id));
            optional = productInfoAggregator.getProductInfoMultiThreaded(id,"USD");
            logger.debug("Aggregated Product Info from productAggregator "+optional);
        }catch(AppError aE) {
            logger.error("Failed to process request for product "+id, aE);
            Error e = new Error(new java.util.Date(),aE.getMessage());
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).type(MediaType.APPLICATION_JSON).build();
            throw new ServerErrorException(String.format("Unable to process request for product %s",id), response, aE);
        }

        optional.orElseThrow(() -> new NotFoundException(String.format("Product Information not available for %s",id)));

        return optional.get();
    }

}

