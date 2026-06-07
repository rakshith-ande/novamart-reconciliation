package com.novamart.reconciliation.exception;

public class VendorNotSupportedException extends RuntimeException {
    public VendorNotSupportedException(String message) {
        super(message);
    }
}