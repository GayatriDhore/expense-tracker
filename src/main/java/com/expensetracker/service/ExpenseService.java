package com.expensetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // ==============================
    // CREATE EXPENSE
    // ==============================
    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    // ==============================
    // GET ALL EXPENSES
    // ==============================
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    // ==============================
    // GET EXPENSE BY ID
    // ==============================
    public Expense getExpenseById(Integer id) {

        return expenseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Expense not found with id: " + id
                        )
                );
    }

    // ==============================
    // UPDATE EXPENSE
    // ==============================
    public Expense updateExpense(
            Integer id,
            Expense expense) {

        Expense existingExpense = getExpenseById(id);

        existingExpense.setTitle(expense.getTitle());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setExpenseDate(expense.getExpenseDate());

        return expenseRepository.save(existingExpense);
    }

    // ==============================
    // DELETE EXPENSE
    // ==============================
    public void deleteExpense(Integer id) {

        Expense expense = getExpenseById(id);

        expenseRepository.delete(expense);
    }

    // ==============================
    // CATEGORY-WISE SUMMARY
    // ==============================
    public Map<String, Double> getCategorySummary() {

        List<Expense> expenses = expenseRepository.findAll();

        Map<String, Double> summary = new LinkedHashMap<>();

        for (Expense expense : expenses) {

            String category = expense.getCategory();
            double amount = expense.getAmount();

            summary.put(
                    category,
                    summary.getOrDefault(category, 0.0) + amount
            );
        }

        return summary;
    }

    // ==============================
    // MONTHLY SUMMARY
    // ==============================
    public Map<String, Double> getMonthlySummary() {

        List<Expense> expenses = expenseRepository.findAll();

        Map<String, Double> summary = new LinkedHashMap<>();

        for (Expense expense : expenses) {

            String month =
                    expense.getExpenseDate().getYear()
                    + "-"
                    + String.format(
                            "%02d",
                            expense.getExpenseDate().getMonthValue()
                    );

            double amount = expense.getAmount();

            summary.put(
                    month,
                    summary.getOrDefault(month, 0.0) + amount
            );
        }

        return summary;
    }

    // ==============================
    // 🚨 UNUSUAL SPENDING DETECTION
    // ==============================
    public String detectUnusualSpending(Integer expenseId) {

        Expense expense = getExpenseById(expenseId);

        String category = expense.getCategory();
        double currentAmount = expense.getAmount();

        List<Expense> categoryExpenses =
                expenseRepository.findByCategory(category);

        if (categoryExpenses.size() <= 1) {

            return "Not enough data to detect unusual spending.";
        }

        double total = 0;

        for (Expense e : categoryExpenses) {

            if (!e.getId().equals(expenseId)) {
                total += e.getAmount();
            }
        }

        double average =
                total / (categoryExpenses.size() - 1);

        if (currentAmount > average * 2) {

            return "🚨 Unusual Spending Detected!\n"
                    + "Category: " + category + "\n"
                    + "Amount: ₹" + currentAmount + "\n"
                    + "Your average spending in this category is around ₹"
                    + String.format("%.2f", average)
                    + ".\n"
                    + "This expense is significantly higher than your normal spending.";
        }

        return "✅ Normal Spending\n"
                + "Category: " + category + "\n"
                + "Amount: ₹" + currentAmount + "\n"
                + "Average spending: ₹"
                + String.format("%.2f", average);
    }

    // ==============================
    // 💡 SMART SAVING SUGGESTIONS
    // ==============================
    public String getSavingSuggestions() {

        List<Expense> expenses = expenseRepository.findAll();

        if (expenses.isEmpty()) {

            return "No expenses found. "
                    + "Start adding expenses to get saving suggestions.";
        }

        Map<String, Double> categoryTotals =
                new LinkedHashMap<>();

        double totalSpending = 0;

        for (Expense expense : expenses) {

            String category = expense.getCategory();
            double amount = expense.getAmount();

            totalSpending += amount;

            categoryTotals.put(
                    category,
                    categoryTotals.getOrDefault(category, 0.0)
                            + amount
            );
        }

        String highestCategory = "";
        double highestAmount = 0;

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > highestAmount) {

                highestAmount = entry.getValue();
                highestCategory = entry.getKey();
            }
        }

        double percentage =
                (highestAmount / totalSpending) * 100;

        return "💡 Smart Saving Suggestions\n"
                + "Total Spending: ₹"
                + String.format("%.2f", totalSpending)
                + "\n"
                + "Highest Spending Category: "
                + highestCategory
                + "\n"
                + "Amount Spent: ₹"
                + String.format("%.2f", highestAmount)
                + "\n"
                + "This category represents "
                + String.format("%.2f", percentage)
                + "% of your total spending.\n"
                + "Suggestion: Try reducing your spending in "
                + highestCategory
                + " by 10% to improve your savings.";
    }

    // ==========================================================
    // 💰 MONTHLY SPENDING
    // ==========================================================
    public double getCurrentMonthSpending() {

        YearMonth currentMonth =
                YearMonth.now();

        List<Expense> expenses =
                expenseRepository.findAll();

        double total = 0;

        for (Expense expense : expenses) {

            if (expense.getExpenseDate() != null
                    && YearMonth.from(
                            expense.getExpenseDate()
                    ).equals(currentMonth)) {

                total += expense.getAmount();
            }
        }

        return total;
    }

    // ==========================================================
    // 💰 BUDGET ANALYSIS
    // ==========================================================
    public Map<String, Object> getBudgetAnalysis(double budget) {

        if (budget <= 0) {

            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }

        double spending =
                getCurrentMonthSpending();

        double remaining =
                budget - spending;

        double percentage =
                (spending / budget) * 100;

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("monthlyBudget", round(budget));
        result.put("currentMonthSpending", round(spending));
        result.put("remainingBudget", round(remaining));
        result.put("budgetUsedPercentage",
                round(percentage));

        if (percentage >= 100) {

            result.put(
                    "status",
                    "OVER_BUDGET"
            );

            result.put(
                    "message",
                    "⚠️ You have exceeded your monthly budget."
            );

        } else if (percentage >= 80) {

            result.put(
                    "status",
                    "WARNING"
            );

            result.put(
                    "message",
                    "⚠️ You have used more than 80% of your monthly budget."
            );

        } else {

            result.put(
                    "status",
                    "ON_TRACK"
            );

            result.put(
                    "message",
                    "✅ Your spending is currently within budget."
            );
        }

        return result;
    }

    // ==========================================================
    // ❤️ FINANCIAL HEALTH SCORE
    // ==========================================================
    public Map<String, Object> getFinancialHealth(double budget) {

        if (budget <= 0) {

            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }

        double spending =
                getCurrentMonthSpending();

        double percentage =
                (spending / budget) * 100;

        int score;

        if (percentage <= 50) {

            score = 90;

        } else if (percentage <= 70) {

            score = 75;

        } else if (percentage <= 85) {

            score = 60;

        } else if (percentage <= 100) {

            score = 45;

        } else {

            score = 25;
        }

        String status;

        if (score >= 80) {

            status = "Excellent";

        } else if (score >= 60) {

            status = "Good";

        } else if (score >= 40) {

            status = "Needs Attention";

        } else {

            status = "High Spending Risk";
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "financialHealthScore",
                score
        );

        result.put(
                "status",
                status
        );

        result.put(
                "monthlyBudget",
                round(budget)
        );

        result.put(
                "monthlySpending",
                round(spending)
        );

        result.put(
                "budgetUsedPercentage",
                round(percentage)
        );

        result.put(
                "generatedAt",
                LocalDate.now().toString()
        );

        return result;
    }

    // ==========================================================
    // ROUND DECIMAL
    // ==========================================================
    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}