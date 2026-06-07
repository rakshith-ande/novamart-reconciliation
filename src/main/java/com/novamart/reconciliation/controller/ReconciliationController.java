package com.novamart.reconciliation.controller;

import com.novamart.reconciliation.model.ReconciliationReport;
import com.novamart.reconciliation.service.ReconciliationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/run-local-test/{processor}")
    public ResponseEntity<ReconciliationReport> runLocalTest(@PathVariable String processor) {
        try {
            InputStream internalStream = new ClassPathResource("test-data/internal_transactions.json").getInputStream();
            
            String extFileName = processor.equalsIgnoreCase("STRIPE") 
                ? "test-data/stripe_settlement.json" 
                : "test-data/adyen_settlement.csv";
                
            InputStream externalStream = new ClassPathResource(extFileName).getInputStream();

            ReconciliationReport report = reconciliationService.reconcile(internalStream, externalStream, processor);
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}