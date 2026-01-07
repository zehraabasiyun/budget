package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Expense;
import org.example.butce.entity.Plan;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.ExpenseRepository;
import org.example.butce.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlanRepository planRepository;

    @Transactional
    public Expense createExpense(Double amount, String description, LocalDate date, 
                                Long budgetId, Long categoryId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Aylık limit kontrolü (gerçekleşen harcamalar + planlanan harcamalar)
        int year = date.getYear();
        int month = date.getMonthValue();
        Double currentMonthTotal = expenseRepository.sumAmountByBudgetAndMonth(budgetId, year, month);
        
        if (currentMonthTotal == null) {
            currentMonthTotal = 0.0;
        }
        
        // Planlanan harcamaları da ekle (henüz tamamlanmamış olanlar)
        List<Plan> plans = planRepository.findByBudgetIdAndYearAndMonth(budgetId, year, month);
        Double plannedAmount = plans.stream()
                .filter(plan -> !plan.getCompleted())
                .mapToDouble(Plan::getPlannedAmount)
                .sum();
        
        Double totalSpending = currentMonthTotal + plannedAmount;
        
        if (totalSpending + amount > budget.getMonthlyLimit()) {
            throw new RuntimeException("Aylık harcama limiti aşıldı! Mevcut Harcama: " + currentMonthTotal + 
                    ", Planlanan: " + plannedAmount + ", Toplam: " + totalSpending +
                    ", Limit: " + budget.getMonthlyLimit() + ", Eklenmek istenen: " + amount);
        }

        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setDate(date);
        expense.setBudget(budget);
        expense.setCategory(category);

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByBudgetId(Long budgetId) {
        return expenseRepository.findByBudgetId(budgetId);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
}

