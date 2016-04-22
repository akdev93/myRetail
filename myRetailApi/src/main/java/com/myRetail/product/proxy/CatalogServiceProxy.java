package com.myRetail.product.proxy;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.dataAccess.AsyncDataAccess;
import com.myRetail.product.model.CatalogInfo;


public abstract class CatalogServiceProxy extends AsyncDataAccess<CatalogInfo> {

    public abstract Optional<CatalogInfo> fetchCatalogInfo(String productId);

    public Future<Optional<CatalogInfo>> fetchCatalogInfoAsync(String productId, ExecutorService pool) {
        return getDataAsync(new Object[]{productId}, pool);
    }
    protected Optional<CatalogInfo> getData(Object[] args) {
        return fetchCatalogInfo((String)args[0]);
    }

}
