package com.myRetail.product.resources;

import javax.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.myRetail.product.aggregator.ProductInfoAggregator;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.Error;
import com.myRetail.product.model.ProductInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


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
    public ProductInfo getProductInfo(@PathParam("id") String id) {

        Optional<ProductInfo> optional;
        try {
            logger.warn("Defaulting curency to USD");
            logger.info(String.format("Starting Product and Price aggregation for %s ",id));
            optional = productInfoAggregator.getProductInfoMultiThreaded(id,"USD");
            logger.debug("Aggregated Product Info from productAggregator "+optional);
        }catch(AppError aE) {
            logger.error("Failed to process request for product "+id, aE);
            Error e = new Error(new java.util.Date(),aE.getMessage());
            throw new ServerErrorException(String.format("Unable to process request for product %s",id),aE);
        }

        optional.orElseThrow(() -> new NotFoundException(String.format("Product Information not available for %s",id)));

        return optional.get();
    }


    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInfo setProductPrice(@PathParam("id")String id, ProductInfo productInfo) {

        List<String> errors = findErrorsInRequest(id, productInfo);
        if(!errors.isEmpty()){
            String errorMessage = errors.stream().map(i -> i.toString()).collect(Collectors.joining(", "));
            throw new BadRequestException(errorMessage);
        }

        getProductInfo(id);
        return productInfoAggregator.updatePrice(productInfo).get();
    }

    protected List<String> findErrorsInRequest(String id, ProductInfo productInfo) {

        List errors = new ArrayList<String>();
        if(!id.equals(productInfo.getId())) {
            errors.add(String.format("Product id in the payload(%s) does not match the path (%s)",productInfo.getId(), id));
        }
        if(productInfo.getPriceInfo().getPrice() < 0) {
            errors.add(String.format("Product Price cannot be negative (%s)",productInfo.getPriceInfo().getPrice()));

        }
        return errors;
    }

}

