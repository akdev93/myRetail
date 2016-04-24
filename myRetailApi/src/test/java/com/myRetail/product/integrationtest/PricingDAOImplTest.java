package com.myRetail.product.integrationtest;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.myRetail.product.dao.CassandraPricingDAO;
import com.myRetail.product.model.PriceInfo;
import com.myRetail.product.resources.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PricingDAOImplTest  {

    private Cluster cluster;
    private Session session;

    private CassandraPricingDAO pricingDAO;
    private static final String testProductId="1234";
    private static PreparedStatement insertPS;
    private static PreparedStatement deletePS;


    public PricingDAOImplTest() {}



    @Before
    public void setUp() {
        Properties p = TestConfig.getInstance().getProperties();

        String connectHost = p.getProperty("PricingDAO.connectHost");
        String keyspaceName = p.getProperty("PricingDAO.keyspaceName");
        int connectPort = Integer.parseInt(p.getProperty("PricingDAO.connectPort"));
        System.out.println(String.format("Connection parameters to database (%s,%s,%s)",connectHost,connectPort,keyspaceName));
        cluster = Cluster.builder().addContactPoint(connectHost).withRetryPolicy(DefaultRetryPolicy.INSTANCE).build();
        session = cluster.connect(keyspaceName);

        session.execute(String.format("insert into product_price(product_id,currency_code,selling_price) values('%s','USD',10.0)",testProductId));
        pricingDAO = new CassandraPricingDAO();
        pricingDAO.setConnectHost(connectHost);
        pricingDAO.setConnectPort(connectPort);
        pricingDAO.setKeyspaceName(keyspaceName);
        pricingDAO.init();
    }

    @After
    public void tearDown() {
        deletePrice(testProductId);
        cluster.closeAsync();
        pricingDAO.close();
    }


    @org.junit.Test
    public void testGetProductPrice() {

        Optional<PriceInfo> optPi = pricingDAO.getProductPrice(testProductId, "USD");
        org.junit.Assert.assertTrue("PriceInfo not returned for a valid price ", optPi.isPresent());
        org.junit.Assert.assertTrue(String.format("Invalid Price %f",optPi.get().getPrice()),(optPi.get().getPrice() == 10.0f));
        org.junit.Assert.assertTrue(String.format("Invalid Currency Code %s",optPi.get().getCurrencyCode()),(optPi.get().getCurrencyCode().equals("USD")));
    }

    @org.junit.Test
    public void testGetProductPriceAsync()  throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Optional<PriceInfo>> future = pricingDAO.getProductPriceAsync(testProductId,"USD",pool);
        Optional<PriceInfo> optPrice = future.get();
        org.junit.Assert.assertTrue("PriceInfo not returned for a valid price ", optPrice.isPresent());
        org.junit.Assert.assertTrue(String.format("Invalid Price %f",optPrice.get().getPrice()),(optPrice.get().getPrice() == 10.0f));
        org.junit.Assert.assertTrue(String.format("Invalid Currency Code %s",optPrice.get().getCurrencyCode()),(optPrice.get().getCurrencyCode().equals("USD")));

    }

    @org.junit.Test
    public void testGetProductPriceWhenNotFound() {
        deletePrice(testProductId);
        Optional<PriceInfo> optPi = pricingDAO.getProductPrice(testProductId, "USD");
        org.junit.Assert.assertTrue("PriceInfo not empty for a invalid price ", !optPi.isPresent());
    }

    @Test
    public void testInsertPrice() {
        deletePrice(testProductId);
        Optional<PriceInfo> optPi = pricingDAO.getProductPrice(testProductId, "USD");
        org.junit.Assert.assertTrue("PriceInfo not empty for a invalid price ", !optPi.isPresent());
        pricingDAO.insertPrice(testProductId,100.0f,"USD");
        optPi = pricingDAO.getProductPrice(testProductId,"USD");
        org.junit.Assert.assertTrue("PriceInfo empty even after insert ", optPi.isPresent());
        org.junit.Assert.assertTrue("Price value is incorrect "+optPi.get().getPrice(), optPi.get().getPrice() == 100.0f);
    }

    @Test
    public void testUpdatePrice() {
        deletePrice(testProductId);
        Optional<PriceInfo> optPi = pricingDAO.getProductPrice(testProductId, "USD");
        org.junit.Assert.assertTrue("PriceInfo not empty for a invalid price ", !optPi.isPresent());
        pricingDAO.insertPrice(testProductId,100.0f,"USD");
        optPi = pricingDAO.getProductPrice(testProductId,"USD");
        org.junit.Assert.assertTrue("PriceInfo empty even after insert ", optPi.isPresent());
        org.junit.Assert.assertTrue("Price value is incorrect "+optPi.get().getPrice(), optPi.get().getPrice() == 100.0f);
        org.junit.Assert.assertTrue("Price currency is incorrect "+optPi.get().getCurrencyCode(), optPi.get().getCurrencyCode().equals("USD"));
        pricingDAO.insertPrice(testProductId,100.1f,"USD");
        optPi = pricingDAO.getProductPrice(testProductId, "USD");
        org.junit.Assert.assertTrue("Price value is incorrect "+optPi.get().getPrice(), optPi.get().getPrice() == 100.1f);
    }

    private void deletePrice(String productId) {
        PreparedStatement ps = session.prepare("delete from product_price where product_id=?");
        BoundStatement bs = ps.bind(productId);
        session.execute(bs);
    }
}

