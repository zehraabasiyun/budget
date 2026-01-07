package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Expense;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createExpense_ShouldSaveExpense_WhenWithinLimit() {
        // Arrange
        Long budgetId = 1L;
        Double limit = 1000.0;

        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setMonthlyLimit(limit);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(new Category()));

        when(expenseRepository.sumAmountByBudgetAndMonth(eq(budgetId), anyInt(), anyInt())).thenReturn(500.0);
        when(planRepository.findByBudgetIdAndYearAndMonth(eq(budgetId), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        Expense expenseToSave = new Expense();
        expenseToSave.setId(1L);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expenseToSave);

        // Act
        Expense result = expenseService.createExpense(200.0, "Grocery", LocalDate.now(), budgetId, 1L);

        // Assert
        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_ShouldThrowException_WhenLimitExceeded() {
        // Arrange
        Long budgetId = 1L;
        Double limit = 1000.0;

        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setMonthlyLimit(limit);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(new Category()));

        when(expenseRepository.sumAmountByBudgetAndMonth(eq(budgetId), anyInt(), anyInt())).thenReturn(900.0);
        when(planRepository.findByBudgetIdAndYearAndMonth(eq(budgetId), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.createExpense(200.0, "Luxury", LocalDate.now(), budgetId, 1L);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Aylık harcama limiti aşıldı"));
        verify(expenseRepository, never()).save(any(Expense.class));
    }
}
