package com.novamart.reconciliation.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamart.reconciliation.model.ExternalSettlement;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class StripeParser implements SettlementParser {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(String processorName) {
        return "STRIPE".equalsIgnoreCase(processorName);
    }

    @Override
    public List<ExternalSettlement> parse(InputStream inputStream) throws Exception {
        List<ExternalSettlement> settlements = new ArrayList<>();
        JsonNode root = mapper.readTree(inputStream);
        
        if (root.isArray()) {
            for (JsonNode node : root) {
                settlements.add(new ExternalSettlement(
                    node.get("id").asText(),
                    new BigDecimal(node.get("amount_gross").asText()),
                    new BigDecimal(node.get("amount_fee").asText()),
                    new BigDecimal(node.get("amount_net").asText()),
                    node.get("currency").asText(),
                    "STRIPE"
                ));
            }
        }
        return settlements;
    }
}