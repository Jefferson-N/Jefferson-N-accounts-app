package com.core.bank.infrastructure.controller;

import com.core.bank.application.service.ReportService;
import com.core.bank.model.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ReportsController.class)
class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void testGetReportJsonFormat() throws Exception {
        UUID clienteId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2022, 2, 1);
        LocalDate to = LocalDate.of(2022, 2, 28);

        ReportJsonCustomer customer = new ReportJsonCustomer()
                .id(clienteId)
                .name("Marianela Montalvo");

        ReportJsonRange range = new ReportJsonRange()
                .from(from)
                .to(to);

        ReportJsonAccountsInnerTransactionsInner txn1 = new ReportJsonAccountsInnerTransactionsInner()
                .id("txn1")
                .date("2022-02-10")
                .transactionType("CREDITO")
                .amount(new BigDecimal("600.0"))
                .balance(new BigDecimal("1600.0"));

        ReportJsonAccountsInnerTotals totals = new ReportJsonAccountsInnerTotals()
                .debits(BigDecimal.ZERO)
                .credits(new BigDecimal("600.0"));

        ReportJsonAccountsInner account = new ReportJsonAccountsInner()
                .accountNumber("225487")
                .accountType("Corriente")
                .initialBalance(new BigDecimal("1000.0"))
                .status(true)
                .transactions(Arrays.asList(txn1))
                .totals(totals);

        ReportJson report = new ReportJson()
                .customer(customer)
                .range(range)
                .accounts(Arrays.asList(account));

        when(reportService.generateReport(any(), any(), any(), any())).thenReturn(report);

        mockMvc.perform(get("/api/reportes")
                .param("clienteId", clienteId.toString())
                .param("from", from.toString())
                .param("to", to.toString())
                .param("format", "json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customer.name").value("Marianela Montalvo"))
            .andExpect(jsonPath("$.accounts[0].accountNumber").value("225487"))
            .andExpect(jsonPath("$.accounts[0].totals.credits").value(600.0));
    }

    @Test
    void testGetReportPdfFormat() throws Exception {
        UUID clienteId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2022, 2, 1);
        LocalDate to = LocalDate.of(2022, 2, 28);

        ReportPdf reportPdf = new ReportPdf()
                .base64("base64encodedpdfcontent");

        when(reportService.generateReport(any(), any(), any(), any())).thenReturn(reportPdf);

        mockMvc.perform(get("/api/reportes")
                .param("clienteId", clienteId.toString())
                .param("from", from.toString())
                .param("to", to.toString())
                .param("format", "pdf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.base64").value("base64encodedpdfcontent"));
    }
}
