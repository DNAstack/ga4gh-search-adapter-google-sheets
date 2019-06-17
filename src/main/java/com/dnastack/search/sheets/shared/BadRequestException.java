package com.dnastack.search.sheets.shared;

/**
 * Thrown when a request cannot be processed due to an error in the request itself (a client-side error).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
