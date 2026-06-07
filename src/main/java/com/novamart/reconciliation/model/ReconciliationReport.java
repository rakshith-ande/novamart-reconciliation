package com.novamart.reconciliation.model;
import java.util.List;
public record ReconciliationReport(int totalProcessed, int totalMatched, int totalDiscrepancies, List<Discrepancy> discrepancies) {}