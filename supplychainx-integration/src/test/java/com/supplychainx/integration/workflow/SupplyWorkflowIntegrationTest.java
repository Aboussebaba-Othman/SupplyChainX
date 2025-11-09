package com.supplychainx.integration.workflow;

import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration E2E pour le workflow complet du module Supply
 * 
 * Scénario testé:
 * 1. Authentification d'un gestionnaire d'approvisionnement
 * 2. Création d'un fournisseur
 * 3. Création d'une matière première
 * 4. Création d'une commande d'approvisionnement
 * 5. Ajout de lignes à la commande
 * 6. Passage de la commande en statut EN_COURS
 * 7. Réception de la commande (augmentation du stock)
 * 8. Vérification de la mise à jour du stock
 */
@DisplayName("Integration Tests - Supply Workflow E2E")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SupplyWorkflowIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String authToken;
    private static Long supplierId;
    private static Long materialId;
    private static Long supplyOrderId;
    private static Long orderLineId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Authenticate as supply manager")
    void step1_authenticateAsSupplyManager() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("GESTIONNAIRE_APPROVISIONNEMENT"))
                .andReturn();

        // Extract token
        String response = result.getResponse().getContentAsString();
        authToken = extractToken(response);
        
        Assertions.assertNotNull(authToken, "Auth token should not be null");
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Create a supplier")
    void step2_createSupplier() throws Exception {
        // Given
        String supplierRequest = """
                {
                    "code": "SUP-TEST-001",
                    "name": "Test Supplier Inc.",
                    "email": "test@supplier.com",
                    "phone": "+1234567890",
                    "address": "123 Test Street",
                    "contact": "John Doe",
                    "rating": 4.5,
                    "leadTime": 7
                }
                """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supplierRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("SUP-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Supplier Inc."))
                .andExpect(jsonPath("$.data.rating").value(4.5))
                .andExpect(jsonPath("$.data.leadTime").value(7))
                .andReturn();

        // Extract supplier ID
        String response = result.getResponse().getContentAsString();
        supplierId = extractId(response);
        
        Assertions.assertNotNull(supplierId, "Supplier ID should not be null");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Create a raw material")
    void step3_createRawMaterial() throws Exception {
        // Given
        String materialRequest = String.format("""
                {
                    "code": "RM-TEST-001",
                    "name": "Test Raw Material",
                    "category": "Test Category",
                    "unit": "kg",
                    "unitPrice": 15.50,
                    "stock": 100,
                    "stockMin": 50,
                    "supplierIds": [%d]
                }
                """, supplierId);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/raw-materials")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("RM-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Raw Material"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.unitPrice").value(15.50))
                .andReturn();

        // Extract material ID
        String response = result.getResponse().getContentAsString();
        materialId = extractId(response);
        
        Assertions.assertNotNull(materialId, "Material ID should not be null");
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Create a supply order")
    void step4_createSupplyOrder() throws Exception {
        // Given
        String orderRequest = String.format("""
                {
                    "orderNumber": "SO-TEST-001",
                    "supplierId": %d,
                    "orderDate": "2025-11-09",
                    "expectedDeliveryDate": "2025-11-16",
                    "status": "EN_ATTENTE"
                }
                """, supplierId);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/supply-orders")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value("SO-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("EN_ATTENTE"))
                .andReturn();

        // Extract order ID
        String response = result.getResponse().getContentAsString();
        supplyOrderId = extractId(response);
        
        Assertions.assertNotNull(supplyOrderId, "Supply order ID should not be null");
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Add order line to supply order")
    void step5_addOrderLine() throws Exception {
        // Given
        String orderLineRequest = String.format("""
                {
                    "materialId": %d,
                    "quantity": 200,
                    "unitPrice": 15.50
                }
                """, materialId);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/supply-order-lines/order/" + supplyOrderId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderLineRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(200))
                .andExpect(jsonPath("$.data.unitPrice").value(15.50))
                .andReturn();

        // Extract order line ID
        String response = result.getResponse().getContentAsString();
        orderLineId = extractId(response);
        
        Assertions.assertNotNull(orderLineId, "Order line ID should not be null");
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: Verify order total amount")
    void step6_verifyOrderTotalAmount() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/supply-order-lines/order/" + supplyOrderId + "/total-amount")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3100.0)); // 200 * 15.50 = 3100
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Change order status to EN_COURS")
    void step7_changeOrderStatusToInProgress() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/supply-orders/" + supplyOrderId + "/status")
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EN_COURS"));
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: Receive the supply order (update stock)")
    void step8_receiveSupplyOrder() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/supply-orders/" + supplyOrderId + "/receive")
                        .header("Authorization", "Bearer " + authToken)
                        .param("actualDeliveryDate", "2025-11-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECUE"));
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Verify material stock increased")
    void step9_verifyStockIncreased() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/raw-materials/" + materialId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(300)); // 100 + 200 = 300
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: Verify complete workflow - Get all data")
    void step10_verifyCompleteWorkflow() throws Exception {
        // Verify supplier exists
        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SUP-TEST-001"));

        // Verify material exists with updated stock
        mockMvc.perform(get("/api/raw-materials/" + materialId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(300));

        // Verify order is received
        mockMvc.perform(get("/api/supply-orders/" + supplyOrderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECUE"))
                .andExpect(jsonPath("$.data.totalAmount").value(3100.0));
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
