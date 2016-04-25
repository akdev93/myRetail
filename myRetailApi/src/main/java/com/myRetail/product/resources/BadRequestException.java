package com.myRetail.product.resources;

import com.myRetail.product.model.Error;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * <p>
 *     <code>BadRequestException</code> extends <code>javax.ws.rs.BadRequestException</code> to
 *     hardwire the <code>Response.Status</code> , the <code>MediaType</code> and the <code>Entity</code>
 *     used in the error when the request is not valid. Some constructures accept a logger to
 *     log the stacktrace(if any) and the error message.
 * </p>
 */
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

    public BadRequestException(String message, Logger logger) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build());
        logger.error(message);
    }

    public BadRequestException(String message, Throwable th, Logger logger) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(new Error(new java.util.Date(), message))
                .type(MediaType.APPLICATION_JSON).build(),th);
        logger.error(message);
        logger.error(th);
    }
}
