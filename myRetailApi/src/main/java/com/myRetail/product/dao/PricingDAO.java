package com.myRetail.product.dao;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.dataAccess.AsyncDataAccess;
import com.myRetail.product.model.PriceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class PricingDAO extends AsyncDataAccess<PriceInfo> {

    private static final Logger logger = LogManager.getLogger(PricingDAO.class);

    public PricingDAO() {}


    public abstract Optional<PriceInfo> getProductPrice(String productId, String currencyCode);

    public Future<Optional<PriceInfo>> getProductPriceAsync(String productId, String currencyCode, ExecutorService pool) {
        return getDataAsync(new  Object[] {productId,currencyCode}, pool);
    }

    public abstract void insertPrice(String productId, float price, String currencyCode) ;


    protected Optional<PriceInfo> getData(Object[] args) {
        return getProductPrice((String)args[0],(String)args[1]);
    }


}
