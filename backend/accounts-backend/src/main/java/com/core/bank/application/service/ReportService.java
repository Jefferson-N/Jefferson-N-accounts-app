package com.core.bank.application.service;

import com.core.bank.infrastructure.exception.BusinessRuleException;
import com.core.bank.application.strategy.reports.JsonReportStrategy;
import com.core.bank.application.strategy.reports.PdfReportStrategy;
import com.core.bank.application.strategy.reports.ReportGenerationStrategy;
import com.core.bank.model.dto.GetReporte200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReportService {

    private final JsonReportStrategy jsonReportStrategy;
    private final PdfReportStrategy pdfReportStrategy;

    public GetReporte200Response generateReport(UUID clienteId, LocalDate from, LocalDate to, String format) {
        ReportGenerationStrategy strategy = getStrategy(format);
        return strategy.generateReport(clienteId, from, to);
    }

    private ReportGenerationStrategy getStrategy(String format) {
        if ("json".equalsIgnoreCase(format)) {
            return jsonReportStrategy;
        } else if ("pdf".equalsIgnoreCase(format)) {
            return pdfReportStrategy;
        } else {
            throw new BusinessRuleException("Formato de reporte no válido. Use 'json' o 'pdf'");
        }
    }
}

