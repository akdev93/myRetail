package com.myRetail.product.aggregator;

import com.myRetail.product.dao.PricingDAO;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import com.myRetail.product.model.PriceInfo;
import com.myRetail.product.model.ProductInfo;
import com.myRetail.product.proxy.CatalogServiceProxy;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ProductInfoAggregatorTest {

    @org.junit.Test
    public void testSyncNoErrors() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = mock(CatalogServiceProxy.class);
        PricingDAO pricingDAO = mock(PricingDAO.class);

        CatalogInfo ci = new CatalogInfo(productId, "one");
        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");


        when(catalogServiceProxy.fetchCatalogInfo(productId)).thenReturn(Optional.of(ci));
        when(pricingDAO.getProductPrice(productId,currencyCode)).thenReturn(Optional.of(pi));

        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfo(productId,currencyCode);

        org.junit.Assert.assertTrue("Optional is empty",(optionalProductInfo.isPresent()));
        ProductInfo productInfo = optionalProductInfo.get();
        org.junit.Assert.assertTrue(String.format("product id is not as expected %s ", productInfo.getId()),productInfo.getId().equals(productId));
        org.junit.Assert.assertTrue(String.format("price is not as expected %s ", productInfo.getPriceInfo().getPrice()),productInfo.getPriceInfo().getPrice() == pi.getPrice());

    }


    @org.junit.Test
    public void testMultiThreadedNoErrors() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        CatalogInfo ci = new CatalogInfo(productId, "one");
        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");

        doReturn(Optional.of(ci)).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doReturn(Optional.of(pi)).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId,currencyCode);

        org.junit.Assert.assertTrue("Optional is empty",(optionalProductInfo.isPresent()));
        ProductInfo productInfo = optionalProductInfo.get();
        org.junit.Assert.assertTrue(String.format("product id is not as expected %s ", productInfo.getId()),productInfo.getId().equals(productId));
        org.junit.Assert.assertTrue(String.format("price is not as expected %s ", productInfo.getPriceInfo().getPrice()),productInfo.getPriceInfo().getPrice() == pi.getPrice());
    }

    @org.junit.Test
    public void testMultiThreadedNoCatalog() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");

        doReturn(Optional.empty()).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doReturn(Optional.of(pi)).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId,currencyCode);

        org.junit.Assert.assertTrue("Optional is not empty although catalog was absent",(!optionalProductInfo.isPresent()));
    }

    @org.junit.Test
    public void testMultiThreadedNoPrice() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        CatalogInfo ci = new CatalogInfo(productId, "one");

        doReturn(Optional.of(ci)).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doReturn(Optional.empty()).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId,currencyCode);

        org.junit.Assert.assertTrue("Optional is not empty although price was absent",(!optionalProductInfo.isPresent()));
    }

    @org.junit.Test
    public void testMultiThreadedNoPriceNoCatalog() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        doReturn(Optional.empty()).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doReturn(Optional.empty()).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId,currencyCode);

        org.junit.Assert.assertTrue("Optional is not empty although price and catalog was absent",(!optionalProductInfo.isPresent()));
    }

    @org.junit.Test
    public void testMultiThreadedCatalogExceptionWithAppError() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");


        doThrow(new AppError("For testing catalog exception")).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doReturn(Optional.of(pi)).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        try {
            Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId,currencyCode);
            fail("No exception was thrown when expected");
        }catch(Exception e) {
            org.junit.Assert.assertTrue(String.format("Unexpected exception type %s",e.getClass().getName()), (e instanceof AppError));
        }

    }

    @org.junit.Test
    public void testMultiThreadedPriceExceptionAppError() {
        String productId="1";
        String currencyCode="USD";
        CatalogServiceProxy catalogServiceProxy = spy(CatalogServiceProxy.class);
        PricingDAO pricingDAO = spy(PricingDAO.class);

        CatalogInfo ci = new CatalogInfo(productId, "one");
        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");

        doReturn(Optional.of(ci)).when(catalogServiceProxy).fetchCatalogInfo(productId);
        doThrow(new AppError("For testing price exception")).when(pricingDAO).getProductPrice(productId,currencyCode);


        ProductInfoAggregator aggregator = new ProductInfoAggregator(catalogServiceProxy, pricingDAO);
        aggregator.init();
        try {
            Optional<ProductInfo> optionalProductInfo = aggregator.getProductInfoMultiThreaded(productId, currencyCode);
            fail("No exception was thrown when expected");
        }catch(Exception e) {
            org.junit.Assert.assertTrue(String.format("Unexpected exception type %s",e.getClass().getName()), (e instanceof AppError));
        }
    }

    @org.junit.Test
    public void testClose() {
        ProductInfoAggregator pia = new ProductInfoAggregator();
        pia.init();
        org.junit.Assert.assertTrue("Pool is not up", !pia.getPool().isShutdown());
        pia.close();
        org.junit.Assert.assertTrue("Pool is not shutdown after close()", pia.getPool().isShutdown());
    }

    @org.junit.Test
    public void testInit() {
        ProductInfoAggregator pia = new ProductInfoAggregator();
        pia.init();
        org.junit.Assert.assertTrue("Pool is not up", !pia.getPool().isShutdown());
    }

    @org.junit.Test
    public void testBuildProductInfoTestWithCatalogAndPrice(){
        String productId="1";
        CatalogInfo ci = new CatalogInfo(productId, "one");
        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");
        Optional<CatalogInfo> optCI = Optional.of(ci);
        Optional<PriceInfo> optPI = Optional.of(pi);

        ProductInfoAggregator pia = new ProductInfoAggregator();
        Optional<ProductInfo> optProductInfo = pia.buildProductInfo(optCI,optPI);
        org.junit.Assert.assertTrue("Product Info is absent", optProductInfo.isPresent());
        org.junit.Assert.assertTrue("Unexpected product Id ", optProductInfo.get().getId().equals(ci.getId()));
        org.junit.Assert.assertTrue("Unexpected product Name ", optProductInfo.get().getName().equals(ci.getName()));
        org.junit.Assert.assertTrue("Unexpected Price ", optProductInfo.get().getPriceInfo().getPrice() == pi.getPrice());
    }

    @org.junit.Test
    public void testBuildProductInfoTestWithNoCatalog(){
        String productId="1";
        PriceInfo pi = new PriceInfo(productId,1.0f,"USD");
        Optional<CatalogInfo> optCI = Optional.empty();
        Optional<PriceInfo> optPI = Optional.of(pi);

        ProductInfoAggregator pia = new ProductInfoAggregator();
        Optional<ProductInfo> optProductInfo = pia.buildProductInfo(optCI,optPI);
        org.junit.Assert.assertTrue("Product Info is not absent although there is no catalog", !optProductInfo.isPresent());
    }

    @org.junit.Test
    public void testBuildProductInfoTestWithNoPrice(){
        String productId="1";
        CatalogInfo ci = new CatalogInfo(productId, "one");
        Optional<CatalogInfo> optCI = Optional.of(ci);
        Optional<PriceInfo> optPI = Optional.empty();

        ProductInfoAggregator pia = new ProductInfoAggregator();
        Optional<ProductInfo> optProductInfo = pia.buildProductInfo(optCI,optPI);
        org.junit.Assert.assertTrue("Product Info is not absent although there is no price", !optProductInfo.isPresent());
    }

    @Test
    public void testUpdatePriceWhereNoCatalogExists() {
        String productId="1";
        ProductInfo productInfo = new ProductInfo("1", "product 1", new PriceInfo("1",1.0f,"USD"));
        Optional<CatalogInfo> optCI = Optional.empty();
        CatalogServiceProxy catalogServiceProxy = mock(CatalogServiceProxy.class);
        PricingDAO pricingDAO = mock(PricingDAO.class);

        doReturn(optCI).when(catalogServiceProxy).fetchCatalogInfo(productId);

        ProductInfoAggregator pia = new ProductInfoAggregator();
        pia.setCatalogServiceProxy(catalogServiceProxy);
        pia.setPricingDAO(pricingDAO);
        Optional<ProductInfo> optPI = pia.updatePrice(productInfo);
        org.junit.Assert.assertTrue("Product Info is not empty although there is no catalog", !optPI.isPresent());
    }


}
