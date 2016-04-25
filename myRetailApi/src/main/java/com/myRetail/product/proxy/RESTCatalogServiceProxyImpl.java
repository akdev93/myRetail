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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;


public class RESTCatalogServiceProxyImpl extends CatalogServiceProxy {

    private String targetUrl;
    private int maxConnections = 100;
    private int maxConnectionsPerHost = 20;
    private int connectTimeOut = 400;
    private int readTimeOut = 3000;
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

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }


    public void init() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig = clientConfig.connectorProvider(new ApacheConnectorProvider())
                .property(ClientProperties.CONNECT_TIMEOUT,getConnectTimeOut())
                .property(ClientProperties.READ_TIMEOUT,getReadTimeOut());
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(getMaxConnectionsPerHost());
        connectionManager.setMaxTotal(getMaxConnections());
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER,connectionManager);
        client = ClientBuilder.newClient(clientConfig);
//        client = ClientBuilder.newClient();
        logger.info(String.format("ConnectTimeout          :%s",getConnectTimeOut()));
        logger.info(String.format("ReadTimeout             :%s",getReadTimeOut()));
        logger.info(String.format("DefaultMaxConnsPerRoute :%s", getMaxConnectionsPerHost()));
        logger.info(String.format("MaxConnections          : %s",getMaxConnections()));
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
