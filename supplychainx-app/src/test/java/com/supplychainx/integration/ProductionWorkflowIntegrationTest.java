package com.supplychainx.integration;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.production.dto.request.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.request.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.response.BillOfMaterialResponseDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.dto.response.ProductionOrderResponseDTO;
import com.supplychainx.production.enums.Priority;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.service.BillOfMaterialService;
import com.supplychainx.production.service.ProductService;
import com.supplychainx.production.service.ProductionOrderService;
import com.supplychainx.supply.dto.request.RawMaterialRequestDTO;
import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.service.RawMaterialService;
import com.supplychainx.supply.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for complete Production workflow:
 * 1. Create Supplier + RawMaterial
 * 2. Create Product
 * 3. Create Bill of Material (link product to raw material)
 * 4. Create Production Order
 * 5. Start Production (verify material stock decreases)
 * 6. Complete Production (verify product stock increases)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductionWorkflowIntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private RawMaterialService rawMaterialService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BillOfMaterialService billOfMaterialService;

    @Autowired
    private ProductionOrderService productionOrderService;

    private Long supplierId;
    private Long rawMaterialId;
    private Long productId;

    @BeforeEach
    void setupTestData() {
        // 1. Create a supplier
        SupplierRequestDTO supplierRequest = SupplierRequestDTO.builder()
                .code("SUP-TEST-001")
                .name("Test Supplier")
                .contact("John Doe")
                .phone("0600000000")
                .email("test@supplier.com")
                .address("123 Test Street")
                .build();
        SupplierResponseDTO supplier = supplierService.create(supplierRequest);
        supplierId = supplier.getId();

        // 2. Create a raw material with sufficient stock
        RawMaterialRequestDTO rawMaterialRequest = RawMaterialRequestDTO.builder()
                .code("RM-TEST-001")
                .name("Test Raw Material")
                .stock(1000)
                .stockMin(100)
                .unit("kg")
                .unitPrice(50.0)
                .category("Test Category")
                .supplierIds(Collections.singletonList(supplierId))
                .build();
        RawMaterialResponseDTO rawMaterial = rawMaterialService.create(rawMaterialRequest);
        rawMaterialId = rawMaterial.getId();

        // 3. Create a product
        ProductRequestDTO productRequest = ProductRequestDTO.builder()
                .code("PROD-TEST-001")
                .name("Test Product")
                .description("A test product for integration testing")
                .category("Test Category")
                .productionTime(120)
                .cost(100.0)
                .stock(0.0)
                .stockMin(10.0)
                .build();
        ProductResponseDTO product = productService.createProduct(productRequest);
        productId = product.getId();
    }

    @Test
    void testCompleteProductionWorkflow_success() {
        // 4. Create Bill of Material (recipe: 1 product = 2.5 kg raw material)
        BillOfMaterialRequestDTO bomRequest = BillOfMaterialRequestDTO.builder()
                .productId(productId)
                .rawMaterialId(rawMaterialId)
                .quantity(2.5)
                .unit("kg")
                .build();
        BillOfMaterialResponseDTO bom = billOfMaterialService.createBillOfMaterial(bomRequest);

        assertNotNull(bom);
        assertNotNull(bom.getId());
        assertEquals(2.5, bom.getQuantity());
        assertEquals(125.0, bom.getLineCost()); // 2.5 * 50.0 = 125.0

        // 5. Create Production Order (quantity: 10 products)
        ProductionOrderRequestDTO orderRequest = ProductionOrderRequestDTO.builder()
                .orderNumber("PO-TEST-001")
                .productId(productId)
                .quantity(10)
                .priority(Priority.URGENT)
                .build();
        ProductionOrderResponseDTO order = productionOrderService.createProductionOrder(orderRequest);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals("PO-TEST-001", order.getOrderNumber());
        assertEquals(10, order.getQuantity());
        assertEquals(ProductionOrderStatus.EN_ATTENTE, order.getStatus());

        // 6. Start Production
        // Required materials: 10 products * 2.5 kg = 25 kg
        // Available stock: 1000 kg → sufficient
        RawMaterialResponseDTO rawMaterialBefore = rawMaterialService.findById(rawMaterialId);
        assertEquals(1000, rawMaterialBefore.getStock());

        ProductionOrderResponseDTO startedOrder = productionOrderService.startProduction(order.getId());

        assertNotNull(startedOrder);
        assertEquals(ProductionOrderStatus.EN_PRODUCTION, startedOrder.getStatus());
        assertNotNull(startedOrder.getStartDate());

        // 7. Complete Production
        // Should consume 25 kg of raw material
        // Should add 10 products to stock
        ProductResponseDTO productBefore = productService.getProductById(productId);
        assertEquals(0.0, productBefore.getStock());

        ProductionOrderResponseDTO completedOrder = productionOrderService.completeProduction(order.getId());

        assertNotNull(completedOrder);
        assertEquals(ProductionOrderStatus.TERMINE, completedOrder.getStatus());
        assertNotNull(completedOrder.getEndDate());

        // Verify product stock increased
        ProductResponseDTO productAfter = productService.getProductById(productId);
        assertEquals(10.0, productAfter.getStock(), "Product stock should increase by 10");

        // Verify raw material stock decreased
        RawMaterialResponseDTO rawMaterialAfter = rawMaterialService.findById(rawMaterialId);
        assertEquals(975, rawMaterialAfter.getStock(), "Raw material stock should decrease by 25 kg (ceil of 25.0)");
    }

    @Test
    void testProductionWorkflow_insufficientMaterials_fails() {
        // Create a BOM with high material requirement
        BillOfMaterialRequestDTO bomRequest = BillOfMaterialRequestDTO.builder()
                .productId(productId)
                .rawMaterialId(rawMaterialId)
                .quantity(200.0) // 200 kg per product
                .unit("kg")
                .build();
        billOfMaterialService.createBillOfMaterial(bomRequest);

        // Create Production Order for 10 products (requires 2000 kg, but only 1000 available)
        ProductionOrderRequestDTO orderRequest = ProductionOrderRequestDTO.builder()
                .orderNumber("PO-TEST-002")
                .productId(productId)
                .quantity(10)
                .priority(Priority.STANDARD)
                .build();
        ProductionOrderResponseDTO order = productionOrderService.createProductionOrder(orderRequest);

        // Try to start production - should fail due to insufficient materials
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productionOrderService.startProduction(order.getId())
        );

        assertTrue(exception.getMessage().contains("Matières premières insuffisantes"));

        // Verify order status remains EN_ATTENTE
        ProductionOrderResponseDTO unchangedOrder = productionOrderService.getProductionOrderById(order.getId());
        assertEquals(ProductionOrderStatus.EN_ATTENTE, unchangedOrder.getStatus());
    }

    @Test
    void testProductionWorkflow_cannotCompleteNonStartedOrder() {
        // Create BOM and Production Order
        BillOfMaterialRequestDTO bomRequest = BillOfMaterialRequestDTO.builder()
                .productId(productId)
                .rawMaterialId(rawMaterialId)
                .quantity(1.0)
                .unit("kg")
                .build();
        billOfMaterialService.createBillOfMaterial(bomRequest);

        ProductionOrderRequestDTO orderRequest = ProductionOrderRequestDTO.builder()
                .orderNumber("PO-TEST-003")
                .productId(productId)
                .quantity(5)
                .priority(Priority.STANDARD)
                .build();
        ProductionOrderResponseDTO order = productionOrderService.createProductionOrder(orderRequest);

        // Try to complete without starting - should fail
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productionOrderService.completeProduction(order.getId())
        );

        assertTrue(exception.getMessage().contains("Seuls les ordres en production peuvent être terminés"));
    }
}
