package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Income;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private IncomeService incomeService;

    @Test
    void createIncome_ShouldSaveIncome_WhenValidData() {
        // Arrange
        Long budgetId = 1L;
        Long categoryId = 2L;

        Budget budget = new Budget();
        budget.setId(budgetId);

        Category category = new Category();
        category.setId(categoryId);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Income incomeToSave = new Income();
        incomeToSave.setId(10L);
        when(incomeRepository.save(any(Income.class))).thenReturn(incomeToSave);

        // Act
        Income result = incomeService.createIncome(500.0, "Bonus", LocalDate.now(), budgetId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(incomeRepository).save(any(Income.class));
    }

    @Test
    void createIncome_ShouldThrowException_WhenBudgetNotFound() {
        // Arrange
        Long budgetId = 1L;
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            incomeService.createIncome(100.0, "Test", LocalDate.now(), budgetId, 1L);
        });
        assertEquals("Budget not found", exception.getMessage());
    }
}
