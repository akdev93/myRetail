package com.myRetail.product.resources;

import com.myRetail.product.model.Error;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class ServerErrorException extends javax.ws.rs.ServerErrorException {

    public ServerErrorException(String message) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build());
    }
    public ServerErrorException(Response r) {
        super(r);
    }

    public ServerErrorException(String message, Throwable th) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build(),th);
    }
}
