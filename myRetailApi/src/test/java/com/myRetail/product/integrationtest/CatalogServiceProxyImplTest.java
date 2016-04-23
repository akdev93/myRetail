package com.myRetail.product.integrationtest;

/**
 * Created by koneria on 4/22/16.
 */

import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import com.myRetail.product.proxy.RESTCatalogServiceProxyImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CatalogServiceProxyImplTest {


    private String targetUrl="http://localhost:8080/app5/myApp/products/v3";

    private RESTCatalogServiceProxyImpl catalogServiceProxy ;

    public CatalogServiceProxyImplTest(){

    }

    @Before
    public void setUp() {

        catalogServiceProxy = new RESTCatalogServiceProxyImpl();
        catalogServiceProxy.setTargetUrl(targetUrl);
        catalogServiceProxy.init();
    }

    @Test
    public void testFetchCatalogInfoWithValidProductIdSync() {

        String productId ="15117729";
        Optional<CatalogInfo> optCI = catalogServiceProxy.fetchCatalogInfo(productId);
        org.junit.Assert.assertTrue("CatalogInfo is empty ", optCI.isPresent());
        org.junit.Assert.assertTrue("Unexpected productId in response ", optCI.get().getId().equals(productId));
        org.junit.Assert.assertTrue(String.format("Unexpected Product Name in response %s",optCI.get().getName()), "product 1".equals(optCI.get().getName()));
    }

    @Test
    public void testFetchCatalogInfoWithInValidProductIdSync() {

        String productId ="1234";
        Optional<CatalogInfo> optCI = catalogServiceProxy.fetchCatalogInfo(productId);
        org.junit.Assert.assertTrue("CatalogInfo is not empty ", !optCI.isPresent());
    }

    @Test
    public void testFetchCatalogInfoWithValidProductIdASync() throws Exception {

        String productId ="15117729";
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Optional<CatalogInfo>> future = catalogServiceProxy.fetchCatalogInfoAsync(productId,pool);

        Optional<CatalogInfo> optCI = future.get();

        org.junit.Assert.assertTrue("CatalogInfo is empty ", optCI.isPresent());
        org.junit.Assert.assertTrue("Unexpected productId in response ", optCI.get().getId().equals(productId));
        org.junit.Assert.assertTrue(String.format("Unexpected Product Name in response %s",optCI.get().getName()), "product 1".equals(optCI.get().getName()));
    }


    @Test
    public void testFetchCatalogInfoWithInValidProductIdASync() throws Exception {

        String productId ="1234";
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Optional<CatalogInfo>> future = catalogServiceProxy.fetchCatalogInfoAsync(productId,pool);

        Optional<CatalogInfo> optCI = future.get();

        org.junit.Assert.assertTrue("CatalogInfo is not empty ", !optCI.isPresent());
    }

    @Test
    public void testFetchCatalogInfoWithExceptionSync() throws Exception {

        String productId ="-123";
        Optional<CatalogInfo> optCI = null;
        try {
            catalogServiceProxy.fetchCatalogInfo(productId);
            fail("No exception was thrown. Invalid test. Check environment");
        }catch(Exception e) {
            org.junit.Assert.assertTrue("Unexpected exception thrown ", (e instanceof AppError));
        }
    }

    @Test
    public void testFetchCatalogInfoWithExceptionASync() throws Exception {

        String productId ="-123";
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Optional<CatalogInfo>> future = catalogServiceProxy.fetchCatalogInfoAsync(productId,pool);

        try {
            Optional<CatalogInfo> optCI = future.get();
            fail("No exception was thrown. Invalid test. Check environment");
        }catch(ExecutionException ee) {
            org.junit.Assert.assertTrue("Unexpected exception thrown ", (ee.getCause() instanceof AppError));
        }
    }

    @After
    public void tearDown() {
        catalogServiceProxy.close();
    }
}
