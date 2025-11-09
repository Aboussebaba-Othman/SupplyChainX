package com.supplychainx.integration.workflow;

import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration E2E pour le workflow complet du module Delivery
 * 
 * Scénario testé:
 * 1. Authentification d'un gestionnaire commercial
 * 2. Création d'un produit (setup avec production manager)
 * 3. Création d'un client
 * 4. Création d'une commande client
 * 5. Création d'une livraison
 * 6. Passage de la livraison en statut EN_COURS
 * 7. Finalisation de la livraison (réduction stock produit)
 * 8. Vérification du stock produit
 */
@DisplayName("Integration Tests - Delivery Workflow E2E")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeliveryWorkflowIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String authToken;
    private static String productionToken;
    private static Long productId;
    private static Long customerId;
    private static Long deliveryOrderId;
    private static Long deliveryId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Authenticate as sales manager")
    void step1_authenticateAsSalesManager() throws Exception {
        String loginRequest = """
                {
                    "username": "sales_manager",
                    "password": "password123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("GESTIONNAIRE_COMMERCIAL"))
                .andReturn();

        authToken = extractToken(result.getResponse().getContentAsString());
        Assertions.assertNotNull(authToken);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Setup - Create product (as production manager)")
    void step2_setupProduct() throws Exception {
        // Login as production manager
        String loginRequest = """
                {
                    "username": "production_manager",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        productionToken = extractToken(loginResult.getResponse().getContentAsString());

        // Create product
        String productRequest = """
                {
                    "code": "PROD-DELIVERY-TEST-001",
                    "name": "Product for Delivery Test",
                    "description": "Test product for delivery workflow",
                    "category": "Electronics",
                    "unit": "piece",
                    "productionCost": 50.00,
                    "sellingPrice": 120.00,
                    "productionTime": 60,
                    "stock": 100,
                    "stockMin": 20
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + productionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("PROD-DELIVERY-TEST-001"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andReturn();

        productId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(productId);
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Create a customer")
    void step3_createCustomer() throws Exception {
        String customerRequest = """
                {
                    "code": "CUST-TEST-001",
                    "name": "Test Customer Corp",
                    "email": "test@customer.com",
                    "phone": "+9876543210",
                    "address": "456 Customer Avenue",
                    "city": "Test City",
                    "country": "Test Country",
                    "postalCode": "12345"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/delivery/customers")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("CUST-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Customer Corp"))
                .andReturn();

        customerId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(customerId);
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Create delivery order")
    void step4_createDeliveryOrder() throws Exception {
        String orderRequest = String.format("""
                {
                    "orderNumber": "DO-TEST-001",
                    "customerId": %d,
                    "orderDate": "2025-11-09",
                    "requestedDeliveryDate": "2025-11-12",
                    "status": "EN_ATTENTE",
                    "orderLines": [
                        {
                            "productId": %d,
                            "quantity": 15,
                            "unitPrice": 120.00
                        }
                    ]
                }
                """, customerId, productId);

        MvcResult result = mockMvc.perform(post("/api/delivery/orders")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value("DO-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("EN_ATTENTE"))
                .andReturn();

        deliveryOrderId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(deliveryOrderId);
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Verify order total amount")
    void step5_verifyOrderTotalAmount() throws Exception {
        mockMvc.perform(get("/api/delivery/orders/" + deliveryOrderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(1800.00)); // 15 * 120.00 = 1800
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: Create delivery")
    void step6_createDelivery() throws Exception {
        String deliveryRequest = String.format("""
                {
                    "deliveryNumber": "DEL-TEST-001",
                    "deliveryOrderId": %d,
                    "vehicle": "Truck-001",
                    "driver": "John Driver",
                    "plannedDeliveryDate": "2025-11-12",
                    "status": "PLANIFIEE"
                }
                """, deliveryOrderId);

        MvcResult result = mockMvc.perform(post("/api/delivery/deliveries")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deliveryRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.deliveryNumber").value("DEL-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("PLANIFIEE"))
                .andExpect(jsonPath("$.data.driver").value("John Driver"))
                .andReturn();

        deliveryId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(deliveryId);
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Change delivery status to EN_COURS")
    void step7_changeDeliveryStatusToInProgress() throws Exception {
        mockMvc.perform(patch("/api/delivery/deliveries/" + deliveryId + "/status")
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EN_COURS"));
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: Complete delivery (reduce product stock)")
    void step8_completeDelivery() throws Exception {
        mockMvc.perform(patch("/api/delivery/deliveries/" + deliveryId + "/deliver")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LIVREE"));
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Verify product stock decreased")
    void step9_verifyProductStockDecreased() throws Exception {
        // Stock should decrease by 15 units
        // Initial: 100, After: 85
        mockMvc.perform(get("/api/production/products/" + productId)
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(85));
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: Verify delivery order status updated")
    void step10_verifyDeliveryOrderStatus() throws Exception {
        mockMvc.perform(get("/api/delivery/orders/" + deliveryOrderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LIVREE"));
    }

    @Test
    @Order(11)
    @DisplayName("Step 11: Verify complete delivery workflow")
    void step11_verifyCompleteWorkflow() throws Exception {
        // Verify customer
        mockMvc.perform(get("/api/delivery/customers/" + customerId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CUST-TEST-001"));

        // Verify delivery order
        mockMvc.perform(get("/api/delivery/orders/" + deliveryOrderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LIVREE"))
                .andExpect(jsonPath("$.data.totalAmount").value(1800.00));

        // Verify delivery
        mockMvc.perform(get("/api/delivery/deliveries/" + deliveryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LIVREE"))
                .andExpect(jsonPath("$.data.driver").value("John Driver"));

        // Verify product stock
        mockMvc.perform(get("/api/production/products/" + productId)
                        .header("Authorization", "Bearer " + productionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(85));
    }

    // Helper methods
    private String extractToken(String jsonResponse) {
        try {
            int tokenStart = jsonResponse.indexOf("\"token\":\"") + 9;
            int tokenEnd = jsonResponse.indexOf("\"", tokenStart);
            return jsonResponse.substring(tokenStart, tokenEnd);
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractId(String jsonResponse) {
        try {
            int idStart = jsonResponse.indexOf("\"id\":") + 5;
            int idEnd = jsonResponse.indexOf(",", idStart);
            if (idEnd == -1) {
                idEnd = jsonResponse.indexOf("}", idStart);
            }
            return Long.parseLong(jsonResponse.substring(idStart, idEnd).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
