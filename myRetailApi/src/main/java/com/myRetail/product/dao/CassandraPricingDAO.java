package com.myRetail.product.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.PriceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class CassandraPricingDAO extends PricingDAO {

    private String connectHost;
    private String keyspaceName;
    private int connectPort;

    private Cluster cluster;
    private Session session;
    private PreparedStatement psSelect;
    private PreparedStatement psInsert;

    private static final Logger logger = LogManager.getLogger(PricingDAO.class);

    public CassandraPricingDAO() {}

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

        cluster = Cluster.builder().addContactPoint(connectHost).withPort(connectPort).withRetryPolicy(DefaultRetryPolicy.INSTANCE).build();
        session = cluster.connect(keyspaceName);
        logger.info(String.format("Connection established with the connect host %s for keyspace %s",connectHost,keyspaceName));
        psSelect = session.prepare("select * from product_price where product_id=? and currency_code=?");
        psInsert = session.prepare("insert into product_price(product_id,currency_code,selling_price) values(?,?,?)");
    }

    public Optional<PriceInfo> getProductPrice(String productId, String currencyCode) {
        ResultSet results = executeQuery(productId,currencyCode);

        Row r = results.one();
        Optional<PriceInfo> optional = null;
        if(r == null)
            return optional.empty();

        PriceInfo pi = new PriceInfo(productId,r.getFloat("selling_price"),r.getString("currency_code"));
        return Optional.of(pi);
    }

    public void insertPrice(String productId, float price, String currencyCode) {
        try {
            session.execute(psInsert.bind(productId,currencyCode, new Float(price)));
        }catch(Exception e) {
            throw new AppError(String.format("Error in inserting price (%f) for product (%s) in currency (%s)",price,productId,currencyCode),e);
        }
    }

    public void close() {
        cluster.close();
    }


    private ResultSet executeQuery(String productId,String currencyCode) {
        ResultSet rs = null;

        try {
            BoundStatement bs = psSelect.bind(productId,currencyCode);
            rs = session.execute(bs);
        }catch(Exception e) {
            logger.error(String.format("failed to execute query %s with parameters %s %s", psSelect.getQueryString(), productId,currencyCode));
            throw new AppError("Error in fetching price for Product "+productId, e);
        }
        return rs;
    }

}
