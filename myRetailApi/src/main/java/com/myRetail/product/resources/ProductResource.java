package com.myRetail.product.resources;

import javax.inject.Inject;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;

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

/**
 * <p>
 * <code>ProductResource</code> is handler behind the REST Api provided by the service. Instance of this class serves
 * the requests for <code>GET /product/{id}</code> and (PUT /product/{id}. Instances of this class are wired with <code>ProductInfoAggregator</code>
 * which serves as a delete to aggregate catalog and pricing data for <code>GET /product/{id}</code> and processing the price
 * update for <code>PUT /product/{id}</code>
 *</p>
 *
 *
 */
@Path("product")
@Singleton
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
            logger.error(String.format("Errors found in the request :%s",errorMessage));
            throw new BadRequestException(errorMessage);
        }

        logger.info(String.format("Checking if the catalog information exists for the product : %s",id));
        getProductInfo(id);
        logger.info(String.format("Updating price for %s : %s",id,productInfo.getPriceInfo()));
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

