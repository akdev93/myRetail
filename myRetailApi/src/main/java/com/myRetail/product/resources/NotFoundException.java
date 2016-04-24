package com.myRetail.product.resources;

import com.myRetail.product.model.Error;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class NotFoundException extends javax.ws.rs.NotFoundException {

    public NotFoundException(String message) {
        super(Response.status(Response.Status.NOT_FOUND)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build());
    }
    public NotFoundException(Response r) {
        super(r);
    }

    public NotFoundException(String message, Throwable th) {
         super(Response.status(Response.Status.NOT_FOUND)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build(),th);
    }
}
