package com.expensetracker.service;

import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIExpenseService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ExpenseRepository expenseRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public AIExpenseService(ExpenseRepository expenseRepository) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.expenseRepository = expenseRepository;
    }

    // =========================================================
    // 1. INDIVIDUAL EXPENSE ANALYSIS
    // =========================================================

    public Map<String, String> analyzeExpense(String expenseText) {

        String prompt = """
                You are an AI assistant inside an Expense Tracker application.

                Analyze the following expense:

                "%s"

                Give the answer in this exact format:

                Category: <Food/Travel/Shopping/Bills/Entertainment/Health/Education/Other>
                Suggested Title: <short title>
                Expense Type: <Necessary or Discretionary>
                Explanation: <one short sentence>
                Advice: <one short useful sentence>

                Do not add anything before or after this format.
                """.formatted(expenseText);

        String aiText = callGemini(prompt);

        if (aiText.startsWith("AI Error:")) {
            return Map.of("error", aiText);
        }

        return parseAIResponse(aiText);
    }


    // =========================================================
    // 2. AI MONTHLY FINANCIAL REPORT
    // =========================================================

    public Map<String, String> generateMonthlyReport(String month) {

        List<Expense> expenses = expenseRepository.findAll()
                .stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> e.getExpenseDate()
                        .toString()
                        .startsWith(month))
                .toList();

        if (expenses.isEmpty()) {
            return Map.of(
                    "message",
                    "No expenses found for " + month
            );
        }

        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double average = total / expenses.size();

        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {

            categoryTotals.merge(
                    expense.getCategory(),
                    expense.getAmount(),
                    Double::sum
            );
        }

        String topCategory = categoryTotals.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        String expenseData = expenses.stream()
                .map(e -> e.getTitle()
                        + " | ₹"
                        + e.getAmount()
                        + " | "
                        + e.getCategory()
                        + " | "
                        + e.getExpenseDate())
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                You are an AI financial assistant inside an Expense Tracker application.

                Analyze the user's monthly expenses.

                Month: %s

                Total Spending: ₹%.2f

                Number of Transactions: %d

                Average Expense: ₹%.2f

                Top Spending Category: %s

                Expense Details:
                %s

                Generate a concise professional financial report.

                Give the answer in EXACTLY this format:

                Spending Summary: <one or two sentences>
                Spending Behavior: <one sentence>
                Biggest Concern: <one sentence>
                Saving Opportunity: <one practical suggestion>
                AI Recommendation: <one useful recommendation>

                Do not add anything before or after this format.
                """.formatted(
                month,
                total,
                expenses.size(),
                average,
                topCategory,
                expenseData
        );

        String aiText = callGemini(prompt);

        if (aiText.startsWith("AI Error:")) {
            return Map.of("error", aiText);
        }

        Map<String, String> report = parseMonthlyReport(aiText);

        report.put("month", month);
        report.put("totalSpending", String.format("%.2f", total));
        report.put("transactionCount", String.valueOf(expenses.size()));
        report.put("averageExpense", String.format("%.2f", average));
        report.put("topCategory", topCategory);

        return report;
    }


    // =========================================================
    // 3. COMMON GEMINI API CALL
    // =========================================================

    private String callGemini(String prompt) {

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        try {

            String response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            System.out.println("=================================");
            System.out.println("GEMINI RESPONSE:");
            System.out.println(response);
            System.out.println("=================================");

            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {

                JsonNode parts = candidates
                        .get(0)
                        .path("content")
                        .path("parts");

                if (parts.isArray() && parts.size() > 0) {

                    return parts
                            .get(0)
                            .path("text")
                            .asText();
                }
            }

            return "AI Error: AI response received, but text could not be extracted.";

        } catch (RestClientResponseException e) {

            System.out.println("=================================");
            System.out.println("GEMINI ERROR STATUS: "
                    + e.getStatusCode());

            System.out.println("GEMINI ERROR BODY:");
            System.out.println(e.getResponseBodyAsString());

            System.out.println("=================================");

            return "AI Error: " + e.getResponseBodyAsString();

        } catch (Exception e) {

            e.printStackTrace();

            return "AI Error: " + e.getMessage();
        }
    }


    // =========================================================
    // 4. PARSE INDIVIDUAL EXPENSE RESPONSE
    // =========================================================

    private Map<String, String> parseAIResponse(String aiText) {

        Map<String, String> result = new HashMap<>();

        String[] lines = aiText.split("\\r?\\n");

        for (String line : lines) {

            line = line.trim();

            if (line.startsWith("Category:")) {

                result.put(
                        "category",
                        line.substring("Category:".length()).trim()
                );

            } else if (line.startsWith("Suggested Title:")) {

                result.put(
                        "suggestedTitle",
                        line.substring("Suggested Title:".length()).trim()
                );

            } else if (line.startsWith("Expense Type:")) {

                result.put(
                        "expenseType",
                        line.substring("Expense Type:".length()).trim()
                );

            } else if (line.startsWith("Explanation:")) {

                result.put(
                        "explanation",
                        line.substring("Explanation:".length()).trim()
                );

            } else if (line.startsWith("Advice:")) {

                result.put(
                        "advice",
                        line.substring("Advice:".length()).trim()
                );
            }
        }

        return result;
    }


    // =========================================================
    // 5. PARSE MONTHLY REPORT
    // =========================================================

    private Map<String, String> parseMonthlyReport(String aiText) {

        Map<String, String> result = new HashMap<>();

        String[] lines = aiText.split("\\r?\\n");

        for (String line : lines) {

            line = line.trim();

            if (line.startsWith("Spending Summary:")) {

                result.put(
                        "spendingSummary",
                        line.substring("Spending Summary:".length()).trim()
                );

            } else if (line.startsWith("Spending Behavior:")) {

                result.put(
                        "spendingBehavior",
                        line.substring("Spending Behavior:".length()).trim()
                );

            } else if (line.startsWith("Biggest Concern:")) {

                result.put(
                        "biggestConcern",
                        line.substring("Biggest Concern:".length()).trim()
                );

            } else if (line.startsWith("Saving Opportunity:")) {

                result.put(
                        "savingOpportunity",
                        line.substring("Saving Opportunity:".length()).trim()
                );

            } else if (line.startsWith("AI Recommendation:")) {

                result.put(
                        "aiRecommendation",
                        line.substring("AI Recommendation:".length()).trim()
                );
            }
        }

        return result;
    }
}