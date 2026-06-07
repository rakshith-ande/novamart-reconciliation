package com.novamart.reconciliation.controller;

import com.novamart.reconciliation.config.ReconciliationConfig;
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
    private final ReconciliationConfig config;

    public ReconciliationController(ReconciliationService reconciliationService, ReconciliationConfig config) {
        this.reconciliationService = reconciliationService;
        this.config = config;
    }

    @GetMapping("/run-local-test/{processor}")
    public ResponseEntity<ReconciliationReport> runLocalTest(@PathVariable String processor) {
        // Look up the filename based on the processor key
        String fileName = config.getTestFiles().get(processor.toUpperCase());

        if (fileName == null) {
            return ResponseEntity.badRequest().build();
        }

        try (InputStream internalStream = new ClassPathResource("test-data/internal_transactions.json").getInputStream();
             InputStream externalStream = new ClassPathResource(fileName).getInputStream()) {

            ReconciliationReport report = reconciliationService.reconcile(internalStream, externalStream, processor);
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}