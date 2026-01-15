package com.core.bank.infrastructure.controller;

import com.core.bank.api.ReportesApi;
import com.core.bank.application.service.ReportService;
import com.core.bank.model.dto.GetReporte200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReportsController implements ReportesApi {

    private final ReportService reportService;

    @Override
    public ResponseEntity<GetReporte200Response> getReporte(UUID clienteId, LocalDate from, LocalDate to, String format) {
        GetReporte200Response report = reportService.generateReport(clienteId, from, to, format);
        return ResponseEntity.ok(report);

    }
}
