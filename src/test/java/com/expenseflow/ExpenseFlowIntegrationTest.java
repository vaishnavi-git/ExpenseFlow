package com.expenseflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExpenseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    @Test
    @Order(1)
    @DisplayName("Admin login should return JWT token")
    void testLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Invalid credentials should return 401")
    void testInvalidLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "admin", "password", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("Unauthenticated request should be rejected")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/expenses should return list when authenticated")
    void testGetExpenses() throws Exception {
        mockMvc.perform(get("/api/expenses")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/expenses/flagged should return flagged reports")
    void testGetFlaggedExpenses() throws Exception {
        mockMvc.perform(get("/api/expenses/flagged")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/audit/summary should return aggregate metrics")
    void testAuditSummary() throws Exception {
        mockMvc.perform(get("/api/audit/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").exists())
                .andExpect(jsonPath("$.flaggedReports").exists())
                .andExpect(jsonPath("$.openViolations").exists());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/audit/violations should return open violations")
    void testGetViolations() throws Exception {
        mockMvc.perform(get("/api/audit/violations")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/expenses/pending should return pending reports")
    void testGetPending() throws Exception {
        mockMvc.perform(get("/api/expenses/pending")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(9)
    @DisplayName("Actuator health should be publicly accessible")
    void testHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
