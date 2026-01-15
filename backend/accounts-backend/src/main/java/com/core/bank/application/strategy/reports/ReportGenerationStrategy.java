package com.core.bank.application.strategy.reports;

import com.core.bank.model.dto.GetReporte200Response;
import java.time.LocalDate;
import java.util.UUID;


public interface ReportGenerationStrategy {
    
    GetReporte200Response generateReport(UUID clienteId, LocalDate from, LocalDate to);
}
