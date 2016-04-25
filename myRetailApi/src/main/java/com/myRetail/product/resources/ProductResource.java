package com.myRetail.product.resources;

import javax.inject.Inject;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.myRetail.product.aggregator.ProductInfoAggregator;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.Error;
import com.myRetail.product.model.ProductInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

/**
 * <p>
 *      <code>ProductResource</code> is handler behind the REST Api provided by the service. Instance of this class serves
 *      the requests for <code>GET /product/{id}</code> and (PUT /product/{id}. Instances of this class are wired with <code>ProductInfoAggregator</code>
 *      which serves as a delete to aggregate catalog and pricing data for <code>GET /product/{id}</code> and processing the price
 *      update for <code>PUT /product/{id}</code>
 *</p>
 * <p>
 *     The API documentation <a href="https://github.com/akdev93/myRetail">here</a> provides request payload, the response structure and
 *     http status codes  used by the API.
 * </p>
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

    /**
     * Returns the product information obtained by aggregating the catalog information from the catalog service
     * and price from the pricing database
     * @param id Product identifier (obtained from the path)
     * @param requestId Unique identifer for the request (obtained from header x-request-id)
     * @return ProductInfo
     */
    @GET @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInfo getProductInfo(@PathParam("id") String id, @HeaderParam("x-request-id")String requestId) {

        Optional<ProductInfo> optional;
        setupThreadContext(Optional.ofNullable(requestId));

        try {
            logger.warn("Defaulting curency to USD");
            logger.info(String.format("Starting Product and Price aggregation for %s ",id));
            optional = productInfoAggregator.getProductInfoMultiThreaded(id,"USD");
            logger.debug("Aggregated Product Info from productAggregator "+optional);
        }catch(AppError aE) {
            Error e = new Error(new java.util.Date(),aE.getMessage());
            throw new ServerErrorException(String.format("Unable to process request for product %s",id),aE,logger);
        }

        optional.orElseThrow(() -> new NotFoundException(String.format("Product Information not available for %s",id,logger)));

        return optional.get();
    }

    /**
     * <p>
     * Updates the price in the price database. Validates consistancy of product identifier in the payload and the
     * path element. Validates that the price is greater or equal to 0. Price is not updated  if the catalog is not
     * found for the product identifier
     * </p>
     * @param id product identifier
     * @param productInfo Product info
     * @return product info with updated price
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProductInfo setProductPrice(@PathParam("id")String id, ProductInfo productInfo) {

        List<String> errors = findErrorsInRequest(id, productInfo);
        if(!errors.isEmpty()){
            String errorMessage = errors.stream().map(i -> i.toString()).collect(Collectors.joining(", "));
            errorMessage = String.format("Errors found in the request :%s",errorMessage);
            throw new BadRequestException(errorMessage,logger);
        }

        logger.info(String.format("Updating price for %s : %s",id,productInfo.getPriceInfo()));
        Optional<ProductInfo> optPI = productInfoAggregator.updatePrice(productInfo);
        if(!optPI.isPresent()) {
            throw new NotFoundException(String.format("No catalog found for product (%s). Not updating price.",id,logger));
        }
        return optPI.get();
    }

    /**
     * <p>
     * Validates the request and returns the list of errors from the validation. Following validations are done
     * <ol>
     *     <li>The product identifier should be consistant in the path and the product info</li>
     *     <li>The price should not be less than 0</li>
     * </ol>
     * </p>
     * @param id Product identifier in the path
     * @param productInfo Product Info
     * @return List of errors(if any)
     */
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

    protected void setupThreadContext(Optional<String> optRequestId) {
        String requestId = "";
        if(!optRequestId.isPresent()) {
            logger.debug("No request id found. Generating a new one");
            requestId = UUID.randomUUID().toString();
        }else{
            requestId = optRequestId.get();
        }

        logger.debug("RequestId "+requestId);
        ThreadContext.put("requestId", requestId);
    }
}

