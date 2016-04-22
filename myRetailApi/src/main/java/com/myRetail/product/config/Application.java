package com.myRetail.product.config;

import com.myRetail.product.resources.NotFoundExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.myRetail.product.resources.ProductResource;

public class Application extends ResourceConfig {

    public Application() {
        register(ProductResource.class);
        register(NotFoundExceptionMapper.class);
    }
}

