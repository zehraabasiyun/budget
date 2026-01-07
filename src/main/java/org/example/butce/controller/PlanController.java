package org.example.butce.controller;

import org.example.butce.entity.Plan;
import org.example.butce.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    @Autowired
    private PlanService planService;

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            String description = (String) request.get("description");
            Double plannedAmount = Double.parseDouble(request.get("plannedAmount").toString());
            String planDateStr = (String) request.get("planDate");
            Long budgetId = Long.parseLong(request.get("budgetId").toString());
            Long categoryId = Long.parseLong(request.get("categoryId").toString());

            LocalDate planDate = LocalDate.parse(planDateStr);

            Plan plan = planService.createPlan(description, plannedAmount, planDate, budgetId, categoryId);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<?> getPlansByBudget(@PathVariable Long budgetId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        List<Plan> plans = planService.getPlansByBudgetId(budgetId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/budget/{budgetId}/month")
    public ResponseEntity<?> getPlansByBudgetAndMonth(@PathVariable Long budgetId, 
                                                     @RequestParam int year, 
                                                     @RequestParam int month, 
                                                     HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        List<Plan> plans = planService.getPlansByBudgetIdAndMonth(budgetId, year, month);
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePlanStatus(@PathVariable Long id, 
                                               @RequestBody Map<String, Object> request, 
                                               HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            Boolean completed = Boolean.parseBoolean(request.get("completed").toString());
            Plan plan = planService.updatePlanStatus(id, completed);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        try {
            planService.deletePlan(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Plan deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}

