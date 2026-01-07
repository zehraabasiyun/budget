package org.example.butce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.butce.entity.Budget;
import org.example.butce.entity.Category;
import org.example.butce.entity.User;
import org.example.butce.repository.BudgetRepository;
import org.example.butce.repository.CategoryRepository;
import org.example.butce.repository.IncomeRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IncomeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncomeRepository incomeRepository;

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
        incomeRepository.deleteAll();
        budgetRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        User user = new User();
        user.setEmail("incomeuser@example.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Budget budget = new Budget();
        budget.setName("Income Budget");
        budget.setTotalBudget(1000.0);
        budget.setMonthlyLimit(1000.0);
        budget.setUser(savedUser);
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusMonths(1));
        Budget savedBudget = budgetRepository.save(budget);
        budgetId = savedBudget.getId();

        Category category = new Category("Salary", "INCOME");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getId();

        session = new MockHttpSession();
        session.setAttribute("userId", savedUser.getId());
    }

    @Test
    void createIncome_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 5000.0);
        request.put("description", "Monthly Salary");
        request.put("date", LocalDate.now().toString());
        request.put("budgetId", budgetId);
        request.put("categoryId", categoryId);

        mockMvc.perform(post("/api/incomes")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000.0));
    }

    @Test
    void getIncomesByBudget_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/incomes/budget/" + budgetId)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
