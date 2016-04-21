package com.myRetail.product.config;

import com.myRetail.product.resources.NotFoundExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.myRetail.product.resources.ProductResource;

/**
 * Created by koneria on 4/17/16.
 */
public class Application extends ResourceConfig {

    public Application() {
        register(ProductResource.class);
        register(NotFoundExceptionMapper.class);
    }
}

