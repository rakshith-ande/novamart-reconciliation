package com.novamart.reconciliation.model;
import java.math.BigDecimal;
public record ExternalSettlement(
        String transactionId,
        BigDecimal grossAmount,
        BigDecimal fee,
        BigDecimal netAmount,
        String currency,
        String processor
) {}