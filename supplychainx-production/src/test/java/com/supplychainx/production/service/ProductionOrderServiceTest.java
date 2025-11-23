package com.supplychainx.production.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductionOrderService Tests")
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
    private ProductionOrder sampleOrder;
    private ProductionOrderRequestDTO requestDTO;
    private ProductionOrderResponseDTO responseDTO;
    private RawMaterial rawMaterial;
    private BillOfMaterial bom;

    @BeforeEach
    void setup() {
        sampleProduct = Product.builder()
                .code("P-001")
                .name("Sample Product")
                .stock(10.0)
                .build();
        sampleProduct.setId(1L);

        sampleOrder = ProductionOrder.builder()
                .orderNumber("PO-100")
                .quantity(5)
                .status(ProductionOrderStatus.EN_ATTENTE)
                .product(sampleProduct)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .estimatedTime(120)
                .build();
        sampleOrder.setId(10L);

        requestDTO = ProductionOrderRequestDTO.builder()
                .orderNumber("PO-100")
                .productId(1L)
                .quantity(5)
                .build();

        responseDTO = new ProductionOrderResponseDTO();
        responseDTO.setId(10L);
        responseDTO.setOrderNumber("PO-100");
        responseDTO.setQuantity(5);

        rawMaterial = RawMaterial.builder()
                .code("RM-001")
                .name("Steel")
                .stock(100)
                .unit("kg")
                .build();
        rawMaterial.setId(2L);

        bom = BillOfMaterial.builder()
                .product(sampleProduct)
                .rawMaterial(rawMaterial)
                .quantity(2.0)
                .unit("kg")
                .build();
        bom.setId(5L);
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("Should create production order successfully")
    void createProductionOrder_success() {
        when(productionOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productionOrderMapper.toEntity(requestDTO)).thenReturn(sampleOrder);
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(sampleOrder);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.createProductionOrder(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("PO-100", result.getOrderNumber());
        verify(productionOrderRepository).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when order number already exists")
    void createProductionOrder_duplicateOrderNumber_throws() {
        when(productionOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())).thenReturn(true);

        assertThatThrownBy(() -> productionOrderService.createProductionOrder(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");

        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void createProductionOrder_productNotFound_throws() {
        when(productionOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionOrderService.createProductionOrder(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produit non trouvé");

        verify(productionOrderRepository, never()).save(any());
    }

    // ========== GET TESTS ==========

    @Test
    @DisplayName("Should get production order by ID")
    void getProductionOrderById_success() {
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.getProductionOrderById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(productionOrderRepository).findById(10L);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void getProductionOrderById_notFound_throws() {
        when(productionOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionOrderService.getProductionOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("non trouvé");
    }

    @Test
    @DisplayName("Should get production order by order number")
    void getProductionOrderByOrderNumber_success() {
        when(productionOrderRepository.findByOrderNumber("PO-100")).thenReturn(Optional.of(sampleOrder));
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.getProductionOrderByOrderNumber("PO-100");

        assertNotNull(result);
        assertEquals("PO-100", result.getOrderNumber());
    }

    @Test
    @DisplayName("Should get all production orders with pagination")
    void getAllProductionOrders_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductionOrder> page = new PageImpl<>(Collections.singletonList(sampleOrder));
        
        when(productionOrderRepository.findAll(pageable)).thenReturn(page);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        Page<ProductionOrderResponseDTO> result = productionOrderService.getAllProductionOrders(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should get production orders by status")
    void getProductionOrdersByStatus_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductionOrder> page = new PageImpl<>(Collections.singletonList(sampleOrder));
        
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.EN_ATTENTE, pageable)).thenReturn(page);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        Page<ProductionOrderResponseDTO> result = productionOrderService.getProductionOrdersByStatus(
                ProductionOrderStatus.EN_ATTENTE, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get production orders by product")
    void getProductionOrdersByProduct_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductionOrder> page = new PageImpl<>(Collections.singletonList(sampleOrder));
        
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productionOrderRepository.findByProductId(1L, pageable)).thenReturn(page);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        Page<ProductionOrderResponseDTO> result = productionOrderService.getProductionOrdersByProduct(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when product not found for orders query")
    void getProductionOrdersByProduct_productNotFound_throws() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productionOrderService.getProductionOrdersByProduct(999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get delayed production orders")
    void getDelayedProductionOrders_success() {
        ProductionOrder delayedOrder = ProductionOrder.builder()
                .orderNumber("PO-LATE")
                .status(ProductionOrderStatus.EN_PRODUCTION)
                .endDate(LocalDate.now().minusDays(2))
                .build();
        
        when(productionOrderRepository.findByStatusAndEndDateBefore(
                eq(ProductionOrderStatus.EN_PRODUCTION), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(delayedOrder));
        when(productionOrderMapper.toResponseDTOList(anyList()))
                .thenReturn(Collections.singletonList(responseDTO));

        List<ProductionOrderResponseDTO> result = productionOrderService.getDelayedProductionOrders();

        assertThat(result).hasSize(1);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("Should update production order successfully")
    void updateProductionOrder_success() {
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productionOrderRepository.save(sampleOrder)).thenReturn(sampleOrder);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.updateProductionOrder(10L, requestDTO);

        assertNotNull(result);
        verify(productionOrderMapper).updateEntityFromDTO(requestDTO, sampleOrder);
        verify(productionOrderRepository).save(sampleOrder);
    }

    @Test
    @DisplayName("Should throw exception when updating terminated order")
    void updateProductionOrder_terminated_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.TERMINE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.updateProductionOrder(10L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("terminé ou annulé");

        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating cancelled order")
    void updateProductionOrder_cancelled_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.ANNULE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.updateProductionOrder(10L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("terminé ou annulé");
    }

    @Test
    @DisplayName("Should throw exception when new order number already exists")
    void updateProductionOrder_duplicateNewOrderNumber_throws() {
        requestDTO.setOrderNumber("PO-200");
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(productionOrderRepository.existsByOrderNumber("PO-200")).thenReturn(true);

        assertThatThrownBy(() -> productionOrderService.updateProductionOrder(10L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    // ========== START PRODUCTION TESTS ==========

    @Test
    @DisplayName("Should start production successfully")
    void startProduction_success() {
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(billOfMaterialRepository.findByProductId(1L)).thenReturn(Collections.singletonList(bom));
        when(productionOrderRepository.save(sampleOrder)).thenReturn(sampleOrder);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.startProduction(10L);

        assertNotNull(result);
        assertEquals(ProductionOrderStatus.EN_PRODUCTION, sampleOrder.getStatus());
        assertNotNull(sampleOrder.getStartDate());
        verify(productionOrderRepository).save(sampleOrder);
    }

    @Test
    @DisplayName("Should throw exception when starting non-pending order")
    void startProduction_notPending_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.startProduction(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("en attente");

        verify(productionOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when insufficient raw materials")
    void startProduction_insufficientMaterials_throws() {
        rawMaterial.setStock(5); // Not enough for quantity 5 * 2.0 = 10kg required
        
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(billOfMaterialRepository.findByProductId(1L)).thenReturn(Collections.singletonList(bom));

        assertThatThrownBy(() -> productionOrderService.startProduction(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Matières premières insuffisantes");

        verify(productionOrderRepository, never()).save(any());
    }

    // ========== COMPLETE PRODUCTION TESTS ==========

    @Test
    @DisplayName("Should complete production successfully")
    void completeProduction_success() {
        sampleOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(productionOrderRepository.save(sampleOrder)).thenReturn(sampleOrder);
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.completeProduction(10L);

        assertNotNull(result);
        assertEquals(ProductionOrderStatus.TERMINE, sampleOrder.getStatus());
        assertNotNull(sampleOrder.getEndDate());
        assertEquals(15.0, sampleProduct.getStock()); // 10 + 5
        verify(productRepository).save(sampleProduct);
        // Raw materials are consumed during startProduction, not completeProduction
        verify(rawMaterialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when completing non-production order")
    void completeProduction_notInProduction_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.completeProduction(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("en production");
    }

    // ========== CANCEL TESTS ==========

    @Test
    @DisplayName("Should cancel production order successfully")
    void cancelProductionOrder_success() {
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(productionOrderRepository.save(sampleOrder)).thenReturn(sampleOrder);
        when(productionOrderMapper.toResponseDTO(sampleOrder)).thenReturn(responseDTO);

        ProductionOrderResponseDTO result = productionOrderService.cancelProductionOrder(10L);

        assertNotNull(result);
        assertEquals(ProductionOrderStatus.ANNULE, sampleOrder.getStatus());
        verify(productionOrderRepository).save(sampleOrder);
    }

    @Test
    @DisplayName("Should throw exception when cancelling terminated order")
    void cancelProductionOrder_alreadyTerminated_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.TERMINE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.cancelProductionOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("terminé");
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled order")
    void cancelProductionOrder_alreadyCancelled_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.ANNULE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.cancelProductionOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà annulé");
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("Should delete production order successfully")
    void deleteProductionOrder_success() {
        sampleOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        productionOrderService.deleteProductionOrder(10L);

        verify(productionOrderRepository).delete(sampleOrder);
    }

    @Test
    @DisplayName("Should throw exception when deleting order in production")
    void deleteProductionOrder_inProduction_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.deleteProductionOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("en cours ou terminé");

        verify(productionOrderRepository, never()).delete(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting terminated order")
    void deleteProductionOrder_terminated_throws() {
        sampleOrder.setStatus(ProductionOrderStatus.TERMINE);
        when(productionOrderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> productionOrderService.deleteProductionOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("en cours ou terminé");
    }
}
