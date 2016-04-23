package com.myRetail.product.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.myRetail.product.resources.ProductResource;

public class Application extends ResourceConfig {

    public Application() {
        register(ProductResource.class);
    }
}

