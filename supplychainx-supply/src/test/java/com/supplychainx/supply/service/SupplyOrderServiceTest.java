package com.supplychainx.supply.service;

import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.request.SupplyOrderRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.entity.Supplier;
import com.supplychainx.supply.entity.SupplyOrder;
import com.supplychainx.supply.entity.SupplyOrderLine;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import com.supplychainx.supply.mapper.SupplyOrderMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplierRepository;
import com.supplychainx.supply.repository.SupplyOrderRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplyOrderService Unit Tests")
class SupplyOrderServiceTest {

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private SupplyOrderMapper supplyOrderMapper;

    @InjectMocks
    private SupplyOrderService supplyOrderService;

    private SupplyOrderRequestDTO requestDTO;
    private SupplyOrder supplyOrder;
    private SupplyOrderResponseDTO responseDTO;
    private Supplier supplier;
    private RawMaterial rawMaterial;
    private SupplyOrderLine orderLine;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        SupplyOrderLineRequestDTO lineRequestDTO = SupplyOrderLineRequestDTO.builder()
                .materialId(1L)
                .quantity(100)
                .unitPrice(10.0)
                .build();

        requestDTO = SupplyOrderRequestDTO.builder()
                .orderNumber("SO001")
                .supplierId(1L)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .status(SupplyOrderStatus.EN_ATTENTE)
                .orderLines(Arrays.asList(lineRequestDTO))
                .build();

        supplier = Supplier.builder()
                .code("SUP001")
                .name("Test Supplier")
                .build();

        rawMaterial = RawMaterial.builder()
                .code("RM001")
                .name("Steel")
                .stock(100)
                .build();

        supplyOrder = SupplyOrder.builder()
                .orderNumber("SO001")
                .supplier(supplier)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .status(SupplyOrderStatus.EN_ATTENTE)
                .build();

        orderLine = SupplyOrderLine.builder()
                .supplyOrder(supplyOrder)
                .material(rawMaterial)
                .quantity(100)
                .unitPrice(10.0)
                .build();

        // Use ArrayList instead of Arrays.asList for mutable list
        supplyOrder.setOrderLines(new java.util.ArrayList<>(Arrays.asList(orderLine)));

