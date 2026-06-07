package com.novamart.reconciliation.parser;

import com.novamart.reconciliation.model.ExternalSettlement;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdyenParser implements SettlementParser {
    @Override
    public boolean supports(String processorName) {
        return "ADYEN".equalsIgnoreCase(processorName);
    }

    @Override
    public List<ExternalSettlement> parse(InputStream inputStream) throws Exception {
        List<ExternalSettlement> settlements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirst = true;
            while ((line = reader.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    settlements.add(new ExternalSettlement(
                        parts[0].trim(),
                        new BigDecimal(parts[1].trim()),
                        new BigDecimal(parts[2].trim()),
                        new BigDecimal(parts[3].trim()),
                        parts[4].trim(),
                        "ADYEN"
                    ));
                }
            }
        }
        return settlements;
    }
}