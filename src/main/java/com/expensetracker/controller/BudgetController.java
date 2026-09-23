package com.expensetracker.controller;

import com.expensetracker.entity.Budget;
import com.expensetracker.service.BudgetService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // =========================
    // SET MONTHLY BUDGET
    // =========================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Budget> setBudget(
            @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "Budget amount must be greater than 0"
            );
        }

        return ResponseEntity.ok(
                budgetService.saveBudget(amount)
        );
    }

    // =========================
    // GET BUDGET STATUS
    // =========================
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBudgetStatus() {

        return ResponseEntity.ok(
                budgetService.getBudgetStatus()
        );
    }
}