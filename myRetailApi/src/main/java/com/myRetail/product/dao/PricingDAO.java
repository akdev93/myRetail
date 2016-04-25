package com.myRetail.product.dao;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.myRetail.product.dataAccess.AsyncDataAccess;
import com.myRetail.product.model.PriceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <p><code>PricingDAO</code> is an absraction for accessing the pricing data
 * from the pricing database. Subclasses implement the interaction with the specific
 * datastores. The abstraction provides the default functionality to lookup the
 * pricing data asynchronously</p>
 */
public abstract class PricingDAO extends AsyncDataAccess<PriceInfo> {

    private static final Logger logger = LogManager.getLogger(PricingDAO.class);

    /**
     * <p>
     * Subclasses implement the interaction with the specific datastore  to retrieve the price
     * based on the product identifer and the currency code
     * </p>
     * @param productId product identifier
     * @param currencyCode currency code
     * @return price
     */
    public abstract Optional<PriceInfo> getProductPrice(String productId, String currencyCode);

    /**
     *<p>Calls <code> getProductPrice</code> asynchronously using the thread pool provided. </p>
     * @param productId product id
     * @param currencyCode currency code
     * @param pool thread pool
     * @return Future
     */
    public Future<Optional<PriceInfo>> getProductPriceAsync(String productId, String currencyCode, ExecutorService pool) {
        return getDataAsync(new  Object[] {productId,currencyCode}, pool);
    }

    /**
     * <p>
     * Subclasses provide the interaction with specific datastore to insert a price and corresponding currency code for
     * the product identified by the product id.
     * </p>
     * @param productId product id
     * @param price price
     * @param currencyCode currency code
     */
    public abstract void insertPrice(String productId, float price, String currencyCode) ;


    /**
     * <p>
     *    Calls the  <code>getProductPrice</code> using the first 2 elements in the array as product identifier and
     *    currency code respectively. If there are any additional elements in the array, they are ignored.
     * </p>
     * @param args agruments
     * @return  price (if available)
     */
    protected Optional<PriceInfo> getData(Object[] args) {
        return getProductPrice((String)args[0],(String)args[1]);
    }

}
