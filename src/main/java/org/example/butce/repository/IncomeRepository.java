package org.example.butce.repository;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByBudget(Budget budget);
    List<Income> findByBudgetId(Long budgetId);
}

