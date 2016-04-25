package com.myRetail.product.resources;

import com.myRetail.product.aggregator.ProductInfoAggregator;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import com.myRetail.product.model.PriceInfo;
import com.myRetail.product.model.ProductInfo;
import org.junit.Test;


import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


public class ProductResourceTest {


    @org.junit.Test
    public void testWhenPriceInfoIsEmpty() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        Optional<ProductInfo> optionalPI = Optional.empty();
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenReturn(optionalPI);

        ProductResource productResource = new ProductResource(productInfoAggregator);

        try {
            productResource.getProductInfo("1","fake-request-id");
            fail("No exception was thrown when expected");
        }catch(Exception e) {
            e.printStackTrace();
            org.junit.Assert.assertTrue(String.format("NotFoundException is not thrown when expected. Found %s", e.getClass().getName()),(e instanceof NotFoundException));
            NotFoundException nfe = (NotFoundException)e;
            org.junit.Assert.assertTrue(String.format("Invalid response code %s",nfe.getResponse().getStatusInfo().getStatusCode()),
                    nfe.getResponse().getStatusInfo().getStatusCode()==404);
        }
    }

    @org.junit.Test
    public void testWhenAppError() {

        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenThrow(new AppError("Error thrown for testing"));

        ProductResource productResource = new ProductResource(productInfoAggregator);

        try {
            productResource.getProductInfo("1", "fake-request-id");
            fail("no exception was thrown when expected");
        }catch(Exception e) {
            org.junit.Assert.assertTrue(String.format("ServerErrorException is not thrown when expected. Found %s",e.getClass().getName()), (e instanceof ServerErrorException));
            ServerErrorException see = (ServerErrorException)e;
            org.junit.Assert.assertTrue(String.format("Invalide response code %s",see.getResponse().getStatusInfo().getStatusCode()),
                    see.getResponse().getStatusInfo().getStatusCode() == 500);
        }

    }

    @org.junit.Test
    public void testValidResponseFromProductInfoAggregator() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        PriceInfo priceInfo = mock(PriceInfo.class);
        CatalogInfo catalogInfo = mock(CatalogInfo.class);
        Optional<ProductInfo> pi = Optional.of(new ProductInfo(catalogInfo.getId(), catalogInfo.getName(), priceInfo));
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenReturn(pi);

        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo productInfo = productResource.getProductInfo("1", "fake-request-id");
        }catch(Exception e) {
            fail("Unexpected failure found even productInfoAggregator returns valid response");
        }
    }

    @Test
    public void testFindErrorsInRequestWithInvalidPath() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        ProductResource productResource = new ProductResource(productInfoAggregator);

        String path = "1234";
        PriceInfo pi = new PriceInfo("12345",10.5f,"USD");
        ProductInfo productInfo = new ProductInfo("12345","test product",pi);
        List<String> errors = productResource.findErrorsInRequest(path, productInfo);

        org.junit.Assert.assertTrue("No errors found when expected", !errors.isEmpty());
        org.junit.Assert.assertTrue(String.format("Unexpected number of errors (%s)",errors.size()), errors.size()==1);
    }

    @Test
    public void testFindErrorsInRequestWithInvalidPrice() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        ProductResource productResource = new ProductResource(productInfoAggregator);

        String path = "12345";
        PriceInfo pi = new PriceInfo("12345",-10.5f,"USD");
        ProductInfo productInfo = new ProductInfo("12345","test product",pi);
        List<String> errors = productResource.findErrorsInRequest(path, productInfo);

        org.junit.Assert.assertTrue("No errors found when expected", !errors.isEmpty());
        org.junit.Assert.assertTrue(String.format("Unexpected number of errors (%s)",errors.size()), errors.size()==1);
    }

    @Test
    public void testFindErrorsInRequestWithInvalidPriceAndPath() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        ProductResource productResource = new ProductResource(productInfoAggregator);

        String path = "1234";
        PriceInfo pi = new PriceInfo("12345",-10.5f,"USD");
        ProductInfo productInfo = new ProductInfo("12345","test product",pi);
        List<String> errors = productResource.findErrorsInRequest(path, productInfo);

        org.junit.Assert.assertTrue("No errors found when expected", !errors.isEmpty());
        org.junit.Assert.assertTrue(String.format("Unexpected number of errors (%s)",errors.size()), errors.size()==2);
    }


    @Test
    public void testFindErrorsInRequestWithValidPriceAndPath() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        ProductResource productResource = new ProductResource(productInfoAggregator);

        String path = "12345";
        PriceInfo pi = new PriceInfo("12345",10.5f,"USD");
        ProductInfo productInfo = new ProductInfo("12345","test product",pi);
        List<String> errors = productResource.findErrorsInRequest(path, productInfo);

        org.junit.Assert.assertTrue("Errors found when expected", errors.isEmpty());
    }

    @Test
    public void testSetProductPrice() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        String path = "12345";
        PriceInfo pi = new PriceInfo(path,10.5f,"USD");
        ProductInfo productInfo = new ProductInfo(path,"test product",pi);

        when(productInfoAggregator.updatePrice(productInfo)).thenReturn(Optional.of(productInfo));
        when(productInfoAggregator.getProductInfoMultiThreaded(path,"USD")).thenReturn(Optional.of(productInfo));
        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo prodInfo = productResource.setProductPrice(path, productInfo);
        }catch(Exception e ) {
            e.printStackTrace();
            fail(String.format("unexpected exception %s",e.getMessage()));
        }
    }

    @Test
    public void testSetProductPriceWithInvalidPath() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        String path = "12345";
        PriceInfo pi = new PriceInfo("1234",10.5f,"USD");
        ProductInfo productInfo = new ProductInfo("1234","test product",pi);

        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo prodInfo = productResource.setProductPrice(path, productInfo);
            fail("No exception thrown for a bad request");
        }catch(Exception e ) {
            org.junit.Assert.assertTrue(String.format("Unexpected exception thrown %s",e.getClass().getName()), (e instanceof BadRequestException));
        }
    }

    @Test
    public void testSetProductPriceWithBadPrice() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        String path = "12345";
        PriceInfo pi = new PriceInfo(path,-10.5f,"USD");
        ProductInfo productInfo = new ProductInfo(path,"test product",pi);

        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo prodInfo = productResource.setProductPrice(path, productInfo);
            fail("No exception thrown for a bad request");
        }catch(Exception e ) {
            org.junit.Assert.assertTrue(String.format("Unexpected exception thrown %s",e.getClass().getName()), (e instanceof BadRequestException));
        }
    }

    @Test
    public void testSetProductPriceForNonExistantProduct() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        String path = "12345";
        PriceInfo pi = new PriceInfo(path,10.5f,"USD");
        ProductInfo productInfo = new ProductInfo(path,"test product",pi);

        when(productInfoAggregator.updatePrice(productInfo)).thenReturn(Optional.of(productInfo));
        when(productInfoAggregator.getProductInfoMultiThreaded(path,"USD")).thenReturn(Optional.empty());
        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo prodInfo = productResource.setProductPrice(path, productInfo);
        }catch(Exception e ) {
            org.junit.Assert.assertTrue(String.format("Unexpected exception thrown %s",e.getClass().getName()), (e instanceof com.myRetail.product.resources.NotFoundException));
        }
    }
}
