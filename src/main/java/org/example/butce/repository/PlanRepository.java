package org.example.butce.repository;

import org.example.butce.entity.Budget;
import org.example.butce.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByBudget(Budget budget);
    List<Plan> findByBudgetId(Long budgetId);
    List<Plan> findByBudgetIdAndPlanDateBetween(Long budgetId, LocalDate startDate, LocalDate endDate);
    @Query(value = "SELECT * FROM plans WHERE budget_id = :budgetId AND EXTRACT(YEAR FROM plan_date) = :year AND EXTRACT(MONTH FROM plan_date) = :month", nativeQuery = true)
    List<Plan> findByBudgetIdAndYearAndMonth(@Param("budgetId") Long budgetId, @Param("year") int year, @Param("month") int month);
}

