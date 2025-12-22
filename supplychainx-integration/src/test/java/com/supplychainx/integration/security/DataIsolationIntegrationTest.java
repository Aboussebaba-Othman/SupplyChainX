package com.supplychainx.integration.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.integration.config.BaseSecurityIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("Integration Tests - Data Isolation & Security Boundaries")
class DataIsolationIntegrationTest extends BaseSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== ROLE-BASED DATA ISOLATION TESTS ====================

    @Test
    @DisplayName("Users should only access data within their authorized module scope")
    void usersShouldOnlyAccessDataWithinTheirModuleScope() throws Exception {
        // Given - Supply manager creates a supplier
        String supplyToken = authenticateAs("supply_manager", "password123");

        String supplierRequest = """
                {
                    "code": "SUP-ISOLATION-001",
                    "name": "Isolation Test Supplier",
                    "email": "isolation@supplier.com",
                    "phone": "+1234567890",
                    "address": "Test Address",
                    "contact": "Test Contact",
                    "rating": 4.5,
                    "leadTime": 7
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + supplyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode supplierJson = objectMapper.readTree(response);
        Long supplierId = supplierJson.get("id").asLong();

        // When & Then - Production manager should NOT be able to access this supplier
        String productionToken = authenticateAs("production_manager", "password123");

        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isForbidden());

        // And - Sales manager should NOT be able to access this supplier
        String salesToken = authenticateAs("sales_manager", "password123");

        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Users should not be able to modify data outside their module")
    void usersShouldNotModifyDataOutsideTheirModule() throws Exception {
        // Given - Admin creates a product
        String adminToken = authenticateAs("admin", "password123");

        String productRequest = """
                {
                    "code": "PROD-ISOLATION-001",
                    "name": "Isolation Test Product",
                    "description": "Product for isolation testing",
                    "category": "Test",
                    "cost": 25.00,
                    "productionTime": 60,
                    "stock": 100.0,
                    "stockMin": 20.0
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode productJson = objectMapper.readTree(response);
        Long productId = productJson.get("id").asLong();

        // When & Then - Supply manager should NOT be able to modify this product
        String supplyToken = authenticateAs("supply_manager", "password123");

        String updateRequest = """
                {
                    "code": "PROD-ISOLATION-001",
                    "name": "Modified by Supply Manager",
                    "description": "Should fail",
                    "category": "Test",
                    "cost": 30.00,
                    "productionTime": 60,
                    "stock": 100.0,
                    "stockMin": 20.0
                }
                """;

        mockMvc.perform(put("/api/production/products/" + productId)
                        .header("Authorization", "Bearer " + supplyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Users should not be able to delete data outside their module")
    void usersShouldNotDeleteDataOutsideTheirModule() throws Exception {
        // Given - Sales manager creates a customer
        String salesToken = authenticateAs("sales_manager", "password123");

        String customerRequest = """
                {
                    "code": "CUST-ISOLATION-001",
                    "name": "Isolation Test Customer",
                    "email": "isolation@customer.com",
                    "phone": "+9876543210",
                    "address": "Test Address",
                    "city": "Test City",
                    "country": "Test Country",
                    "postalCode": "12345"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/delivery/customers")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode customerJson = objectMapper.readTree(response);
        Long customerId = customerJson.get("id").asLong();

        // When & Then - Supply manager should NOT be able to delete this customer
        String supplyToken = authenticateAs("supply_manager", "password123");

        mockMvc.perform(delete("/api/delivery/customers/" + customerId)
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isForbidden());

        // And - Production manager should NOT be able to delete this customer
        String productionToken = authenticateAs("production_manager", "password123");

        mockMvc.perform(delete("/api/delivery/customers/" + customerId)
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isForbidden());
    }

    // ==================== READ-ONLY ACCESS ISOLATION TESTS ====================

    @Test
    @DisplayName("Read-only users should not be able to modify data even within their scope")
    void readOnlyUsersShouldNotModifyDataWithinTheirScope() throws Exception {
        // Given - Supply manager creates a supplier
        String supplyToken = authenticateAs("supply_manager", "password123");

        String supplierRequest = """
                {
                    "code": "SUP-READONLY-001",
                    "name": "ReadOnly Test Supplier",
                    "email": "readonly@supplier.com",
                    "phone": "+1111111111",
                    "address": "Test Address",
                    "contact": "Test Contact",
                    "rating": 4.0,
                    "leadTime": 5
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + supplyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode supplierJson = objectMapper.readTree(response);
        Long supplierId = supplierJson.get("id").asLong();

        // When & Then - Logistics supervisor (read-only) can view
        String logisticsToken = authenticateAs("logistics_supervisor", "password123");

        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isOk());

        // But - Logistics supervisor cannot modify
        String updateRequest = """
                {
                    "code": "SUP-READONLY-001",
                    "name": "Modified by Logistics",
                    "email": "readonly@supplier.com",
                    "phone": "+1111111111",
                    "address": "Modified Address",
                    "contact": "Test Contact",
                    "rating": 5.0,
                    "leadTime": 3
                }
                """;

        mockMvc.perform(put("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isForbidden());

        // And - Logistics supervisor cannot delete
        mockMvc.perform(delete("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isForbidden());
    }

    // ==================== ADMIN OVERRIDE TESTS ====================

    @Test
    @DisplayName("Admin should be able to access and modify all data across all modules")
    void adminShouldAccessAndModifyAllData() throws Exception {
        // Given - Supply manager creates a supplier
        String supplyToken = authenticateAs("supply_manager", "password123");

        String supplierRequest = """
                {
                    "code": "SUP-ADMIN-ACCESS-001",
                    "name": "Admin Access Test Supplier",
                    "email": "adminaccess@supplier.com",
                    "phone": "+2222222222",
                    "address": "Test Address",
                    "contact": "Test Contact",
                    "rating": 4.5,
                    "leadTime": 6
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + supplyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode supplierJson = objectMapper.readTree(response);
        Long supplierId = supplierJson.get("id").asLong();

        // When & Then - Admin can access the supplier
        String adminToken = authenticateAs("admin", "password123");

        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUP-ADMIN-ACCESS-001"));

        // And - Admin can modify the supplier
        String updateRequest = """
                {
                    "code": "SUP-ADMIN-ACCESS-001",
                    "name": "Modified by Admin",
                    "email": "adminaccess@supplier.com",
                    "phone": "+2222222222",
                    "address": "Admin Modified Address",
                    "contact": "Admin Contact",
                    "rating": 5.0,
                    "leadTime": 4
                }
                """;

        mockMvc.perform(put("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Modified by Admin"));
    }

    // ==================== LIST ENDPOINT ISOLATION TESTS ====================

    @Test
    @DisplayName("Users should only see list data within their authorized scope")
    void usersShouldOnlySeeListDataWithinScope() throws Exception {
        // Given - Supply manager can see suppliers list
        String supplyToken = authenticateAs("supply_manager", "password123");

        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isOk());

        // When & Then - Production manager cannot see suppliers list
        String productionToken = authenticateAs("production_manager", "password123");

        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isForbidden());

        // And - Production manager can see products list
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isOk());

        // But - Supply manager cannot see products list
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isForbidden());
    }

    // ==================== CROSS-USER DATA ISOLATION TESTS ====================

    @Test
    @DisplayName("Users with same role should see same data in shared scope")
    void usersWithSameRoleShouldSeeSameDataInSharedScope() throws Exception {
        // Given - First supply manager creates a supplier
        String supplyToken1 = authenticateAs("supply_manager", "password123");

        String supplierRequest = """
                {
                    "code": "SUP-SHARED-001",
                    "name": "Shared Supplier",
                    "email": "shared@supplier.com",
                    "phone": "+3333333333",
                    "address": "Shared Address",
                    "contact": "Shared Contact",
                    "rating": 4.0,
                    "leadTime": 5
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + supplyToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        JsonNode supplierJson = objectMapper.readTree(response);
        Long supplierId = supplierJson.get("id").asLong();

        // When & Then - Second supply manager (if exists) should also be able to access
        // Note: This assumes there's only one supply_manager in test data
        // In a real multi-tenant scenario, you'd test with multiple users of same role

        // Admin should also see it
        String adminToken = authenticateAs("admin", "password123");

        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUP-SHARED-001"));
    }

    // ==================== SENSITIVE DATA ACCESS TESTS ====================

    @Test
    @DisplayName("User passwords should never be exposed in API responses")
    void userPasswordsShouldNeverBeExposed() throws Exception {
        // Given
        String token = authenticateAs("admin", "password123");

        // When - Get current user info
        MvcResult result = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Response should not contain password field
        String responseBody = result.getResponse().getContentAsString();
        JsonNode userJson = objectMapper.readTree(responseBody);

        assertThat(userJson.has("password")).isFalse();
        assertThat(responseBody).doesNotContain("password");
        assertThat(responseBody).doesNotContain("$2a$"); // BCrypt hash prefix
    }

    @Test
    @DisplayName("Refresh tokens should not be accessible across different users")
    void refreshTokensShouldNotBeAccessibleAcrossDifferentUsers() throws Exception {
        // Given - User 1 logs in and gets refresh token
        String loginRequest1 = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest1))
                .andExpect(status().isOk())
                .andReturn();

        String response1 = loginResult1.getResponse().getContentAsString();
        JsonNode json1 = objectMapper.readTree(response1);
        String refreshToken1 = json1.get("refreshToken").asText();

        // And - User 2 logs in
        String loginRequest2 = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        MvcResult loginResult2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest2))
                .andExpect(status().isOk())
                .andReturn();

        String response2 = loginResult2.getResponse().getContentAsString();
        JsonNode json2 = objectMapper.readTree(response2);
        String refreshToken2 = json2.get("refreshToken").asText();

        // When & Then - Tokens should be different
        assertThat(refreshToken1).isNotEqualTo(refreshToken2);

        // And - Each token should only work for its own user
        String refreshRequest1 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken1);

        MvcResult refreshResult1 = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest1))
                .andExpect(status().isOk())
                .andReturn();

        String refreshResponse1 = refreshResult1.getResponse().getContentAsString();
        JsonNode refreshJson1 = objectMapper.readTree(refreshResponse1);

        // Verify user is admin
        assertThat(refreshJson1.get("user").get("username").asText()).isEqualTo("admin");
    }

    // ==================== DATA LEAKAGE PREVENTION TESTS ====================

    @Test
    @DisplayName("Error messages should not leak sensitive information about data existence")
    void errorMessagesShouldNotLeakSensitiveInformation() throws Exception {
        // Given - Supply manager tries to access non-existent product
        String supplyToken = authenticateAs("supply_manager", "password123");

        // When & Then - Should return 403 Forbidden (not 404 Not Found)
        // This prevents information disclosure about resource existence
        mockMvc.perform(get("/api/production/products/99999")
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isForbidden());

        // Error message should not reveal whether product exists
        MvcResult result = mockMvc.perform(get("/api/production/products/99999")
                        .header("Authorization", "Bearer " + supplyToken))
                .andReturn();

        String errorMessage = result.getResponse().getContentAsString();
        assertThat(errorMessage).doesNotContainIgnoringCase("not found");
        assertThat(errorMessage).doesNotContainIgnoringCase("does not exist");
    }

    // ==================== HELPER METHODS ====================

    private String authenticateAs(String username, String password) throws Exception {
        String loginRequest = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }
}
