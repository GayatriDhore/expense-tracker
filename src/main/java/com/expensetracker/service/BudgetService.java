package com.expensetracker.service;

import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Expense;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository) {

        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public Budget saveBudget(double amount) {

        String month = YearMonth.now().toString();

        Budget budget =
                budgetRepository
                        .findByMonth(month)
                        .orElse(new Budget());

        budget.setAmount(amount);
        budget.setMonth(month);

        return budgetRepository.save(budget);
    }

    public Map<String, Object> getBudgetStatus() {

        String currentMonth =
                YearMonth.now().toString();

        double budgetAmount =
                budgetRepository
                        .findByMonth(currentMonth)
                        .map(Budget::getAmount)
                        .orElse(0.0);

        List<Expense> expenses =
                expenseRepository.findAll();

        double spent = 0;

        for (Expense expense : expenses) {

            if (expense.getExpenseDate() != null) {

                YearMonth expenseMonth =
                        YearMonth.from(
                                expense.getExpenseDate()
                        );

                if (expenseMonth.toString()
                        .equals(currentMonth)) {

                    spent += expense.getAmount();
                }
            }
        }

        double remaining =
                budgetAmount - spent;

        double percentage =
                budgetAmount > 0
                        ? (spent / budgetAmount) * 100
                        : 0;

        String status;

        if (budgetAmount == 0) {
            status = "Budget not set";
        }
        else if (percentage >= 100) {
            status = "Budget exceeded";
        }
        else if (percentage >= 80) {
            status = "Warning: 80%+ budget used";
        }
        else {
            status = "Within budget";
        }

        Map<String, Object> result =
                new HashMap<>();

        result.put("month", currentMonth);
        result.put("budget", budgetAmount);
        result.put("spent", spent);
        result.put("remaining", remaining);
        result.put("percentageUsed", percentage);
        result.put("status", status);

        return result;
    }
}