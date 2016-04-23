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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void init() {
        client = ClientBuilder.newClient();
    }

    private Response invokeRestAPI(String productId, Invocation.Builder invocationBuilder) {
        Response r = null;
        try {
            r = invocationBuilder.get();
        }catch(Exception e) {
            throw new AppError("Unable to get the catalog information for product "+productId, e,logger);
        }
        return r;
    }

    private Optional<CatalogInfo> getCatalogInfoFromResponse(Response r, String productId) {

        Optional<CatalogInfo> optional = null;

        if(r.getStatusInfo().getFamily().equals((Response.Status.Family.SUCCESSFUL))) {
            optional = Optional.of(r.readEntity(CatalogInfo.class));
        } else if(r.getStatusInfo().getFamily().equals(Response.Status.Family.CLIENT_ERROR)) {
            optional = Optional.empty();
        } else {
            throw new AppError("Catalog fetch failure (Unexpected Http Status "+r.getStatus()+ ") when processing "+productId);
        }
        return optional;
    }

    public void close() {
        client.close();
    }
}
