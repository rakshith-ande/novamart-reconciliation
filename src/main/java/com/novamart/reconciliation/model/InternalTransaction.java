package com.novamart.reconciliation.model;
import java.math.BigDecimal;
public record InternalTransaction(
        String transactionId,
        String orderId,
        BigDecimal amount,
        String currency,
        String processor,
        String status
) {}