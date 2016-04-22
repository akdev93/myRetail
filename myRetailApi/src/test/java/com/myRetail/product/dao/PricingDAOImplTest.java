package com.myRetail.product.dao;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.myRetail.product.model.PriceInfo;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


public class PricingDAOImplTest  {

    private Cluster cluster;
    private Session session;
    private String connectHost = "localhost";
    private String keyspaceName = "product_test";
    private int connectPort = 7000;
    private CassandraPricingDAO pricingDAO;
    private static final String testProductId="1234";
    private static PreparedStatement insertPS;
    private static PreparedStatement deletePS;


    public PricingDAOImplTest() {}



    @Before
    public void setUp() {
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
        long t1 = System.currentTimeMillis();
        cluster.closeAsync();
        long t2 = System.currentTimeMillis();
        System.out.println("Time to close "+(t2-t1));
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

    private void deletePrice(String productId) {
        PreparedStatement ps = session.prepare("delete from product_price where product_id=?");
        BoundStatement bs = ps.bind(productId);
        session.execute(bs);
    }
}

