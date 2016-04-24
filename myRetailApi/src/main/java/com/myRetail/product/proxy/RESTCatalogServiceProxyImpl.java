package com.myRetail.product.proxy;

import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.myRetail.product.model.AppError;
import com.myRetail.product.model.CatalogInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RESTCatalogServiceProxyImpl extends CatalogServiceProxy {

    private String targetUrl;

    private Client client;

    private static final Logger logger = LogManager.getLogger(RESTCatalogServiceProxyImpl.class);


    public Optional<CatalogInfo> fetchCatalogInfo(String productId) {

        Optional<CatalogInfo> optional = null;
        WebTarget webTarget = client.target(targetUrl).path(productId);

        Invocation.Builder invokationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invokeRestAPI(productId, invokationBuilder);

        return getCatalogInfoFromResponse(response,productId);
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        logger.info("Setting up targetUrl to "+targetUrl);
        this.targetUrl = targetUrl;
    }

    public void init() {
        client = ClientBuilder.newClient();
        logger.info("Created client for the webservice");
    }

    private Response invokeRestAPI(String productId, Invocation.Builder invocationBuilder) {
        Response r = null;
        try {
            logger.debug(String.format("Calling the service for %s ",productId));
            r = invocationBuilder.get();
            logger.debug(String.format("Response obtained from service for %s ",productId));
        }catch(Exception e) {
            throw new AppError("Unable to get the catalog information for product "+productId, e,logger);
        }
        return r;
    }

    private Optional<CatalogInfo> getCatalogInfoFromResponse(Response r, String productId) {

        Optional<CatalogInfo> optional = null;
        logger.debug(String.format("Response status code %s and family ",
                r.getStatus(),r.getStatusInfo().getFamily()));

        if(r.getStatusInfo().getFamily().equals((Response.Status.Family.SUCCESSFUL))) {
            optional = Optional.of(r.readEntity(CatalogInfo.class));
        } else if(r.getStatusInfo().getFamily().equals(Response.Status.Family.CLIENT_ERROR)) {
            optional = Optional.empty();
        } else {
            throw new AppError(
                String.format("Catalog fetch failure (Unexpected Http Status %s) when processing %s ",
                        r.getStatus(),productId),logger);
        }
        logger.info(String.format("Catalog info for %s:%s ",productId,optional));
        return optional;
    }

    public void close() {
        client.close();
        logger.info("Closed connections to the webservice");
    }
}
