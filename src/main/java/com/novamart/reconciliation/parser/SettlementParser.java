package com.novamart.reconciliation.parser;

import com.novamart.reconciliation.model.ExternalSettlement;
import java.io.InputStream;
import java.util.List;

public interface SettlementParser {
    boolean supports(String processorName);
    List<ExternalSettlement> parse(InputStream inputStream) throws Exception;
}