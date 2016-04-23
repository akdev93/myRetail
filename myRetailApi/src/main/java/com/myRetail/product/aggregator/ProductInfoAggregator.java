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



public class ProductInfoAggregator {

    private PricingDAO pricingDAO;
    private CatalogServiceProxy catalogServiceProxy;
    private ExecutorService pool;



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
    }

    public void init() {
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public Optional<ProductInfo> getProductInfo(String productId, String currencyCode) {
        Optional<CatalogInfo> optionalCI = catalogServiceProxy.fetchCatalogInfo(productId);
        Optional<PriceInfo> optionalPI = pricingDAO.getProductPrice(productId,currencyCode);
        return buildProductInfo(optionalCI, optionalPI);
    }

    public Optional<ProductInfo> getProductInfoMultiThreaded(String productId, String currencyCode) {

        Future<Optional<CatalogInfo>> futureCI = catalogServiceProxy.fetchCatalogInfoAsync(productId,pool);
        Future<Optional<PriceInfo>> futurePI = pricingDAO.getProductPriceAsync(productId,currencyCode,pool);

        Optional<CatalogInfo> optionalCI = null;
        Optional<PriceInfo> optionalPI = null;
        try {
             optionalCI = futureCI.get();
             optionalPI = futurePI.get();

        }catch(InterruptedException ieE) {
            throw new AppError("Could not fetch catalog or price info due to thread interruption ", ieE);
        }catch(ExecutionException eE) {
            AppError ae = unwrapException(eE);
            throw ae;
        }


        return buildProductInfo(optionalCI, optionalPI);
    }

    public void updatePrice(ProductInfo productInfo) {
        pricingDAO.insertPrice(productInfo.getId(),productInfo.getPriceInfo().getPrice(),productInfo.getPriceInfo().getCurrencyCode());
    }

    public void close() {
        pool.shutdown();
    }


    protected Optional<ProductInfo> buildProductInfo(Optional<CatalogInfo> optionalCI, Optional<PriceInfo> optionalPI) {
        System.out.println("ci:"+optionalCI);
        System.out.println("pi:"+optionalPI);
        if(!optionalCI.isPresent() || !optionalPI.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new ProductInfo(optionalCI.get().getId(),optionalCI.get().getName(),optionalPI.get()));
    }


    protected AppError unwrapException(ExecutionException ee) {
        Throwable t = ee.getCause();
        if(t instanceof AppError)
            return (AppError)t;
        return new AppError(t);
    }

    protected ExecutorService getPool() {
        return this.pool;
    }
}
