package com.supplychainx.production.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.production.dto.request.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.response.ProductionOrderResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.mapper.ProductionOrderMapper;
import com.supplychainx.production.repository.BillOfMaterialRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BillOfMaterialRepository billOfMaterialRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    private Product sampleProduct;

    @BeforeEach
    void setup() {
        sampleProduct = Product.builder()
                .code("P-001")
                .name("Sample Product")
                .stock(0.0)
                .build();
        sampleProduct.setId(1L);
    }

    @Test
    void createProductionOrder_success() {
        ProductionOrderRequestDTO request = ProductionOrderRequestDTO.builder()
                .orderNumber("PO-100")
                .productId(1L)
                .quantity(5)
                .build();

        ProductionOrder mapped = ProductionOrder.builder()
                .orderNumber(request.getOrderNumber())
                .quantity(request.getQuantity())
                .build();
        // id may be assigned by DB on save; not required for mapping

        ProductionOrder saved = ProductionOrder.builder()
                .orderNumber(request.getOrderNumber())
                .quantity(request.getQuantity())
                .status(ProductionOrderStatus.EN_ATTENTE)
                .product(sampleProduct)
                .build();
        saved.setId(10L);

        ProductionOrderResponseDTO responseDTO = new ProductionOrderResponseDTO();
        responseDTO.setId(10L);
        responseDTO.setOrderNumber(request.getOrderNumber());

        when(productionOrderRepository.existsByOrderNumber(request.getOrderNumber())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productionOrderMapper.toEntity(request)).thenReturn(mapped);
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(saved);
        when(productionOrderMapper.toResponseDTO(saved)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.createProductionOrder(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("PO-100", result.getOrderNumber());
    }

    @Test
    void startProduction_insufficientMaterials_throws() {
        // Build an order with quantity such that required materials exceed stock
        ProductionOrder order = ProductionOrder.builder()
                .quantity(10)
                .status(ProductionOrderStatus.EN_ATTENTE)
                .product(sampleProduct)
                .build();
        order.setId(20L);

        // BOM with raw material having zero stock
        RawMaterial raw = RawMaterial.builder()
                .code("RM-1")
                .name("Raw")
                .stock(0)
                .unit("kg")
                .build();
        raw.setId(2L);

        BillOfMaterial bom = BillOfMaterial.builder()
                .product(sampleProduct)
                .rawMaterial(raw)
                .quantity(2.0)
                .unit("kg")
                .build();
        bom.setId(5L);

        when(productionOrderRepository.findById(20L)).thenReturn(Optional.of(order));
        when(billOfMaterialRepository.findByProductId(sampleProduct.getId())).thenReturn(Collections.singletonList(bom));

        BusinessException ex = assertThrows(BusinessException.class, () -> productionOrderService.startProduction(20L));
        assertTrue(ex.getMessage().contains("Matières premières insuffisantes"));
    }
}
