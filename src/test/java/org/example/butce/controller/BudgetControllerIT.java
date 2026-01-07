package org.example.butce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.butce.entity.User;
import org.example.butce.repository.BudgetRepository;
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
public class BudgetControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private Long userId;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("budgetuser@example.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();

        session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userEmail", savedUser.getEmail());
    }

    @Test
    void createBudget_ShouldReturnOk_WhenAuthorized() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "My Budget");
        request.put("startDate", LocalDate.now().toString());
        request.put("endDate", LocalDate.now().plusMonths(1).toString());
        request.put("totalBudget", 5000.0);
        request.put("monthlyLimit", 2000.0);

        mockMvc.perform(post("/api/budgets")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Budget"));
    }

    @Test
    void getBudgets_ShouldReturnList_WhenAuthorized() throws Exception {
        mockMvc.perform(get("/api/budgets")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBudgets_ShouldReturnUnauthorized_WhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isUnauthorized());
    }
}
