package com.supplychainx.integration.workflow;

import com.jayway.jsonpath.JsonPath;
import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integration Tests - Supply Workflow E2E")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupplyWorkflowIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String supplyManagerToken; 
    private String purchaseManagerToken; 
    private Long supplierId;
    private Long materialId;
    private Long supplyOrderId;
    private Long orderLineId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Authenticate as supply manager (for supplier/material setup)")
    void step1_authenticateAsSupplyManager() throws Exception {
        String loginRequest = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("GESTIONNAIRE_APPROVISIONNEMENT"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        supplyManagerToken = JsonPath.read(response, "$.token");
        
        Assertions.assertNotNull(supplyManagerToken, "Supply manager token should not be null");
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
                        .header("Authorization", "Bearer " + supplyManagerToken)
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
                        .header("Authorization", "Bearer " + supplyManagerToken)
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
    @DisplayName("Step 4: Authenticate as purchase manager (for orders)")
    void step4_authenticateAsPurchaseManager() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "purchase_manager",
                    "password": "password123"
                }
                """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("RESPONSABLE_ACHATS"))
                .andReturn();

        // Extract token using JsonPath
        String response = result.getResponse().getContentAsString();
        purchaseManagerToken = JsonPath.read(response, "$.token");
        
        Assertions.assertNotNull(purchaseManagerToken, "Purchase manager token should not be null");
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Create a supply order with order line")
    void step5_createSupplyOrder() throws Exception {
        // Given - Supply order must include at least one order line
        String orderRequest = String.format("""
                {
                    "orderNumber": "SO-TEST-001",
                    "supplierId": %d,
                    "orderDate": "2025-11-09",
                    "expectedDeliveryDate": "2025-11-16",
                    "status": "EN_ATTENTE",
                    "orderLines": [
                        {
                            "materialId": %d,
                            "quantity": 200,
                            "unitPrice": 15.50
                        }
                    ]
                }
                """, supplierId, materialId);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/supply-orders")
                        .header("Authorization", "Bearer " + purchaseManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value("SO-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.data.orderLines").isArray())
                .andExpect(jsonPath("$.data.orderLines[0].quantity").value(200))
                .andReturn();

        // Extract order ID
        String response = result.getResponse().getContentAsString();
        supplyOrderId = extractId(response);
        
        Assertions.assertNotNull(supplyOrderId, "Supply order ID should not be null");
        
        // Extract order line ID from the first line
        try {
            Integer lineId = JsonPath.read(response, "$.data.orderLines[0].id");
            orderLineId = lineId != null ? lineId.longValue() : null;
        } catch (Exception e) {
            orderLineId = null;
        }
        

    }

    @Test
    @Order(6)
    @DisplayName("Step 6: Verify order line was created via total amount")
    void step6_verifyOrderLine() throws Exception {
        // Verify via total amount endpoint (doesn't need orderLineId)
        mockMvc.perform(get("/api/supply-order-lines/order/" + supplyOrderId + "/total-amount")
                        .header("Authorization", "Bearer " + purchaseManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3100.0)); // 200 * 15.50 = 3100
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Change order status to IN_PROGRESS")
    void step7_changeOrderStatusToInProgress() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/supply-orders/" + supplyOrderId + "/status")
                        .header("Authorization", "Bearer " + purchaseManagerToken)
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
                        .header("Authorization", "Bearer " + purchaseManagerToken)
                        .param("actualDeliveryDate", "2025-11-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECUE"));
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Verify stock increased after reception")
    void step9_verifyStockIncreased() throws Exception {
        // Verify that the material stock was increased by the order quantity (200)
        mockMvc.perform(get("/api/raw-materials/" + materialId)
                        .header("Authorization", "Bearer " + purchaseManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(300)); // Initial 100 + received 200 = 300
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Verify complete workflow")
    void step9_verifyCompleteWorkflow() throws Exception {
        // Verify supplier exists
        mockMvc.perform(get("/api/suppliers/" + supplierId)
                        .header("Authorization", "Bearer " + purchaseManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SUP-TEST-001"));

        // Verify material exists with updated stock
        mockMvc.perform(get("/api/raw-materials/" + materialId)
                        .header("Authorization", "Bearer " + purchaseManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(300));

        // Verify order is received with proper orderLines
        mockMvc.perform(get("/api/supply-orders/" + supplyOrderId)
                        .header("Authorization", "Bearer " + purchaseManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECUE"))
                .andExpect(jsonPath("$.data.orderLines[0].subtotal").value(3100.0));
    }

    // Helper methods
    private Long extractId(String jsonResponse) {
        try {
            Integer id = JsonPath.read(jsonResponse, "$.data.id");
            return id != null ? id.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
