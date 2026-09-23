package com.expensetracker.controller;

import com.expensetracker.entity.Expense;
import com.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // =========================
    // CREATE EXPENSE
    // =========================
    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody Expense expense) {

        return ResponseEntity.ok(
                expenseService.saveExpense(expense)
        );
    }

    // =========================
    // GET ALL EXPENSES
    // =========================
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {

        return ResponseEntity.ok(
                expenseService.getAllExpenses()
        );
    }

    // =========================
    // GET EXPENSE BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                expenseService.getExpenseById(id)
        );
    }

    // =========================
    // UPDATE EXPENSE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Integer id,
            @Valid @RequestBody Expense expense) {

        return ResponseEntity.ok(
                expenseService.updateExpense(id, expense)
        );
    }

    // =========================
    // DELETE EXPENSE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpense(
            @PathVariable Integer id) {

        expenseService.deleteExpense(id);

        return ResponseEntity.ok(
                "Expense deleted successfully"
        );
    }

    // =========================
    // CATEGORY SUMMARY
    // =========================
    @GetMapping("/category-summary")
    public ResponseEntity<Map<String, Double>> getCategorySummary() {

        return ResponseEntity.ok(
                expenseService.getCategorySummary()
        );
    }

    // =========================
    // MONTHLY SUMMARY
    // =========================
    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Double>> getMonthlySummary() {

        return ResponseEntity.ok(
                expenseService.getMonthlySummary()
        );
    }

    // =========================
    // CURRENT MONTH SPENDING
    // =========================
    @GetMapping("/current-month-spending")
    public ResponseEntity<Double> getCurrentMonthSpending() {

        return ResponseEntity.ok(
                expenseService.getCurrentMonthSpending()
        );
    }

    // =========================
    // SMART SAVING
    // =========================
    @GetMapping("/saving-suggestions")
    public ResponseEntity<String> getSavingSuggestions() {

        return ResponseEntity.ok(
                expenseService.getSavingSuggestions()
        );
    }

    // =========================
    // UNUSUAL SPENDING
    // =========================
    @GetMapping("/{id}/unusual")
    public ResponseEntity<String> detectUnusualSpending(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                expenseService.detectUnusualSpending(id)
        );
    }
}