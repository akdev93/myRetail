package com.myRetail.product.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * <p><code>Error</code> models the payload that is returned in case of any exception
 * in processing a valid or invalid request to the APIs</p>
 */
public class Error {
    @Getter
    @Setter
    private Date date;

    @Getter
    @Setter
    private String message;


    /**
     * <p>
     * Constructur - creates an instance of this class without initializing any message.
     * </p>
     * <i>needed for JAXB serialization</i>
     */
    public Error() {}

    public Error(Date d, String message ) {
        this.date = d;
        this.message = message;
    }


}
