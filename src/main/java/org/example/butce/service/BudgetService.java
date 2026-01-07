package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Plan;
import org.example.butce.entity.User;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.ExpenseRepository;
import org.example.butce.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PlanRepository planRepository;

    public Budget createBudget(String name, LocalDate startDate, LocalDate endDate, 
                               Double totalBudget, Double monthlyLimit, Long userId) {
        User user = userService.findById(userId);
        
        Budget budget = new Budget();
        budget.setName(name);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setTotalBudget(totalBudget);
        budget.setMonthlyLimit(monthlyLimit);
        budget.setUser(user);
        
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsByUserId(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    public Budget getBudgetByIdAndUserId(Long id, Long userId) {
        Budget budget = getBudgetById(id);
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return budget;
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    public Double getTotalExpenses(Long budgetId) {
        List<org.example.butce.entity.Expense> expenses = expenseRepository.findByBudgetId(budgetId);
        return expenses.stream()
                .mapToDouble(org.example.butce.entity.Expense::getAmount)
                .sum();
    }

    public Double getMonthlyExpenses(Long budgetId, int year, int month) {
        Double total = expenseRepository.sumAmountByBudgetAndMonth(budgetId, year, month);
        return total != null ? total : 0.0;
    }

    public Double getMonthlyPlannedAmount(Long budgetId, int year, int month) {
        List<Plan> plans = planRepository.findByBudgetIdAndYearAndMonth(budgetId, year, month);
        // Sadece tamamlanmamış planları say
        return plans.stream()
                .filter(plan -> !plan.getCompleted())
                .mapToDouble(Plan::getPlannedAmount)
                .sum();
    }
}

