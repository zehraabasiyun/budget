package org.example.butce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.Plan;
import org.example.butce.entity.User;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.PlanRepository;
import org.example.butce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PlanControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private Long budgetId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        planRepository.deleteAll();
        budgetRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        User user = new User();
        user.setEmail("planuser@example.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Budget budget = new Budget();
        budget.setName("Plan Budget");
        budget.setTotalBudget(1000.0);
        budget.setMonthlyLimit(2000.0);
        budget.setUser(savedUser);
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusMonths(1));
        Budget savedBudget = budgetRepository.save(budget);
        budgetId = savedBudget.getId();

        Category category = new Category("Vacation", "EXPENSE");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getId();

        session = new MockHttpSession();
        session.setAttribute("userId", savedUser.getId());
    }

    @Test
    void createPlan_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("description", "Hotel Booking");
        request.put("plannedAmount", 500.0);
        request.put("planDate", LocalDate.now().toString());
        request.put("budgetId", budgetId);
        request.put("categoryId", categoryId);

        mockMvc.perform(post("/api/plans")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedAmount").value(500.0));
    }

    @Test
    void updatePlanStatus_ShouldReturnOk_WhenCompleted() throws Exception {
        Plan plan = new Plan();
        plan.setDescription("Flight");
        plan.setPlannedAmount(300.0);
        plan.setPlanDate(LocalDate.now());
        plan.setBudget(budgetRepository.findById(budgetId).get());
        plan.setCategory(categoryRepository.findById(categoryId).get());
        plan.setCompleted(false);
        Plan savedPlan = planRepository.save(plan);

        Map<String, Object> request = new HashMap<>();
        request.put("completed", true);

        mockMvc.perform(put("/api/plans/" + savedPlan.getId() + "/status")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }
}
