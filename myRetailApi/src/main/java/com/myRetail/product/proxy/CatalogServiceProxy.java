package com.myRetail.product.proxy;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.model.CatalogInfo;
/**
 * Created by koneria on 4/17/16.
 */
public abstract class CatalogServiceProxy {

    public abstract Optional<CatalogInfo> fetchCatalogInfo(String productId);

    public Future<Optional<CatalogInfo>> fetchCatalogInfoAsync(String productId, ExecutorService pool) {
        AsyncCall call = createAsyncCall(productId);
        return pool.submit(call);
    }

    private  AsyncCall createAsyncCall(String productId) {
        return new AsyncCall(productId);
    }

    private class AsyncCall implements Callable<Optional<CatalogInfo>> {
        private String productId;
        public AsyncCall(String productId) {
            this.productId = productId;
        }

        public Optional<CatalogInfo> call(){
            return fetchCatalogInfo(productId);
        }
    }
}
