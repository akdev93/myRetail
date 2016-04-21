package com.myRetail.product.resources;

import com.myRetail.product.aggregator.ProductInfoAggregator;
import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import com.myRetail.product.model.PriceInfo;
import com.myRetail.product.model.ProductInfo;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
/**
 * Created by koneria on 4/20/16.
 */

public class ProductResourceTest extends TestCase {

    public ProductResourceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ProductResourceTest.class);
    }

    public void testWhenPriceInfoIsEmpty() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        Optional<ProductInfo> optionalPI = Optional.empty();
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenReturn(optionalPI);

        ProductResource productResource = new ProductResource(productInfoAggregator);

        try {
            productResource.getProductInfo("1");
            fail("No exception was thrown when expected");
        }catch(Exception e) {
            org.junit.Assert.assertTrue(String.format("NotFoundException is not thrown when expected. Found %s", e.getClass().getName()),(e instanceof NotFoundException));
            NotFoundException nfe = (NotFoundException)e;
            org.junit.Assert.assertTrue(String.format("Invalid response code %s",nfe.getResponse().getStatusInfo().getStatusCode()),
                    nfe.getResponse().getStatusInfo().getStatusCode()==404);
        }
    }

    public void testWhenAppError() {

        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenThrow(new AppError("Error thrown for testing"));

        ProductResource productResource = new ProductResource(productInfoAggregator);

        try {
            productResource.getProductInfo("1");
            fail("no exception was thrown when expected");
        }catch(Exception e) {
            org.junit.Assert.assertTrue(String.format("ServerErrorException is not thrown when expected. Found %s",e.getClass().getName()), (e instanceof ServerErrorException));
            ServerErrorException see = (ServerErrorException)e;
            org.junit.Assert.assertTrue(String.format("Invalide response code %s",see.getResponse().getStatusInfo().getStatusCode()),
                    see.getResponse().getStatusInfo().getStatusCode() == 500);
        }

    }

    public void testValidResponseFromProductInfoAggregator() {
        ProductInfoAggregator productInfoAggregator = mock(ProductInfoAggregator.class);
        PriceInfo priceInfo = mock(PriceInfo.class);
        CatalogInfo catalogInfo = mock(CatalogInfo.class);
        Optional<ProductInfo> pi = Optional.of(new ProductInfo(catalogInfo.getId(), catalogInfo.getName(), priceInfo));
        when(productInfoAggregator.getProductInfoMultiThreaded("1","USD")).thenReturn(pi);

        ProductResource productResource = new ProductResource(productInfoAggregator);
        try {
            ProductInfo productInfo = productResource.getProductInfo("1");
        }catch(Exception e) {
            fail("Unexpected failure found even productInfoAggregator returns valid response");
        }
    }
}
