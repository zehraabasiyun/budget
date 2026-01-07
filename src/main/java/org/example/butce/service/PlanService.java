package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
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
public class PlanService {
    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Transactional
    public Plan createPlan(String description, Double plannedAmount, LocalDate planDate, 
                          Long budgetId, Long categoryId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Aylık limit kontrolü (gerçekleşen harcamalar + mevcut planlanan harcamalar)
        int year = planDate.getYear();
        int month = planDate.getMonthValue();
        Double currentMonthTotal = expenseRepository.sumAmountByBudgetAndMonth(budgetId, year, month);
        
        if (currentMonthTotal == null) {
            currentMonthTotal = 0.0;
        }
        
        // Mevcut planlanan harcamaları da ekle (henüz tamamlanmamış olanlar)
        List<Plan> existingPlans = planRepository.findByBudgetIdAndYearAndMonth(budgetId, year, month);
        Double existingPlannedAmount = existingPlans.stream()
                .filter(plan -> !plan.getCompleted())
                .mapToDouble(Plan::getPlannedAmount)
                .sum();
        
        Double totalSpending = currentMonthTotal + existingPlannedAmount;
        
        if (totalSpending + plannedAmount > budget.getMonthlyLimit()) {
            throw new RuntimeException("Aylık harcama limiti aşıldı! Mevcut Harcama: " + currentMonthTotal + 
                    ", Mevcut Planlanan: " + existingPlannedAmount + ", Toplam: " + totalSpending +
                    ", Limit: " + budget.getMonthlyLimit() + ", Eklenmek istenen Plan: " + plannedAmount);
        }

        Plan plan = new Plan();
        plan.setDescription(description);
        plan.setPlannedAmount(plannedAmount);
        plan.setPlanDate(planDate);
        plan.setBudget(budget);
        plan.setCategory(category);
        plan.setCompleted(false);

        return planRepository.save(plan);
    }

    public List<Plan> getPlansByBudgetId(Long budgetId) {
        return planRepository.findByBudgetId(budgetId);
    }

    public List<Plan> getPlansByBudgetIdAndMonth(Long budgetId, int year, int month) {
        return planRepository.findByBudgetIdAndYearAndMonth(budgetId, year, month);
    }

    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    @Transactional
    public Plan updatePlanStatus(Long id, Boolean completed) {
        Plan plan = getPlanById(id);
        Boolean wasCompleted = plan.getCompleted();
        plan.setCompleted(completed);
        
        // Eğer plan tamamlandıysa ve daha önce tamamlanmamışsa, otomatik expense oluştur
        if (completed && !wasCompleted) {
            try {
                expenseService.createExpense(
                    plan.getPlannedAmount(),
                    "Plan: " + plan.getDescription(),
                    plan.getPlanDate(),
                    plan.getBudget().getId(),
                    plan.getCategory().getId()
                );
            } catch (Exception e) {
                // Eğer expense oluşturulamazsa (örneğin limit aşıldıysa), plan durumunu geri al
                plan.setCompleted(false);
                throw new RuntimeException("Plan tamamlanamadı: " + e.getMessage());
            }
        }
        // Eğer plan iptal edildiyse (completed = false), oluşturulan expense'i silmek için
        // bir mekanizma eklenebilir, ama şimdilik sadece plan durumunu güncelliyoruz
        
        return planRepository.save(plan);
    }

    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
}

