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


public class ProductInfoAggregator {

    private PricingDAO pricingDAO;
    private CatalogServiceProxy catalogServiceProxy;
    private ExecutorService pool;

    private static final Logger logger = LogManager.getLogger(ProductInfoAggregator.class);


    private int poolSize = 1;

    public ProductInfoAggregator() {}

    public ProductInfoAggregator(CatalogServiceProxy catalogServiceProxy, PricingDAO pricingDAO) {
        setPricingDAO(pricingDAO);
        setCatalogServiceProxy(catalogServiceProxy);
    }
    public PricingDAO getPricingDAO() {
        return pricingDAO;
    }

    public void setPricingDAO(PricingDAO pricingDAO) {
        this.pricingDAO = pricingDAO;
    }

    public CatalogServiceProxy getCatalogServiceProxy() {
        return catalogServiceProxy;
    }

    public void setCatalogServiceProxy(CatalogServiceProxy catalogServiceProxy) {
        this.catalogServiceProxy = catalogServiceProxy;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        logger.info(String.format("Pool size in ProductInfoAggregator set to %s",this.poolSize));
    }

    public void init() {
        pool = Executors.newFixedThreadPool(poolSize);
        logger.info(String.format("Initialized ProductInfoAggregator with thread pool(%s)",poolSize));
    }

    public Optional<ProductInfo> getProductInfo(String productId, String currencyCode) {
        logger.info(String.format("Fetching product information and price information for product id (%s) and currency code (%s)",
                productId,currencyCode));
        Optional<CatalogInfo> optionalCI = catalogServiceProxy.fetchCatalogInfo(productId);
        logger.debug("Catalog info from the catalog service is complete (Sync)");
        Optional<PriceInfo> optionalPI = pricingDAO.getProductPrice(productId,currencyCode);
        logger.debug("Price Lookup is complete (Sync)");
        return buildProductInfo(optionalCI, optionalPI);
    }

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

    public Optional<ProductInfo> updatePrice(ProductInfo productInfo) {
        logger.info(String.format("Updating price for product %s",productInfo.getId()));
        pricingDAO.insertPrice(productInfo.getId(),productInfo.getPriceInfo().getPrice(),
                productInfo.getPriceInfo().getCurrencyCode());
        logger.info("Fetching the updating product info");
        return getProductInfo(productInfo.getId(), productInfo.getPriceInfo().getCurrencyCode());
    }

    public void close() {
        pool.shutdown();
        logger.info("Thread pool in ProductInfoAggregator has been shutdown");
    }


    protected Optional<ProductInfo> buildProductInfo(Optional<CatalogInfo> optionalCI, Optional<PriceInfo> optionalPI) {
        logger.debug("Catalog Info used to build ProductInfo:"+optionalCI);
        logger.debug("Price Info used to build CatalogInfo:"+optionalPI);
        if(!optionalCI.isPresent() || !optionalPI.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new ProductInfo(optionalCI.get().getId(),optionalCI.get().getName(),optionalPI.get()));
    }


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
