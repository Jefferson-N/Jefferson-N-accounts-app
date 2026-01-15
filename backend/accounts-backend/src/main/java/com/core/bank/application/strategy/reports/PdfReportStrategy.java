package com.core.bank.application.strategy.reports;

import com.core.bank.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PdfReportStrategy implements ReportGenerationStrategy {

    private final JsonReportStrategy jsonReportStrategy;
    
    private static final Color YELLOW_HEADER = new Color(255, 204, 0);      // #ffcc00
    private static final Color YELLOW_BORDER = new Color(255, 184, 0);      // #ffb800
    private static final Color TEXT_DARK = new Color(51, 51, 51);           // #333
    private static final Color TEXT_LIGHT = new Color(85, 85, 85);          // #555
    private static final Color BORDER_LIGHT = new Color(238, 238, 238);     // #eee
    private static final Color BG_HOVER = new Color(245, 245, 245);         // #f5f5f5

    @Override
    public GetReporte200Response generateReport(UUID clienteId, LocalDate from, LocalDate to) {
        ReportJson reportJson = (ReportJson) jsonReportStrategy.generateReport(clienteId, from, to);
        
        String base64Pdf = generatePdfBase64(reportJson);
        
        return new ReportPdf().base64(base64Pdf);
    }

    private String generatePdfBase64(ReportJson reportJson) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            float margin = 40;
            float yPosition = 750;
            float pageWidth = PDRectangle.LETTER.getWidth() - (2 * margin);
            
            // Encabezado con título
            yPosition = drawHeader(contentStream, yPosition, margin, pageWidth);
            yPosition -= 20;
            
            // Información del cliente y período
            yPosition = drawClientInfo(contentStream, reportJson, yPosition, margin, pageWidth);
            yPosition -= 15;
            
            // Totales generales del reporte
            if (reportJson.getAccounts() != null && !reportJson.getAccounts().isEmpty()) {
                yPosition = drawTotalsSummary(contentStream, reportJson.getAccounts(), yPosition, margin, pageWidth);
                yPosition -= 20;
            }
            
            // Detalle de cuentas
            if (reportJson.getAccounts() != null && !reportJson.getAccounts().isEmpty()) {
                yPosition = drawAccountsSection(contentStream, reportJson.getAccounts(), yPosition, margin, pageWidth);
            }
            
            contentStream.close();
            
            // Convertir PDF a base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();
            
            return Base64.getEncoder().encodeToString(pdfBytes);
            
        } catch (IOException e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private float drawHeader(PDPageContentStream contentStream, float yPosition, float margin, float width) 
            throws IOException {
        // Fondo amarillo del encabezado
        contentStream.setNonStrokingColor(YELLOW_HEADER);
        contentStream.addRect(margin, yPosition - 40, width, 40);
        contentStream.fill();
        
        // Borde inferior
        contentStream.setStrokingColor(YELLOW_BORDER);
        contentStream.setLineWidth(2);
        contentStream.moveTo(margin, yPosition - 40);
        contentStream.lineTo(margin + width, yPosition - 40);
        contentStream.stroke();
        
        // Título
        contentStream.setNonStrokingColor(TEXT_DARK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 10, yPosition - 28);
        contentStream.showText("ESTADO DE CUENTA");
        contentStream.endText();
        
        return yPosition - 50;
    }

    private float drawClientInfo(PDPageContentStream contentStream, ReportJson reportJson, 
            float yPosition, float margin, float width) throws IOException {
        contentStream.setNonStrokingColor(TEXT_DARK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        
        // Cliente
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        if (reportJson.getCustomer() != null) {
            contentStream.showText("Cliente: " + reportJson.getCustomer().getName());
        }
        contentStream.endText();
        yPosition -= 15;
        
        // Período
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        if (reportJson.getRange() != null) {
            contentStream.showText("Período: " + reportJson.getRange().getFrom() + " al " + 
                                 reportJson.getRange().getTo());
        }
        contentStream.endText();
        
        return yPosition - 15;
    }

    private float drawTotalsSummary(PDPageContentStream contentStream, 
            java.util.List<ReportJsonAccountsInner> accounts, float yPosition, float margin, float width) 
            throws IOException {
        
        // Calcular totales globales
        double totalDebits = 0;
        double totalCredits = 0;
        
        for (ReportJsonAccountsInner account : accounts) {
            if (account.getTotals() != null) {
                totalDebits += account.getTotals().getDebits().doubleValue();
                totalCredits += account.getTotals().getCredits().doubleValue();
            }
        }
        
        // Título
        contentStream.setNonStrokingColor(TEXT_DARK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("TOTALES DEL PERÍODO");
        contentStream.endText();
        yPosition -= 15;
        
        // Totales
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.setNonStrokingColor(TEXT_LIGHT);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Débitos: " + totalDebits + "           Créditos: " + totalCredits);
        contentStream.endText();
        
        return yPosition - 15;
    }

    private float drawAccountsSection(PDPageContentStream contentStream, 
            java.util.List<ReportJsonAccountsInner> accounts, float yPosition, float margin, float width) 
            throws IOException {
        
        // Encabezado de detalle
        contentStream.setNonStrokingColor(TEXT_DARK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("DETALLE DE CUENTAS");
        contentStream.endText();
        yPosition -= 15;
        
        for (ReportJsonAccountsInner account : accounts) {
            // Título de la cuenta
            contentStream.setNonStrokingColor(TEXT_DARK);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Cuenta: " + account.getAccountNumber() + " (" + account.getAccountType() + ")");
            contentStream.endText();
            yPosition -= 15;
            
            // Info de la cuenta
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.setNonStrokingColor(TEXT_LIGHT);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Saldo Inicial: " + account.getInitialBalance() + " | " +
                                 "Estado: " + (account.getStatus() ? "Activa" : "Inactiva"));
            contentStream.endText();
            yPosition -= 12;
            
            // Tabla de transacciones
            if (account.getTransactions() != null && !account.getTransactions().isEmpty()) {
                yPosition = drawTransactionsTable(contentStream, account.getTransactions(), 
                                                yPosition, margin, width);
            } else {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Sin transacciones en el período");
                contentStream.endText();
                yPosition -= 12;
            }
            
            yPosition -= 10;
        }
        
        return yPosition;
    }

    private float drawTransactionsTable(PDPageContentStream contentStream, 
            java.util.List<ReportJsonAccountsInnerTransactionsInner> transactions, 
            float yPosition, float margin, float width) throws IOException {
        
        float colWidth = width / 4;
        float rowHeight = 12;
        
        yPosition -= 15;
        contentStream.setNonStrokingColor(YELLOW_HEADER);
        contentStream.addRect(margin, yPosition - rowHeight, width, rowHeight);
        contentStream.fill();
        
        // Borde de la tabla
        contentStream.setStrokingColor(YELLOW_BORDER);
        contentStream.setLineWidth(1);
        contentStream.addRect(margin, yPosition - rowHeight, width, rowHeight);
        contentStream.stroke();
        
        // Texto de encabezados
        contentStream.setNonStrokingColor(TEXT_DARK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
        
        String[] headers = {"Fecha", "Tipo", "Monto", "Saldo"};
        float xPos = margin + 5;
        contentStream.beginText();
        for (String header : headers) {
            contentStream.newLineAtOffset(xPos, yPosition - 10);
            contentStream.showText(header);
            xPos += colWidth;
        }
        contentStream.endText();
        
        yPosition -= rowHeight;
        
        // Filas de transacciones
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.setNonStrokingColor(TEXT_LIGHT);
        
        for (ReportJsonAccountsInnerTransactionsInner txn : transactions) {
            // Borde de fila
            contentStream.setStrokingColor(BORDER_LIGHT);
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(margin, yPosition - rowHeight, width, rowHeight);
            contentStream.stroke();
            
            // Datos de la transacción
            xPos = margin + 5;
            String[] data = {
                String.valueOf(txn.getDate()),
                String.valueOf(txn.getTransactionType()),
                String.valueOf(txn.getAmount()),
                String.valueOf(txn.getBalance())
            };
            
            contentStream.beginText();
            for (String value : data) {
                contentStream.newLineAtOffset(xPos, yPosition - 9);
                contentStream.showText(truncateText(value, 12));
                xPos += colWidth;
            }
            contentStream.endText();
            
            yPosition -= rowHeight;
        }
        
        yPosition -= 10;  
        return yPosition;
    }
    
    private String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 2) + "..";
        }
        return text;
    }
}
