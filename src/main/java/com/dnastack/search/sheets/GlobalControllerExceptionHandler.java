package com.dnastack.search.sheets;

import com.dnastack.search.sheets.shared.BadRequestException;
import com.dnastack.search.sheets.shared.NotFoundException;
import com.dnastack.search.sheets.shared.SearchErrorResponse;
import com.dnastack.search.sheets.shared.ServerErrorException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SearchErrorResponse> handle(NotFoundException ex) {
        log.info("Returning 404", ex);
        return ResponseEntity.status(404).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<SearchErrorResponse> handle(BadRequestException ex) {
        log.info("Returning 400", ex);
        return ResponseEntity.status(400).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<SearchErrorResponse> handle(ServerErrorException ex) {
        log.info("Returning 500", ex);
        return ResponseEntity.status(500).body(new SearchErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(GoogleJsonResponseException.class)
    public ResponseEntity<SearchErrorResponse> handle(GoogleJsonResponseException ex) {
        log.info("Returning " + ex.getStatusCode(), ex);
        if (ex.getStatusCode() == 401) {
            return ResponseEntity.status(ex.getStatusCode())
                .header("www-authenticate", ex.getHeaders().getAuthenticate())
                .body(new SearchErrorResponse(ex.getDetails().getMessage()));
        }
        return ResponseEntity.status(ex.getStatusCode()).body(new SearchErrorResponse(ex.getDetails().getMessage()));
    }

}
