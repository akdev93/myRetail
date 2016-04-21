package com.myRetail.product.dao;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.PriceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by koneria on 4/17/16.
 */
public class PricingDAO {

    private String connectHost;
    private String keyspaceName;
    private int connectPort;

    private Cluster cluster;
    private Session session;

    private static final Logger logger = LogManager.getLogger(PricingDAO.class);

    public PricingDAO() {}

    public String getConnectHost() {
        return connectHost;
    }

    public void setConnectHost(String connectHost) {
        this.connectHost = connectHost;
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    public int getConnectPort() {
        return connectPort;
    }

    public void setConnectPort(int connectPort) {
        this.connectPort = connectPort;
    }

    public void init() {

        logger.info(String.format("Connect Host  : %s ",connectHost));
        logger.info(String.format("Connect Port  : %s ",connectPort));
        logger.info(String.format("Keyspace Name : %s ",keyspaceName));

        cluster = Cluster.builder().addContactPoint(connectHost).withRetryPolicy(DefaultRetryPolicy.INSTANCE).build();
        session = cluster.connect(keyspaceName);
        logger.info(String.format("Connection established with the connect host %s for keyspace %s",connectHost,keyspaceName));
    }

    public Optional<PriceInfo> getProductPrice(String productId, String currencyCode) {
        //ResultSet results = session.execute("SELECT * FROM product_price WHERE product_id='"+productId+"' and currency_code='"+currencyCode+"'");
        //Todo - prepared statement
        ResultSet results = executeQuery(productId,"SELECT * FROM product_price WHERE product_id='"+productId+"' and currency_code='"+currencyCode+"'");
        Row r = results.one();
        Optional<PriceInfo> optional = null;
        if(r == null)
            return optional.empty();

        PriceInfo pi = new PriceInfo(productId,r.getFloat("selling_price"),r.getString("currency_code"));
        return Optional.of(pi);
    }

    public Future<Optional<PriceInfo>> getProductPriceAsync(String productId, String currencyCode, ExecutorService pool) {
        AsyncCall call = createAsyncCall(productId,currencyCode);
        return pool.submit(call);
    }


    private AsyncCall createAsyncCall(String productId, String currencyCode) {
        return new AsyncCall(productId, currencyCode);
    }

    public void close() {
        cluster.close();
    }

    private ResultSet executeQuery(String productId, String query) {
        ResultSet rs = null;
        try {
            rs = session.execute(query);
        }catch(Exception e) {
            logger.error(String.format("failed to execute query %s",query));
            throw new AppError("Error in fetching price for Product "+productId, e);
        }
        return rs;
    }

    private class AsyncCall implements Callable<Optional<PriceInfo>> {

        private String productId;
        private String currencyCode;

        public AsyncCall(String productId, String currencyCode) {
            this.productId = productId;
            this.currencyCode = currencyCode;
        }
        public Optional<PriceInfo> call() {
            return getProductPrice(productId,currencyCode);
        }
    }
}
