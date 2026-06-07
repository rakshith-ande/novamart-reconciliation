package com.novamart.reconciliation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamart.reconciliation.model.*;
import com.novamart.reconciliation.parser.SettlementParser;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReconciliationService {

    private final List<SettlementParser> parsers;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReconciliationService(List<SettlementParser> parsers) {
        this.parsers = parsers;
    }

    public ReconciliationReport reconcile(InputStream internalStream, InputStream externalStream, String processor) throws Exception {

        List<InternalTransaction> allInternal = mapper.readValue(internalStream, new TypeReference<>() {
        });
        List<InternalTransaction> internalRecords = allInternal.stream()
                .filter(t -> t.processor().equalsIgnoreCase(processor) && "CAPTURED".equalsIgnoreCase(t.status()))
                .toList();

        SettlementParser parser = parsers.stream()
                .filter(p -> p.supports(processor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Processor: " + processor));

        List<ExternalSettlement> externalRecords = parser.parse(externalStream);

        Map<String, List<ExternalSettlement>> externalMap = externalRecords.stream()
                .collect(Collectors.groupingBy(ExternalSettlement::transactionId));

        List<Discrepancy> discrepancies = new ArrayList<>();
        Set<String> matchedExternalIds = new HashSet<>();
        int matchedCount = 0;

        for (InternalTransaction internal : internalRecords) {
            String txnId = internal.transactionId();
            List<ExternalSettlement> settlements = externalMap.get(txnId);

            if (settlements == null || settlements.isEmpty()) {
                // --- CONFIDENCE SCORING LOGIC ---
                // 1. Attempt Fuzzy Match: Look for ANY record with the same amount
                Optional<ExternalSettlement> fuzzyMatch = externalRecords.stream()
                        .filter(e -> e.grossAmount().compareTo(internal.amount()) == 0)
                        .findFirst();

                if (fuzzyMatch.isPresent()) {
                    // Found a record with the same amount but different ID (Confidence: 0.7)
                    discrepancies.add(new Discrepancy(txnId, "FUZZY_MATCH_POSSIBLE", processor,
                            internal.amount(), fuzzyMatch.get().grossAmount(),
                            "ID not found, but amount matches record: " + fuzzyMatch.get().transactionId(), 0.7));
                } else {
                    // No ID match and no amount match (Confidence: 0.0)
                    discrepancies.add(new Discrepancy(txnId, "MISSING", processor,
                            internal.amount(), BigDecimal.ZERO, "Captured internally but missing in settlement.", 0.0));
                }
            } else {
                // --- EXACT MATCH (Confidence: 1.0) ---
                matchedExternalIds.add(txnId);
                if (settlements.size() > 1) {
                    discrepancies.add(new Discrepancy(txnId, "DUPLICATE", processor, internal.amount(), settlements.get(0).grossAmount(), "Appears " + settlements.size() + " times in settlement.", 1.0));
                } else {
                    ExternalSettlement ext = settlements.get(0);
                    if (internal.amount().compareTo(ext.grossAmount()) != 0) {
                        discrepancies.add(new Discrepancy(txnId, "AMOUNT_MISMATCH", processor, internal.amount(), ext.grossAmount(), "Internal amount differs from External Gross.", 1.0));
                    } else {
                        matchedCount++;
                    }
                }
            }
        }

        for (ExternalSettlement ext : externalRecords) {
            if (!matchedExternalIds.contains(ext.transactionId())) {
                discrepancies.add(new Discrepancy(ext.transactionId(), "PHANTOM", processor, BigDecimal.ZERO, ext.grossAmount(), "Exists in settlement but missing from internal DB.", 1.0));
            }
        }

        return new ReconciliationReport(internalRecords.size(), matchedCount, discrepancies.size(), discrepancies);
    }
}