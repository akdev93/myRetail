package com.myRetail.product.model;

import org.apache.logging.log4j.Logger;


/**
 * <code>AppError</code> is used to encapsulate all types of application
 * exceptions that cause an unrecoverable exception in processing a valid
 * request.
 */
public class AppError extends RuntimeException {

    public AppError(String message) {
        super(message);
    }

    public AppError(String message, Throwable cause) {
        super(message, cause);
    }

    public AppError(Throwable cause) {
        super(cause);
    }

    public AppError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AppError(String message, Throwable cause, Logger logger) {
        this(message,cause);
        logger.error(message, cause);
    }

    public AppError(String message,  Logger logger) {
        this(message);
        logger.error(message);
    }

    public AppError() {
    }
}
