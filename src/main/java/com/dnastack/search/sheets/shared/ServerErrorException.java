package com.dnastack.search.sheets.shared;

/**
 * Thrown when an unexpected situation occurs due to a fault on the server side.
 */
public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
}
