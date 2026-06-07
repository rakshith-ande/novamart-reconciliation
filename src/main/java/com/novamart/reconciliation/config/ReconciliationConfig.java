package com.novamart.reconciliation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.reconciliation")
public class ReconciliationConfig {

    private Map<String, String> testFiles;

    public Map<String, String> getTestFiles() {
        return testFiles;
    }

    public void setTestFiles(Map<String, String> testFiles) {
        this.testFiles = testFiles;
    }
}