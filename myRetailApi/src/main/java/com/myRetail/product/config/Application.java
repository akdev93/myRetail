package com.myRetail.product.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.myRetail.product.resources.ProductResource;

/**
 * <code>Application</code> registers the resources for
 * the REST service. The resources in this application are
 * <ol>
 *     <li><code>ProductResource</code></li>
 * </ol>
 */
public class Application extends ResourceConfig {

    public Application() {
        register(ProductResource.class);
    }
}

