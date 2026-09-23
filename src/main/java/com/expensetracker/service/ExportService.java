package com.expensetracker.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;

@Service
public class ExportService {

    private final ExpenseRepository expenseRepository;

    public ExportService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // ==============================
    // EXPORT EXPENSES TO CSV
    // ==============================
    public byte[] exportExpensesToCSV() {

        List<Expense> expenses = expenseRepository.findAll();

        StringBuilder csv = new StringBuilder();

        // CSV Header
        csv.append("ID,Title,Amount,Category,Expense Date\n");

        // CSV Data
        for (Expense expense : expenses) {

            csv.append(expense.getId())
                    .append(",")
                    .append(escapeCsv(expense.getTitle()))
                    .append(",")
                    .append(expense.getAmount())
                    .append(",")
                    .append(escapeCsv(expense.getCategory()))
                    .append(",")
                    .append(expense.getExpenseDate())
                    .append("\n");
        }

        return csv.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    // Handle commas and quotes in text
    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")
                || value.contains("\n")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}