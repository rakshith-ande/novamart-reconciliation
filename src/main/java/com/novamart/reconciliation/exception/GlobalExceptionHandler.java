package com.novamart.reconciliation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VendorNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleVendorNotFound(VendorNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Vendor Configuration Missing", "message", ex.getMessage()));
    }
}