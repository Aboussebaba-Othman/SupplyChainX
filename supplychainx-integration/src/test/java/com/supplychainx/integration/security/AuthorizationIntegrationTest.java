package com.supplychainx.integration.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("Integration Tests - Role-Based Access Control (RBAC)")
class AuthorizationIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== UNAUTHENTICATED ACCESS TESTS ====================

    @Test
    @DisplayName("Should deny access to protected endpoints without authentication token")
    void shouldDenyAccessWithoutToken() throws Exception {
        // Supply module
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isUnauthorized());

        // Production module
        mockMvc.perform(get("/api/production/products"))
                .andExpect(status().isUnauthorized());

        // Delivery module
        mockMvc.perform(get("/api/delivery/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with invalid token")
    void shouldDenyAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access with malformed Authorization header")
    void shouldDenyAccessWithMalformedAuthHeader() throws Exception {
        // Missing "Bearer" prefix
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "token-without-bearer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to public endpoints without token")
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Login endpoint
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk());

        // Check username availability
        mockMvc.perform(get("/api/auth/check-username?username=testuser"))
                .andExpect(status().isOk());

        // Check email availability
        mockMvc.perform(get("/api/auth/check-email?email=test@example.com"))
                .andExpect(status().isOk());
    }

    // ==================== ADMIN ROLE TESTS ====================

    @Test
    @DisplayName("Admin should have full access to all modules")
    void adminShouldHaveFullAccessToAllModules() throws Exception {
        // Given - Admin login
        String adminToken = authenticateAs("admin", "password123");

        // When & Then - Access Supply module (READ)
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Access Production module (READ)
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Access Delivery module (READ)
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin should be able to create, update, and delete in all modules")
    void adminShouldHaveWriteAccessToAllModules() throws Exception {
        // Given
        String adminToken = authenticateAs("admin", "password123");

        // When & Then - Create supplier
        String supplierRequest = """
                {
                    "code": "SUP-ADMIN-001",
                    "name": "Admin Test Supplier",
                    "email": "admin@supplier.com",
                    "phone": "+1111111111",
                    "address": "Admin Test Address",
                    "contact": "Admin Contact",
                    "rating": 5.0,
                    "leadTime": 3
                }
                """;

        mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated());

        // Create product
        String productRequest = """
                {
                    "code": "PROD-ADMIN-001",
                    "name": "Admin Test Product",
                    "description": "Product created by admin",
                    "category": "Test",
                    "cost": 50.00,
                    "productionTime": 120,
                    "stock": 200.0,
                    "stockMin": 50.0
                }
                """;

        mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated());

        // Create customer
        String customerRequest = """
                {
                    "code": "CUST-ADMIN-001",
                    "name": "Admin Test Customer",
                    "email": "admin@customer.com",
                    "phone": "+1111111111",
                    "address": "Admin Test Address",
                    "city": "Admin City",
                    "country": "Admin Country",
                    "postalCode": "11111"
                }
                """;

        mockMvc.perform(post("/api/delivery/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerRequest))
                .andExpect(status().isCreated());
    }

    // ==================== SUPPLY MANAGER TESTS ====================

    @Test
    @DisplayName("Supply manager should have full access to supply module")
    void supplyManagerShouldHaveFullAccessToSupply() throws Exception {
        // Given
        String token = authenticateAs("supply_manager", "password123");

        // When & Then - Can view suppliers
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Can create supplier
        String supplierRequest = """
                {
                    "code": "SUP-SM-001",
                    "name": "Supply Manager Test Supplier",
                    "email": "sm@supplier.com",
                    "phone": "+2222222222",
                    "address": "SM Test Address",
                    "contact": "SM Contact",
                    "rating": 4.0,
                    "leadTime": 5
                }
                """;

        mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Supply manager should NOT have access to production module")
    void supplyManagerShouldNotAccessProduction() throws Exception {
        // Given
        String token = authenticateAs("supply_manager", "password123");

        // When & Then - Cannot access production products
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));

        // Cannot create products
        String productRequest = """
                {
                    "code": "PROD-FORBIDDEN-SM",
                    "name": "Forbidden Product",
                    "description": "Should fail",
                    "category": "Test",
                    "cost": 10.00,
                    "productionTime": 30,
                    "stock": 50.0,
                    "stockMin": 10.0
                }
                """;

        mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Supply manager should NOT have access to delivery module")
    void supplyManagerShouldNotAccessDelivery() throws Exception {
        // Given
        String token = authenticateAs("supply_manager", "password123");

        // When & Then - Cannot access customers
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==================== PRODUCTION MANAGER TESTS ====================

    @Test
    @DisplayName("Production manager should have full access to production module")
    void productionManagerShouldHaveFullAccessToProduction() throws Exception {
        // Given
        String token = authenticateAs("production_manager", "password123");

        // When & Then - Can view products
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Can create product
        String productRequest = """
                {
                    "code": "PROD-PM-001",
                    "name": "Production Manager Test Product",
                    "description": "Product for PM tests",
                    "category": "Test",
                    "cost": 35.00,
                    "productionTime": 90,
                    "stock": 150.0,
                    "stockMin": 30.0
                }
                """;

        mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Production manager should NOT have access to delivery module")
    void productionManagerShouldNotAccessDelivery() throws Exception {
        // Given
        String token = authenticateAs("production_manager", "password123");

        // When & Then - Cannot access customers
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Production manager should NOT have access to supply module")
    void productionManagerShouldNotAccessSupply() throws Exception {
        // Given
        String token = authenticateAs("production_manager", "password123");

        // When & Then - Cannot access suppliers
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==================== SALES MANAGER TESTS ====================

    @Test
    @DisplayName("Sales manager should have full access to delivery module")
    void salesManagerShouldHaveFullAccessToDelivery() throws Exception {
        // Given
        String token = authenticateAs("sales_manager", "password123");

        // When & Then - Can view customers
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Can create customer
        String customerRequest = """
                {
                    "code": "CUST-SALES-001",
                    "name": "Sales Manager Test Customer",
                    "email": "sales@customer.com",
                    "phone": "+3333333333",
                    "address": "Sales Test Address",
                    "city": "Sales City",
                    "country": "Sales Country",
                    "postalCode": "33333"
                }
                """;

        mockMvc.perform(post("/api/delivery/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerRequest))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Sales manager should NOT have access to supply module")
    void salesManagerShouldNotAccessSupply() throws Exception {
        // Given
        String token = authenticateAs("sales_manager", "password123");

        // When & Then - Cannot access suppliers
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Sales manager should NOT have access to production module")
    void salesManagerShouldNotAccessProduction() throws Exception {
        // Given
        String token = authenticateAs("sales_manager", "password123");

        // When & Then - Cannot access products
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==================== LOGISTICS SUPERVISOR TESTS ====================

    @Test
    @DisplayName("Logistics supervisor should only have read access to supply")
    void logisticsSupervisorShouldOnlyReadSupply() throws Exception {
        // Given
        String token = authenticateAs("logistics_supervisor", "password123");

        // When & Then - Can view suppliers
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Cannot create supplier (write permission denied)
        String supplierRequest = """
                {
                    "code": "SUP-FORBIDDEN-LOG",
                    "name": "Forbidden Supplier",
                    "email": "forbidden@supplier.com",
                    "phone": "+4444444444",
                    "address": "Forbidden Address",
                    "contact": "Forbidden Contact",
                    "rating": 3.0,
                    "leadTime": 10
                }
                """;

        mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isForbidden());
    }

    // ==================== PLANNER TESTS ====================

    @Test
    @DisplayName("Planner should have limited access to production")
    void plannerShouldHaveLimitedAccessToProduction() throws Exception {
        // Given
        String token = authenticateAs("planner", "password123");

        // When & Then - Can view production orders
        mockMvc.perform(get("/api/production/production-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Cannot create products (no write permission)
        String productRequest = """
                {
                    "code": "PROD-FORBIDDEN-PLANNER",
                    "name": "Forbidden Product",
                    "description": "Product for forbidden test",
                    "category": "Test",
                    "cost": 15.00,
                    "productionTime": 45,
                    "stock": 75.0,
                    "stockMin": 15.0
                }
                """;

        mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isForbidden());
    }

    // ==================== CROSS-MODULE ACCESS TESTS ====================

    @Test
    @DisplayName("Cross-module access should be properly restricted for all roles")
    void crossModuleAccessShouldBeRestrictedForAllRoles() throws Exception {
        // Supply manager cannot access production
        String supplyToken = authenticateAs("supply_manager", "password123");
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isForbidden());

        // Production manager cannot access delivery
        String productionToken = authenticateAs("production_manager", "password123");
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isForbidden());

        // Sales manager cannot access supply
        String salesToken = authenticateAs("sales_manager", "password123");
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isForbidden());

        // Logistics supervisor cannot access production
        String logisticsToken = authenticateAs("logistics_supervisor", "password123");
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isForbidden());
    }

    // ==================== HTTP METHOD PERMISSION TESTS ====================

    @Test
    @DisplayName("Read-only roles should be able to GET but not POST/PUT/DELETE")
    void readOnlyRolesShouldOnlyAllowGET() throws Exception {
        // Given - Logistics supervisor (read-only for supply)
        String token = authenticateAs("logistics_supervisor", "password123");

        // When & Then - GET is allowed
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // POST is forbidden
        String supplierRequest = """
                {
                    "code": "SUP-TEST",
                    "name": "Test",
                    "email": "test@test.com",
                    "phone": "+0000000000",
                    "address": "Test",
                    "contact": "Test",
                    "rating": 3.0,
                    "leadTime": 5
                }
                """;

        mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isForbidden());

        // PUT is forbidden (assuming supplier with ID 1 exists)
        mockMvc.perform(put("/api/suppliers/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isForbidden());

        // DELETE is forbidden
        mockMvc.perform(delete("/api/suppliers/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==================== AUTHENTICATION ENDPOINTS ACCESS ====================

    @Test
    @DisplayName("Authenticated users should be able to access /me endpoint")
    void authenticatedUsersShouldAccessMeEndpoint() throws Exception {
        // Given
        String token = authenticateAs("admin", "password123");

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("All authenticated users should be able to logout")
    void allAuthenticatedUsersShouldBeAbleToLogout() throws Exception {
        // Given - Login as supply manager
        String token = authenticateAs("supply_manager", "password123");

        // When & Then - Logout should succeed
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ==================== ROLE HIERARCHY TESTS ====================

    @Test
    @DisplayName("Admin role should supersede all other role restrictions")
    void adminRoleShouldSupersedeAllOtherRoles() throws Exception {
        // Given
        String adminToken = authenticateAs("admin", "password123");

        // When & Then - Admin can access endpoints restricted to other roles
        // Supply manager endpoints
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Production manager endpoints
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Sales manager endpoints
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
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
