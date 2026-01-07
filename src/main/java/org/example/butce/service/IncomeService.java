package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Income;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class IncomeService {
    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Income createIncome(Double amount, String description, LocalDate date, 
                              Long budgetId, Long categoryId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Income income = new Income();
        income.setAmount(amount);
        income.setDescription(description);
        income.setDate(date);
        income.setBudget(budget);
        income.setCategory(category);

        return incomeRepository.save(income);
    }

    public List<Income> getIncomesByBudgetId(Long budgetId) {
        return incomeRepository.findByBudgetId(budgetId);
    }

    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }
}