        responseDTO = SupplyOrderResponseDTO.builder()
                .id(1L)
                .orderNumber("SO001")
                .status(SupplyOrderStatus.EN_ATTENTE)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should create supply order successfully")
    void shouldCreateSupplyOrderSuccessfully() {
        when(supplyOrderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplyOrderMapper.toEntity(any(SupplyOrderRequestDTO.class))).thenReturn(supplyOrder);
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.create(requestDTO);

        assertNotNull(result);
        assertEquals("SO001", result.getOrderNumber());
        verify(supplyOrderRepository).existsByOrderNumber("SO001");
        verify(supplierRepository).findById(1L);
        verify(rawMaterialRepository).findById(1L);
        verify(supplyOrderRepository).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when creating order with duplicate number")
    void shouldThrowExceptionWhenCreatingOrderWithDuplicateNumber() {
        when(supplyOrderRepository.existsByOrderNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            supplyOrderService.create(requestDTO);
        });

        verify(supplyOrderRepository).existsByOrderNumber("SO001");
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    void shouldThrowExceptionWhenSupplierNotFound() {
        when(supplyOrderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            supplyOrderService.create(requestDTO);
        });

        verify(supplierRepository).findById(1L);
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should update supply order successfully")
    void shouldUpdateSupplyOrderSuccessfully() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.update(1L, requestDTO);

        assertNotNull(result);
        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderMapper).updateEntityFromDTO(requestDTO, supplyOrder);
        verify(supplyOrderRepository).save(supplyOrder);
    }

    @Test
    @DisplayName("Should throw exception when updating received order")
    void shouldThrowExceptionWhenUpdatingReceivedOrder() {
        supplyOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        assertThrows(BusinessException.class, () -> {
            supplyOrderService.update(1L, requestDTO);
        });

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should delete supply order successfully")
    void shouldDeleteSupplyOrderSuccessfully() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        assertDoesNotThrow(() -> supplyOrderService.delete(1L));

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository).delete(supplyOrder);
    }

    @Test
    @DisplayName("Should throw exception when deleting received order")
    void shouldThrowExceptionWhenDeletingReceivedOrder() {
        supplyOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        assertThrows(BusinessException.class, () -> {
            supplyOrderService.delete(1L);
        });

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository, never()).delete(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should find order by ID successfully")
    void shouldFindOrderByIdSuccessfully() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(supplyOrderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should find order by order number successfully")
    void shouldFindOrderByOrderNumberSuccessfully() {
        when(supplyOrderRepository.findByOrderNumber("SO001")).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.findByOrderNumber("SO001");

        assertNotNull(result);
        assertEquals("SO001", result.getOrderNumber());
        verify(supplyOrderRepository).findByOrderNumber("SO001");
    }

    @Test
    @DisplayName("Should find all orders with pagination")
    void shouldFindAllOrdersWithPagination() {
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, 1);
        List<SupplyOrderResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplyOrderRepository.findAll(pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        PageResponse<SupplyOrderResponseDTO> result = supplyOrderService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(supplyOrderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should find orders by status")
    void shouldFindOrdersByStatus() {
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, 1);
        List<SupplyOrderResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplyOrderRepository.findByStatus(SupplyOrderStatus.EN_ATTENTE, pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplyOrderResponseDTO> result = supplyOrderService.findByStatus(SupplyOrderStatus.EN_ATTENTE, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplyOrderRepository).findByStatus(SupplyOrderStatus.EN_ATTENTE, pageable);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.updateStatus(1L, SupplyOrderStatus.EN_COURS);

        assertNotNull(result);
        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository).save(supplyOrder);
    }

    @Test
    @DisplayName("Should receive order and update stock successfully")
    void shouldReceiveOrderAndUpdateStockSuccessfully() {
        supplyOrder.setStatus(SupplyOrderStatus.EN_COURS);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        LocalDate deliveryDate = LocalDate.now();
        SupplyOrderResponseDTO result = supplyOrderService.receiveOrder(1L, deliveryDate);

        assertNotNull(result);
        verify(supplyOrderRepository).findById(1L);
        verify(rawMaterialRepository).save(rawMaterial);
        verify(supplyOrderRepository).save(supplyOrder);
    }

    @Test
    @DisplayName("Should throw exception when receiving non-in-progress order")
    void shouldThrowExceptionWhenReceivingNonInProgressOrder() {
        supplyOrder.setStatus(SupplyOrderStatus.EN_ATTENTE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        assertThrows(BusinessException.class, () -> {
            supplyOrderService.receiveOrder(1L, LocalDate.now());
        });

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toResponseDTO(any(SupplyOrder.class))).thenReturn(responseDTO);

        SupplyOrderResponseDTO result = supplyOrderService.cancelOrder(1L);

        assertNotNull(result);
        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository).save(supplyOrder);
    }

    @Test
    @DisplayName("Should throw exception when canceling received order")
    void shouldThrowExceptionWhenCancelingReceivedOrder() {
        supplyOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        assertThrows(BusinessException.class, () -> {
            supplyOrderService.cancelOrder(1L);
        });

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should check if order can be deleted")
    void shouldCheckIfOrderCanBeDeleted() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        boolean canBeDeleted = supplyOrderService.canBeDeleted(1L);

        assertTrue(canBeDeleted);
        verify(supplyOrderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return false when checking if received order can be deleted")
    void shouldReturnFalseWhenCheckingIfReceivedOrderCanBeDeleted() {
        supplyOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        boolean canBeDeleted = supplyOrderService.canBeDeleted(1L);

        assertFalse(canBeDeleted);
        verify(supplyOrderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should check if order can be modified")
    void shouldCheckIfOrderCanBeModified() {
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        boolean canBeModified = supplyOrderService.canBeModified(1L);

        assertTrue(canBeModified);
        verify(supplyOrderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return false when checking if received order can be modified")
    void shouldReturnFalseWhenCheckingIfReceivedOrderCanBeModified() {
        supplyOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        boolean canBeModified = supplyOrderService.canBeModified(1L);

        assertFalse(canBeModified);
        verify(supplyOrderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should find orders by date range")
    void shouldFindOrdersByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, 1);
        List<SupplyOrderResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplyOrderRepository.findByOrderDateBetween(startDate, endDate, pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplyOrderResponseDTO> result = supplyOrderService.findByDateRange(startDate, endDate, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplyOrderRepository).findByOrderDateBetween(startDate, endDate, pageable);
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        assertThrows(BusinessException.class, () -> {
            supplyOrderService.findByDateRange(startDate, endDate, pageable);
        });

        verify(supplyOrderRepository, never()).findByOrderDateBetween(any(), any(), any());
    }

    @Test
    @DisplayName("Should count active orders by supplier")
    void shouldCountActiveOrdersBySupplier() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderRepository.countActiveOrdersBySupplier(1L)).thenReturn(5L);

        Long count = supplyOrderService.countActiveOrdersBySupplier(1L);

        assertEquals(5L, count);
        verify(supplierRepository).existsById(1L);
        verify(supplyOrderRepository).countActiveOrdersBySupplier(1L);
    }

    @Test
    @DisplayName("Should sum total amount by status")
    void shouldSumTotalAmountByStatus() {
        when(supplyOrderRepository.sumTotalAmountByStatus(SupplyOrderStatus.EN_ATTENTE)).thenReturn(1000.0);

        Double total = supplyOrderService.sumTotalAmountByStatus(SupplyOrderStatus.EN_ATTENTE);

        assertEquals(1000.0, total);
        verify(supplyOrderRepository).sumTotalAmountByStatus(SupplyOrderStatus.EN_ATTENTE);
    }
}
