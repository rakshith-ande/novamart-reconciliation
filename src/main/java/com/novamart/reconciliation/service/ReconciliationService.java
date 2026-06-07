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
        
        List<InternalTransaction> allInternal = mapper.readValue(internalStream, new TypeReference<>() {});
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
                discrepancies.add(new Discrepancy(txnId, "MISSING", processor, internal.amount(), BigDecimal.ZERO, "Captured internally but missing in settlement."));
            } else {
                matchedExternalIds.add(txnId);
                if (settlements.size() > 1) {
                    discrepancies.add(new Discrepancy(txnId, "DUPLICATE", processor, internal.amount(), settlements.get(0).grossAmount(), "Appears " + settlements.size() + " times in settlement."));
                } else {
                    ExternalSettlement ext = settlements.get(0);
                    if (internal.amount().compareTo(ext.grossAmount()) != 0) {
                        discrepancies.add(new Discrepancy(txnId, "AMOUNT_MISMATCH", processor, internal.amount(), ext.grossAmount(), "Internal amount differs from External Gross."));
                    } else {
                        matchedCount++;
                    }
                }
            }
        }

        for (ExternalSettlement ext : externalRecords) {
            if (!matchedExternalIds.contains(ext.transactionId())) {
                discrepancies.add(new Discrepancy(ext.transactionId(), "PHANTOM", processor, BigDecimal.ZERO, ext.grossAmount(), "Exists in settlement but missing from internal DB."));
            }
        }

        return new ReconciliationReport(internalRecords.size(), matchedCount, discrepancies.size(), discrepancies);
    }
}