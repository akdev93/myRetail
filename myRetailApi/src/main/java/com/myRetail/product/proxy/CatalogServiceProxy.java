package com.myRetail.product.proxy;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.dataAccess.AsyncDataAccess;
import com.myRetail.product.model.CatalogInfo;


/**
 * <p><code>CatalogServiceProxy</code> is an absraction for the proxy to the catalog service. Subclasses implement
 * the interaction with the catalog service with specific protocol and semantics. The abstraction does include
 * default functionality to enable async invokation of the call to the catalog service.</p>
 */
public abstract class CatalogServiceProxy extends AsyncDataAccess<CatalogInfo> {

    /**
     * Subclasses implement the calls to  the catalog service to fetch the catalog information. An empty
     * <code>Optional</code> is returned if no catalog is found for the product identifier
     * @param productId product identifier
     * @return CatalogInfo
     */
    public abstract Optional<CatalogInfo> fetchCatalogInfo(String productId);

    /**
     * Subclasses implement the initialization process
     */
    public abstract void init();

    /**
     * <p>
     * Calls the <code>fetchCatalogInfo</code> in asynchronously using the thread pool provided.
     * </p>
     * @param productId product identifier
     * @param pool thread pool
     * @return Future instance containg the catalog
     */
    public Future<Optional<CatalogInfo>> fetchCatalogInfoAsync(String productId, ExecutorService pool) {
        return getDataAsync(new Object[]{productId}, pool);
    }

    /**
     * <p>Calls the <code>fetchCatalogInfo</code> using the first argument in the array as the product identifier.
     * All other elements in the array (if any) are ignored.</p>
     * @param args arguments
     * @return Optional wrapped CatalogInfo
     */
    protected Optional<CatalogInfo> getData(Object[] args) {
        return fetchCatalogInfo((String)args[0]);
    }

    /**
     * <p>
     * Subclasses implement the close procedures to shutdown the instance gracefully.
     * </p>
     */
    public abstract void close();

}
