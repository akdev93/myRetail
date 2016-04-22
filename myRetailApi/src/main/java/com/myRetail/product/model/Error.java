package com.myRetail.product.model;

import java.util.Date;



public class Error {
    private Date date;
    private String message;

    public Error() {}

    public Error(Date d, String message ) {
        this.date = d;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
