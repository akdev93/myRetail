package com.myRetail.product.resources;

import com.myRetail.product.model.Error;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class BadRequestException extends javax.ws.rs.BadRequestException {

    public BadRequestException(String message) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build());
    }
    public BadRequestException(Response r) {
        super(r);
    }

    public BadRequestException(String message, Throwable th) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build(),th);
    }
}
