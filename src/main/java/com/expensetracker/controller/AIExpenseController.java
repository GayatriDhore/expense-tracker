package com.expensetracker.controller;

import com.expensetracker.service.AIExpenseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AIExpenseController {

    private final AIExpenseService aiExpenseService;

    public AIExpenseController(AIExpenseService aiExpenseService) {
        this.aiExpenseService = aiExpenseService;
    }

    // =========================================================
    // 1. INDIVIDUAL EXPENSE AI ANALYSIS
    // =========================================================

    @PostMapping("/analyze-expense")
    public ResponseEntity<?> analyzeExpense(
            @RequestBody Map<String, String> request) {

        String expenseText = request.get("expense");

        if (expenseText == null || expenseText.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Expense description cannot be empty"
                    ));
        }

        Map<String, String> result =
                aiExpenseService.analyzeExpense(expenseText);

        return ResponseEntity.ok(
                Map.of(
                        "expense", expenseText,
                        "aiAnalysis", result
                )
        );
    }


    // =========================================================
    // 2. AI MONTHLY FINANCIAL REPORT
    // =========================================================

    @GetMapping("/monthly-report")
    public ResponseEntity<?> monthlyReport(
            @RequestParam String month) {

        if (month == null || month.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Month cannot be empty"
                    ));
        }

        Map<String, String> result =
                aiExpenseService.generateMonthlyReport(month);

        return ResponseEntity.ok(result);
    }
}