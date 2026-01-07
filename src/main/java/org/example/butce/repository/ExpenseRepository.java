package org.example.butce.repository;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBudget(Budget budget);
    List<Expense> findByBudgetId(Long budgetId);
    
    @Query(value = "SELECT COALESCE(SUM(e.amount), 0) FROM expenses e WHERE e.budget_id = :budgetId AND EXTRACT(YEAR FROM e.date) = :year AND EXTRACT(MONTH FROM e.date) = :month", nativeQuery = true)
    Double sumAmountByBudgetAndMonth(@Param("budgetId") Long budgetId, @Param("year") int year, @Param("month") int month);
    
    List<Expense> findByBudgetAndDateBetween(Budget budget, LocalDate startDate, LocalDate endDate);
}

