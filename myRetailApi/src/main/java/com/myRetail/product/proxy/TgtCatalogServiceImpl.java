package com.myRetail.product.proxy;

import java.util.Optional;
import com.myRetail.product.model.CatalogInfo;

/**
 * Created by koneria on 4/18/16.
 */
public class TgtCatalogServiceImpl extends CatalogServiceProxy {

    public Optional<CatalogInfo> fetchCatalogInfo(String productId) {
        return Optional.empty();
    }
}
