package com.delivery.exception;

/**
 * Exception thrown for invalid delivery method or date combinations.
 */
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
}
