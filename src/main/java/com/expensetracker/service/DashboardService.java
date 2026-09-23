package com.expensetracker.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;

    public DashboardService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Map<String, Object> getDashboardData() {

        List<Expense> expenses = expenseRepository.findAll();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        double totalSpending = 0.0;

        String highestCategory = "";
        double highestCategoryAmount = 0.0;

        Map<String, Double> categorySummary =
                new LinkedHashMap<>();

        Map<String, Double> monthlySummary =
                new LinkedHashMap<>();

        for (Expense expense : expenses) {

            double amount = expense.getAmount();

            totalSpending += amount;

            // Category summary
            String category = expense.getCategory();

            double categoryTotal =
                    categorySummary.getOrDefault(category, 0.0)
                    + amount;

            categorySummary.put(category, categoryTotal);

            // Highest spending category
            if (categoryTotal > highestCategoryAmount) {

                highestCategoryAmount = categoryTotal;
                highestCategory = category;
            }

            // Monthly summary
            String month =
                    expense.getExpenseDate().getYear()
                    + "-"
                    + String.format(
                            "%02d",
                            expense.getExpenseDate().getMonthValue()
                    );

            monthlySummary.put(
                    month,
                    monthlySummary.getOrDefault(month, 0.0)
                    + amount
            );
        }

        dashboard.put("totalExpenses", expenses.size());

        dashboard.put("totalSpending", totalSpending);

        dashboard.put(
                "highestSpendingCategory",
                highestCategory
        );

        dashboard.put(
                "highestCategoryAmount",
                highestCategoryAmount
        );

        dashboard.put(
                "categorySummary",
                categorySummary
        );

        dashboard.put(
                "monthlySummary",
                monthlySummary
        );

        return dashboard;
    }
}