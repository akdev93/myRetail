package com.myRetail.product.proxy;

import java.util.Optional;
import com.myRetail.product.model.CatalogInfo;


public class TgtCatalogServiceImpl extends CatalogServiceProxy {

    public Optional<CatalogInfo> fetchCatalogInfo(String productId) {
        return Optional.empty();
    }

    public void init() {

    }

    public void close() {

    }
}
