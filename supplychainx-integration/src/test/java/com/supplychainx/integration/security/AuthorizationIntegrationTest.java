package com.supplychainx.integration.security;

import com.jayway.jsonpath.JsonPath;
import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour l'autorisation basée sur les rôles et permissions
 */
@DisplayName("Integration Tests - Authorization & Permissions")
class AuthorizationIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should deny access without authentication token")
    void shouldDenyAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/suppliers"))
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
    @DisplayName("Admin should have access to all modules")
    void adminShouldHaveAccessToAllModules() throws Exception {
        // Given - Admin login
        String adminToken = authenticateAs("admin", "password123");

        // When & Then - Access Supply module
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Access Production module
        mockMvc.perform(get("/api/production/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Access Delivery module
        mockMvc.perform(get("/api/delivery/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

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
                    "code": "SUP-AUTH-TEST-001",
                    "name": "Auth Test Supplier",
                    "email": "auth@supplier.com",
                    "phone": "+1111111111",
                    "address": "Test Address",
                    "contact": "Test Contact",
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
    }

    @Test
    @DisplayName("Logistics supervisor should only have read access to supply")
    void logisticsSupervisorShouldOnlyReadSupply() throws Exception {
        // Given
        String token = authenticateAs("logistics_supervisor", "password123");

        // When & Then - Can view suppliers
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Cannot create supplier
        String supplierRequest = """
                {
                    "code": "SUP-FORBIDDEN-001",
                    "name": "Forbidden Supplier",
                    "email": "forbidden@supplier.com",
                    "phone": "+2222222222",
                    "address": "Test",
                    "contact": "Test",
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
                    "code": "PROD-AUTH-TEST-001",
                    "name": "Auth Test Product",
                    "description": "Product for auth tests",
                    "category": "Test",
                    "cost": 30.00,
                    "productionTime": 60,
                    "stock": 100.0,
                    "stockMin": 20.0
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
                    "code": "CUST-AUTH-TEST-001",
                    "name": "Auth Test Customer",
                    "email": "auth@customer.com",
                    "phone": "+3333333333",
                    "address": "Test Address",
                    "city": "Test City",
                    "country": "Test Country",
                    "postalCode": "00000"
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
    @DisplayName("Planner should have limited access to production")
    void plannerShouldHaveLimitedAccessToProduction() throws Exception {
        // Given
        String token = authenticateAs("planner", "password123");

        // When & Then - Can view production orders
        mockMvc.perform(get("/api/production/production-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Cannot create products (no permission)
        String productRequest = """
                {
                    "code": "PROD-FORBIDDEN-001",
                    "name": "Forbidden Product",
                    "description": "Product for forbidden test",
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
    @DisplayName("Cross-module access should be properly restricted")
    void crossModuleAccessShouldBeRestricted() throws Exception {
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
    }

    // Helper method
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

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
