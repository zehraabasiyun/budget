package org.example.butce.service;

import org.example.butce.entity.Budget;
import org.example.butce.entity.User;
import org.example.butce.repository.BudgetRepository;
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
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void createBudget_ShouldSaveBudget_WhenUserExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(user);

        Budget budgetToSave = new Budget();
        budgetToSave.setId(10L);
        budgetToSave.setName("Test Budget");
        when(budgetRepository.save(any(Budget.class))).thenReturn(budgetToSave);

        // Act
        Budget result = budgetService.createBudget("Test Budget", LocalDate.now(), LocalDate.now().plusMonths(1),
                1000.0, 500.0, userId);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Test Budget", result.getName());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void getBudgetByIdAndUserId_ShouldReturnBudget_WhenAccessGranted() {
        // Arrange
        Long budgetId = 10L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUser(user);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act
        Budget result = budgetService.getBudgetByIdAndUserId(budgetId, userId);

        // Assert
        assertEquals(budget, result);
    }

    @Test
    void getBudgetByIdAndUserId_ShouldThrowException_WhenAccessDenied() {
        // Arrange
        Long budgetId = 10L;
        Long userId = 1L;
        Long otherUserId = 2L;

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUser(otherUser);

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.getBudgetByIdAndUserId(budgetId, userId);
        });
        assertEquals("Access denied", exception.getMessage());
    }
}
