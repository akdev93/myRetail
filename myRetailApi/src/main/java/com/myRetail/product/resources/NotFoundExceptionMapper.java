package com.myRetail.product.resources;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.myRetail.product.model.Error;
/**
 * Created by koneria on 4/17/16.
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    public NotFoundExceptionMapper() { System.out.println("Created NFEM "+this);}

    @Override
    public Response toResponse(NotFoundException exception) {

        Error e = new Error(new java.util.Date(),exception.getMessage());
        Response r = Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(e).type(MediaType.APPLICATION_JSON).build();
        return r;
    }
}


