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
 * Tests d'intégration E2E pour le workflow complet du module Production
 * 
 * Scénario testé:
 * 1. Authentification d'un chef de production
 * 2. Création de matières premières (setup)
 * 3. Création d'un produit
 * 4. Création d'une nomenclature (BOM) pour le produit
 * 5. Création d'un ordre de production
 * 6. Démarrage de la production (réduction stock matières)
 * 7. Finalisation de la production (augmentation stock produit)
 * 8. Vérification des stocks
 */
@DisplayName("Integration Tests - Production Workflow E2E")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductionWorkflowIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String authToken;
    private static String supplyToken;
    private static Long rawMaterialId;
    private static Long productId;
    private static Long bomId;
    private static Long productionOrderId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Authenticate as production manager")
    void step1_authenticateAsProductionManager() throws Exception {
        String loginRequest = """
                {
                    "username": "production_manager",
                    "password": "password123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CHEF_PRODUCTION"))
                .andReturn();

        authToken = extractToken(result.getResponse().getContentAsString());
        Assertions.assertNotNull(authToken);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Setup - Create raw material (as supply manager)")
    void step2_setupRawMaterial() throws Exception {
        // Login as supply manager
        String loginRequest = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        supplyToken = extractToken(loginResult.getResponse().getContentAsString());

        // Create raw material
        String materialRequest = """
                {
                    "code": "RM-PROD-TEST-001",
                    "name": "Wood for Production Test",
                    "category": "Wood",
                    "unit": "kg",
                    "unitPrice": 20.00,
                    "stock": 1000,
                    "stockMin": 100,
                    "supplierIds": []
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/raw-materials")
                        .header("Authorization", "Bearer " + supplyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("RM-PROD-TEST-001"))
                .andExpect(jsonPath("$.data.stock").value(1000))
                .andReturn();

        rawMaterialId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(rawMaterialId);
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Create a product")
    void step3_createProduct() throws Exception {
        String productRequest = """
                {
                    "code": "PROD-TEST-001",
                    "name": "Test Wooden Chair",
                    "description": "A test chair for integration tests",
                    "category": "Furniture",
                    "unit": "piece",
                    "productionCost": 45.50,
                    "sellingPrice": 99.99,
                    "productionTime": 120,
                    "stock": 50,
                    "stockMin": 10
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/production/products")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("PROD-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Wooden Chair"))
                .andExpect(jsonPath("$.data.stock").value(50))
                .andReturn();

        productId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(productId);
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Create BOM (Bill of Materials)")
    void step4_createBillOfMaterials() throws Exception {
        String bomRequest = String.format("""
                {
                    "productId": %d,
                    "rawMaterialId": %d,
                    "quantity": 5.0,
                    "unit": "kg"
                }
                """, productId, rawMaterialId);

        MvcResult result = mockMvc.perform(post("/api/production/bills-of-material")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bomRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(5.0))
                .andReturn();

        bomId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(bomId);
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Verify BOM for product")
    void step5_verifyBOMForProduct() throws Exception {
        mockMvc.perform(get("/api/production/bills-of-material/product/" + productId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].quantity").value(5.0));
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: Create production order")
    void step6_createProductionOrder() throws Exception {
        String orderRequest = String.format("""
                {
                    "orderNumber": "PO-TEST-001",
                    "productId": %d,
                    "quantity": 10,
                    "plannedStartDate": "2025-11-09",
                    "plannedEndDate": "2025-11-10",
                    "priority": "NORMAL",
                    "status": "EN_ATTENTE"
                }
                """, productId);

        MvcResult result = mockMvc.perform(post("/api/production/production-orders")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value("PO-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andReturn();

        productionOrderId = extractId(result.getResponse().getContentAsString());
        Assertions.assertNotNull(productionOrderId);
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Start production (reduce raw material stock)")
    void step7_startProduction() throws Exception {
        mockMvc.perform(patch("/api/production/production-orders/" + productionOrderId + "/start")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EN_PRODUCTION"));
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: Verify raw material stock decreased")
    void step8_verifyRawMaterialStockDecreased() throws Exception {
        // Stock should decrease by: 10 chairs * 5 kg/chair = 50 kg
        // Initial: 1000 kg, After: 950 kg
        mockMvc.perform(get("/api/raw-materials/" + rawMaterialId)
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(950));
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Complete production (increase product stock)")
    void step9_completeProduction() throws Exception {
        mockMvc.perform(patch("/api/production/production-orders/" + productionOrderId + "/complete")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TERMINEE"));
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: Verify product stock increased")
    void step10_verifyProductStockIncreased() throws Exception {
        // Stock should increase by: 10 chairs
        // Initial: 50, After: 60
        mockMvc.perform(get("/api/production/products/" + productId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(60));
    }

    @Test
    @Order(11)
    @DisplayName("Step 11: Verify complete production workflow")
    void step11_verifyCompleteWorkflow() throws Exception {
        // Verify product
        mockMvc.perform(get("/api/production/products/" + productId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("PROD-TEST-001"))
                .andExpect(jsonPath("$.data.stock").value(60));

        // Verify BOM
        mockMvc.perform(get("/api/production/bills-of-material/" + bomId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(5.0));

        // Verify production order
        mockMvc.perform(get("/api/production/production-orders/" + productionOrderId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TERMINEE"))
                .andExpect(jsonPath("$.data.quantity").value(10));

        // Verify raw material stock
        mockMvc.perform(get("/api/raw-materials/" + rawMaterialId)
                        .header("Authorization", "Bearer " + supplyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(950));
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
