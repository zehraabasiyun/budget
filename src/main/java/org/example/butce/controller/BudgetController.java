package org.example.butce.controller;

import org.example.butce.entity.Budget;
import org.example.butce.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<?> createBudget(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            String name = (String) request.get("name");
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");
            Double totalBudget = Double.parseDouble(request.get("totalBudget").toString());
            Double monthlyLimit = Double.parseDouble(request.get("monthlyLimit").toString());

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            Budget budget = budgetService.createBudget(name, startDate, endDate, totalBudget, monthlyLimit, userId);
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getBudgets(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        List<Map<String, Object>> budgetsWithStats = budgets.stream().map(budget -> {
            Map<String, Object> budgetMap = new HashMap<>();
            budgetMap.put("id", budget.getId());
            budgetMap.put("name", budget.getName());
            budgetMap.put("startDate", budget.getStartDate());
            budgetMap.put("endDate", budget.getEndDate());
            budgetMap.put("totalBudget", budget.getTotalBudget());
            budgetMap.put("monthlyLimit", budget.getMonthlyLimit());

            // Toplam harcamaları hesapla
            Double totalExpenses = budgetService.getTotalExpenses(budget.getId());
            Double remainingBudget = budget.getTotalBudget() - totalExpenses;
            budgetMap.put("totalExpenses", totalExpenses);
            budgetMap.put("remainingBudget", remainingBudget);

            // Bu ayki harcamaları hesapla
            Double monthlyExpenses = budgetService.getMonthlyExpenses(budget.getId(), currentYear, currentMonth);
            // Planlanan harcamaları da ekle (henüz tamamlanmamış olanlar)
            Double monthlyPlanned = budgetService.getMonthlyPlannedAmount(budget.getId(), currentYear, currentMonth);
            Double totalMonthlySpending = monthlyExpenses + monthlyPlanned;
            Double remainingMonthlyLimit = budget.getMonthlyLimit() - totalMonthlySpending;
            budgetMap.put("monthlyExpenses", monthlyExpenses);
            budgetMap.put("monthlyPlanned", monthlyPlanned);
            budgetMap.put("totalMonthlySpending", totalMonthlySpending);
            budgetMap.put("remainingMonthlyLimit", remainingMonthlyLimit);

            return budgetMap;
        }).toList();

        return ResponseEntity.ok(budgetsWithStats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBudget(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            Budget budget = budgetService.getBudgetByIdAndUserId(id, userId);
            
            // Kalan tutarları hesapla
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            
            Double totalExpenses = budgetService.getTotalExpenses(budget.getId());
            Double remainingBudget = budget.getTotalBudget() - (totalExpenses != null ? totalExpenses : 0.0);
            
            Double monthlyExpenses = budgetService.getMonthlyExpenses(budget.getId(), currentYear, currentMonth);
            // Planlanan harcamaları da ekle (henüz tamamlanmamış olanlar)
            Double monthlyPlanned = budgetService.getMonthlyPlannedAmount(budget.getId(), currentYear, currentMonth);
            Double totalMonthlySpending = (monthlyExpenses != null ? monthlyExpenses : 0.0) + (monthlyPlanned != null ? monthlyPlanned : 0.0);
            Double remainingMonthlyLimit = budget.getMonthlyLimit() - totalMonthlySpending;
            
            Map<String, Object> budgetWithStats = new HashMap<>();
            budgetWithStats.put("id", budget.getId());
            budgetWithStats.put("name", budget.getName());
            budgetWithStats.put("startDate", budget.getStartDate());
            budgetWithStats.put("endDate", budget.getEndDate());
            budgetWithStats.put("totalBudget", budget.getTotalBudget());
            budgetWithStats.put("monthlyLimit", budget.getMonthlyLimit());
            budgetWithStats.put("totalExpenses", totalExpenses != null ? totalExpenses : 0.0);
            budgetWithStats.put("remainingBudget", remainingBudget);
            budgetWithStats.put("monthlyExpenses", monthlyExpenses != null ? monthlyExpenses : 0.0);
            budgetWithStats.put("monthlyPlanned", monthlyPlanned != null ? monthlyPlanned : 0.0);
            budgetWithStats.put("totalMonthlySpending", totalMonthlySpending);
            budgetWithStats.put("remainingMonthlyLimit", remainingMonthlyLimit);
            
            return ResponseEntity.ok(budgetWithStats);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            budgetService.getBudgetByIdAndUserId(id, userId);
            budgetService.deleteBudget(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Budget deleted"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

