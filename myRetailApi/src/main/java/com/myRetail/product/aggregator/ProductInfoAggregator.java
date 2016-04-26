package com.myRetail.product.aggregator;


import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.dao.PricingDAO;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import com.myRetail.product.model.PriceInfo;
import com.myRetail.product.model.ProductInfo;
import com.myRetail.product.proxy.CatalogServiceProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <p>
 * <code>ProductInfoAggregator</code> aggregates the catalog information obtained from the catalog service
 * using an instance of <code>CatalogServiceProxy</code> and pricing data obtained from the price database
 * using an instance of <code>PricingDAO</code>. The class includes functions that perform this aggregation
 * synchronously and asynchronously. The instnace is configured with a thread pool at the time of start up
 * which is used for the asynchronous operations.
 * </p>
 */
public class ProductInfoAggregator {

    private PricingDAO pricingDAO;
    private CatalogServiceProxy catalogServiceProxy;
    private ExecutorService pool;

    private static final Logger logger = LogManager.getLogger(ProductInfoAggregator.class);


    private int poolSize = 1;

    public ProductInfoAggregator() {}

    /**
     * <p>Constructor - creates an instance of this class using a <code>CatalogServiceProxy</code>
     * and <code>PricingDAO</code></p>
     * @param catalogServiceProxy Catalog service proxy
     * @param pricingDAO Pricing DAO
     */
    public ProductInfoAggregator(CatalogServiceProxy catalogServiceProxy, PricingDAO pricingDAO) {
        setPricingDAO(pricingDAO);
        setCatalogServiceProxy(catalogServiceProxy);
    }


    public void setPricingDAO(PricingDAO pricingDAO) {
        this.pricingDAO = pricingDAO;
    }


    public void setCatalogServiceProxy(CatalogServiceProxy catalogServiceProxy) {
        this.catalogServiceProxy = catalogServiceProxy;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        logger.info(String.format("Pool size in ProductInfoAggregator set to %s",this.poolSize));
    }

    /**
     * Initializes the instance with a thread pool of configured size.
     */
    public void init() {
        pool = Executors.newFixedThreadPool(poolSize);
        logger.info(String.format("Initialized ProductInfoAggregator with thread pool(%s)",poolSize));
    }

    /**
     * <p>Returns the product information by looking up the catalog and the price in sequence. If the catalog
     * information is not found, no price looking up is performed. If either price or catalog is missing, the
     * product information is deemed incomplete and is not returned.</p>
     * @param productId product identifier
     * @param currencyCode currency code
     * @return product info (if available)
     */
    public Optional<ProductInfo> getProductInfo(String productId, String currencyCode) {
        logger.info(String.format("Fetching product information and price information for product id (%s) and currency code (%s)",
                productId,currencyCode));
        Optional<CatalogInfo> optionalCI = catalogServiceProxy.fetchCatalogInfo(productId);
        logger.debug("Catalog info from the catalog service is complete (Sync)");
        Optional<PriceInfo> optionalPI = pricingDAO.getProductPrice(productId,currencyCode);
        logger.debug("Price Lookup is complete (Sync)");
        return buildProductInfo(optionalCI, optionalPI);
    }

    /**
     * <p>Returns the product information by looking up the catalog and pricing in parallel. Pricing is accessed
     * even if the catalog is not present. Product information is returned if and only if both catalog and pricing
     * information are available.</p>
     * @param productId product identifier
     * @param currencyCode currency code
     * @return product info (if available)
     */
    public Optional<ProductInfo> getProductInfoMultiThreaded(String productId, String currencyCode) {

        Future<Optional<CatalogInfo>> futureCI = catalogServiceProxy.fetchCatalogInfoAsync(productId,pool);
        Future<Optional<PriceInfo>> futurePI = pricingDAO.getProductPriceAsync(productId,currencyCode,pool);

        Optional<CatalogInfo> optionalCI;
        Optional<PriceInfo> optionalPI;
        try {
             optionalCI = futureCI.get();
             logger.debug("Catalog info from the catalog service is complete (ASync)");
             optionalPI = futurePI.get();
             logger.debug("Price Lookup is complete (ASync)");
        }catch(InterruptedException ieE) {
            throw new AppError("Could not fetch catalog or price info due to thread interruption ", ieE,logger);
        }catch(ExecutionException eE) {
            throw unwrapException(eE);
        }


        logger.info(String.format("Product Information obtained for product id %s and currency code %s",
                productId, currencyCode));
        return buildProductInfo(optionalCI, optionalPI);
    }

    /**
     * <p>Updates the price with the provided price info if the catalog information for the product is available</p>
     * @param productInfo product info
     * @return product info with updated price
     */
    public Optional<ProductInfo> updatePrice(ProductInfo productInfo) {
        logger.info(String.format("Fetching the current catalog information for the product :%s",productInfo.getId()));
        Optional<CatalogInfo> optCI = catalogServiceProxy.fetchCatalogInfo(productInfo.getId());
        if(!optCI.isPresent()) {
            logger.warn(String.format("No catalog information for the product :%s",productInfo.getId()));
            return Optional.empty();
        }
        logger.info(String.format("Updating price for product %s",productInfo.getId()));
        pricingDAO.insertPrice(productInfo.getId(),productInfo.getPriceInfo().getPrice(),
                productInfo.getPriceInfo().getCurrencyCode());
        logger.info("Fetching the updating product info");
        return getProductInfo(productInfo.getId(), productInfo.getPriceInfo().getCurrencyCode());
    }


    /**
     * <p>Shuts down the thread pool</p>
     */
    public void close() {
        pool.shutdown();
        logger.info("Thread pool in ProductInfoAggregator has been shutdown");
    }


    /**
     * <p>Builds the product info form the catalog and pricing informatoin. If either of them are empty, then an empty
     * <code>Optonal</code> instance is returned.</p>
     * @param optionalCI catalog info
     * @param optionalPI price info
     * @return product info
     */
    protected Optional<ProductInfo> buildProductInfo(Optional<CatalogInfo> optionalCI, Optional<PriceInfo> optionalPI) {
        logger.debug("Catalog Info used to build ProductInfo:"+optionalCI);
        logger.debug("Price Info used to build CatalogInfo:"+optionalPI);
        if(!optionalCI.isPresent() || !optionalPI.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new ProductInfo(optionalCI.get().getId(),optionalCI.get().getName(),optionalPI.get()));
    }


    /**
     * <p>
     *     Convinience method to unwrap and throw an AppError if it the cause. If not a new AppError is created.
     * </p>
     * @param ee ExecutionException
     * @return App Error
     */
    protected AppError unwrapException(ExecutionException ee) {
        logger.error(String.format("Unwrapping Exception (%s)",ee.getClass().getName()));
        Throwable t = ee.getCause();
        if(t instanceof AppError)
            return (AppError)t;

        logger.error("Exception not of type AppError. Creating a new AppError");
        return new AppError(t);
    }

    protected ExecutorService getPool() {
        return this.pool;
    }
}
