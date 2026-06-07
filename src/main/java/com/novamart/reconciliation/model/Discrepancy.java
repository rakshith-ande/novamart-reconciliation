package com.novamart.reconciliation.model;
import java.math.BigDecimal;
public record Discrepancy(
        String transactionId,
        String type,
        String processor,
        BigDecimal internalAmount,
        BigDecimal externalAmount,
        String message,
        double confidenceScore
) {}