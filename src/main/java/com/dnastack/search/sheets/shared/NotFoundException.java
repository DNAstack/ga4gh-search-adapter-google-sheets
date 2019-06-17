package com.dnastack.search.sheets.shared;

/**
 * Thrown when a requested data item cannot be found.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
