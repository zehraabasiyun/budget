package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Plan;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.ExpenseRepository;
import org.example.butce.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    void createPlan_ShouldSavePlan_WhenWithinLimit() {
        // Arrange
        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setMonthlyLimit(2000.0);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(new Category()));

        when(expenseRepository.sumAmountByBudgetAndMonth(eq(budgetId), anyInt(), anyInt())).thenReturn(500.0);
        when(planRepository.findByBudgetIdAndYearAndMonth(eq(budgetId), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        Plan planToSave = new Plan();
        planToSave.setId(1L);
        when(planRepository.save(any(Plan.class))).thenReturn(planToSave);

        // Act
        Plan result = planService.createPlan("Holiday", 1000.0, LocalDate.now(), budgetId, 1L);

        // Assert
        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void updatePlanStatus_ShouldCreateExpense_WhenCompleted() {
        // Arrange
        Long planId = 1L;
        Plan plan = new Plan();
        plan.setId(planId);
        plan.setCompleted(false);
        plan.setPlannedAmount(100.0);
        plan.setPlanDate(LocalDate.now());
        plan.setDescription("Dinner");

        Budget budget = new Budget();
        budget.setId(10L);
        plan.setBudget(budget);

        Category category = new Category();
        category.setId(5L);
        plan.setCategory(category);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(plan);

        // Act
        planService.updatePlanStatus(planId, true);

        // Assert
        assertTrue(plan.getCompleted());
        verify(expenseService).createExpense(eq(100.0), anyString(), any(LocalDate.class), eq(10L), eq(5L));
    }
}
