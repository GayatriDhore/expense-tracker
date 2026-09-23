package com.expensetracker.controller;

import com.expensetracker.entity.Expense;
import com.expensetracker.service.ExpenseService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.Cell;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
public class PDFExportController {

    private final ExpenseService expenseService;

    public PDFExportController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF() {

        try {
            List<Expense> expenses = expenseService.getAllExpenses();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Title
            Paragraph title =
                    new Paragraph("EXPENSE TRACKER");

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            // Subtitle
            Paragraph subtitle =
                    new Paragraph("Expense Report");

            subtitle.setAlignment(Element.ALIGN_CENTER);

            document.add(subtitle);

            document.add(new Paragraph(" "));

            // Table
            Table table = new Table(5);

            table.addCell(new Cell("ID"));
            table.addCell(new Cell("Title"));
            table.addCell(new Cell("Amount"));
            table.addCell(new Cell("Category"));
            table.addCell(new Cell("Date"));

            double totalAmount = 0;

            for (Expense expense : expenses) {

                table.addCell(
                        new Cell(String.valueOf(expense.getId()))
                );

                table.addCell(
                        new Cell(expense.getTitle())
                );

                table.addCell(
                        new Cell("₹ " + expense.getAmount())
                );

                table.addCell(
                        new Cell(expense.getCategory())
                );

                table.addCell(
                        new Cell(
                                String.valueOf(
                                        expense.getExpenseDate()
                                )
                        )
                );

                totalAmount += expense.getAmount();
            }

            document.add(table);

            document.add(new Paragraph(" "));

            // Total
            Paragraph total =
                    new Paragraph(
                            "Total Expenses: ₹ " + totalAmount
                    );

            document.add(total);

            document.close();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=expenses.pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }
}