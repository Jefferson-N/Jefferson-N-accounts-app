package com.core.bank.application.strategy.reports;

import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PdfReportStrategy implements ReportGenerationStrategy {

    private final JsonReportStrategy jsonReportStrategy;

    @Override
    public GetReporte200Response generateReport(UUID clienteId, LocalDate from, LocalDate to) {
        ReportJson reportJson = (ReportJson) jsonReportStrategy.generateReport(clienteId, from, to);
        
        String base64Pdf = generatePdfBase64(reportJson);
        
        return new ReportPdf().base64(base64Pdf);
    }

    private String generatePdfBase64(ReportJson reportJson) {
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("=== ESTADO DE CUENTA ===\n\n");
        
        if (reportJson.getCustomer() != null) {
            pdfContent.append("Cliente: ").append(reportJson.getCustomer().getName()).append("\n");
            pdfContent.append("ID: ").append(reportJson.getCustomer().getId()).append("\n\n");
        }
        
        if (reportJson.getRange() != null) {
            pdfContent.append("Periodo: ").append(reportJson.getRange().getFrom())
                    .append(" a ").append(reportJson.getRange().getTo()).append("\n\n");
        }
        if (reportJson.getAccounts() != null) {
            pdfContent.append("CUENTAS:\n");
            pdfContent.append("-".repeat(80)).append("\n");
            
            for (ReportJsonAccountsInner account : reportJson.getAccounts()) {
                pdfContent.append("\nCuenta: ").append(account.getAccountNumber()).append("\n");
                pdfContent.append("Tipo: ").append(account.getAccountType()).append("\n");
                pdfContent.append("Saldo Inicial: ").append(account.getInitialBalance()).append("\n");
                pdfContent.append("Estado: ").append(account.getStatus() ? "Activa" : "Inactiva").append("\n\n");
                
                if (account.getTransactions() != null && !account.getTransactions().isEmpty()) {
                    pdfContent.append("Transacciones:\n");
                    for (ReportJsonAccountsInnerTransactionsInner txn : account.getTransactions()) {
                        pdfContent.append("  ").append(txn.getDate()).append(" | ")
                                .append(txn.getTransactionType()).append(" | ")
                                .append(txn.getAmount()).append(" | ")
                                .append("Saldo: ").append(txn.getBalance()).append("\n");
                    }
                } else {
                    pdfContent.append("Sin transacciones en el período.\n");
                }
                
                if (account.getTotals() != null) {
                    pdfContent.append("\nTotales:\n");
                    pdfContent.append("  Débitos: ").append(account.getTotals().getDebits()).append("\n");
                    pdfContent.append("  Créditos: ").append(account.getTotals().getCredits()).append("\n");
                }
                pdfContent.append("-".repeat(80)).append("\n");
            }
        }
        
        return Base64.getEncoder().encodeToString(pdfContent.toString().getBytes());
    }
}
