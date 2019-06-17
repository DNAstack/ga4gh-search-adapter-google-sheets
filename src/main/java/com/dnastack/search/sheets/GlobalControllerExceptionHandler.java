package com.dnastack.search.sheets;

import com.dnastack.search.sheets.shared.BadRequestException;
import com.dnastack.search.sheets.shared.NotFoundException;
import com.dnastack.search.sheets.shared.SearchErrorResponse;
import com.dnastack.search.sheets.shared.ServerErrorException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SearchErrorResponse> handle(NotFoundException ex) {
        return ResponseEntity.status(404).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<SearchErrorResponse> handle(BadRequestException ex) {
        return ResponseEntity.status(400).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<SearchErrorResponse> handle(ServerErrorException ex) {
        return ResponseEntity.status(500).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(GoogleJsonResponseException.class)
    public ResponseEntity<SearchErrorResponse> handle(GoogleJsonResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new SearchErrorResponse(ex.getDetails().getMessage()));
    }

}
