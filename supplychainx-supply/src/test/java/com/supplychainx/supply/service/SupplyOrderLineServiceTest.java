    package com.supplychainx.supply.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderLineResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.entity.SupplyOrder;
import com.supplychainx.supply.entity.SupplyOrderLine;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import com.supplychainx.supply.mapper.SupplyOrderLineMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplyOrderLineRepository;
import com.supplychainx.supply.repository.SupplyOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplyOrderLineService Tests")
class SupplyOrderLineServiceTest {

    @Mock
    private SupplyOrderLineRepository supplyOrderLineRepository;

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private SupplyOrderLineMapper supplyOrderLineMapper;

    @InjectMocks
    private SupplyOrderLineService supplyOrderLineService;

    private SupplyOrder testOrder;
    private RawMaterial testMaterial;
    private SupplyOrderLine testOrderLine;
    private SupplyOrderLineRequestDTO requestDTO;
    private SupplyOrderLineResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testOrder = new SupplyOrder();
        testOrder.setId(1L);
        testOrder.setOrderNumber("SO-001");
        testOrder.setStatus(SupplyOrderStatus.EN_ATTENTE);

        testMaterial = new RawMaterial();
        testMaterial.setId(1L);
        testMaterial.setCode("RM-001");
        testMaterial.setName("Test Material");

        testOrderLine = new SupplyOrderLine();
        testOrderLine.setId(1L);
        testOrderLine.setSupplyOrder(testOrder);
        testOrderLine.setMaterial(testMaterial);
        testOrderLine.setQuantity(100);
        testOrderLine.setUnitPrice(10.0);

        requestDTO = new SupplyOrderLineRequestDTO();
        requestDTO.setMaterialId(1L);
        requestDTO.setQuantity(100);
        requestDTO.setUnitPrice(10.0);

        responseDTO = new SupplyOrderLineResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setQuantity(100);
        responseDTO.setUnitPrice(10.0);
    }

    @Test
    @DisplayName("Should create order line successfully")
    void shouldCreateOrderLineSuccessfully() {
        // Given
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(testMaterial));
        when(supplyOrderLineMapper.toEntity(requestDTO)).thenReturn(testOrderLine);
        when(supplyOrderLineRepository.save(any(SupplyOrderLine.class))).thenReturn(testOrderLine);
        when(supplyOrderLineMapper.toResponseDTO(testOrderLine)).thenReturn(responseDTO);

        // When
        SupplyOrderLineResponseDTO result = supplyOrderLineService.create(1L, requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(supplyOrderRepository).findById(1L);
        verify(rawMaterialRepository).findById(1L);
        verify(supplyOrderLineRepository).save(any(SupplyOrderLine.class));
    }

    @Test
    @DisplayName("Should throw exception when creating line for non-existent order")
    void shouldThrowExceptionWhenCreatingLineForNonExistentOrder() {
        // Given
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.create(1L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Commande non trouvée");

        verify(supplyOrderRepository).findById(1L);
        verify(supplyOrderLineRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating line for received order")
    void shouldThrowExceptionWhenCreatingLineForReceivedOrder() {
        // Given
        testOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.create(1L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible d'ajouter une ligne");

        verify(supplyOrderLineRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating line for cancelled order")
    void shouldThrowExceptionWhenCreatingLineForCancelledOrder() {
        // Given
        testOrder.setStatus(SupplyOrderStatus.ANNULEE);
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.create(1L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible d'ajouter une ligne");
    }

    @Test
    @DisplayName("Should throw exception when creating line with non-existent material")
    void shouldThrowExceptionWhenCreatingLineWithNonExistentMaterial() {
        // Given
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.create(1L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Matière première non trouvée");
    }

    @Test
    @DisplayName("Should update order line successfully")
    void shouldUpdateOrderLineSuccessfully() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));
        when(supplyOrderLineRepository.save(testOrderLine)).thenReturn(testOrderLine);
        when(supplyOrderLineMapper.toResponseDTO(testOrderLine)).thenReturn(responseDTO);

        // When
        SupplyOrderLineResponseDTO result = supplyOrderLineService.update(1L, requestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(supplyOrderLineRepository).findById(1L);
        verify(supplyOrderLineMapper).updateEntityFromDTO(requestDTO, testOrderLine);
        verify(supplyOrderLineRepository).save(testOrderLine);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent line")
    void shouldThrowExceptionWhenUpdatingNonExistentLine() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.update(1L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ligne de commande non trouvée");
    }

    @Test
    @DisplayName("Should throw exception when updating line for received order")
    void shouldThrowExceptionWhenUpdatingLineForReceivedOrder() {
        // Given
        testOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.update(1L, requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible de modifier une ligne");
    }

    @Test
    @DisplayName("Should update material when material ID changes")
    void shouldUpdateMaterialWhenMaterialIdChanges() {
        // Given
        RawMaterial newMaterial = new RawMaterial();
        newMaterial.setId(2L);
        newMaterial.setCode("RM-002");
        
        requestDTO.setMaterialId(2L);
        
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));
        when(rawMaterialRepository.findById(2L)).thenReturn(Optional.of(newMaterial));
        when(supplyOrderLineRepository.save(testOrderLine)).thenReturn(testOrderLine);
        when(supplyOrderLineMapper.toResponseDTO(testOrderLine)).thenReturn(responseDTO);

        // When
        SupplyOrderLineResponseDTO result = supplyOrderLineService.update(1L, requestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(rawMaterialRepository).findById(2L);
        assertThat(testOrderLine.getMaterial()).isEqualTo(newMaterial);
    }

    @Test
    @DisplayName("Should delete order line successfully")
    void shouldDeleteOrderLineSuccessfully() {
        // Given
        SupplyOrderLine anotherLine = new SupplyOrderLine();
        anotherLine.setId(2L);
        
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));
        when(supplyOrderLineRepository.findBySupplyOrderId(1L))
                .thenReturn(Arrays.asList(testOrderLine, anotherLine));

        // When
        supplyOrderLineService.delete(1L);

        // Then
        verify(supplyOrderLineRepository).delete(testOrderLine);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent line")
    void shouldThrowExceptionWhenDeletingNonExistentLine() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ligne de commande non trouvée");
    }

    @Test
    @DisplayName("Should throw exception when deleting line from received order")
    void shouldThrowExceptionWhenDeletingLineFromReceivedOrder() {
        // Given
        testOrder.setStatus(SupplyOrderStatus.RECUE);
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible de supprimer une ligne");
    }

    @Test
    @DisplayName("Should throw exception when deleting last line")
    void shouldThrowExceptionWhenDeletingLastLine() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));
        when(supplyOrderLineRepository.findBySupplyOrderId(1L))
                .thenReturn(Collections.singletonList(testOrderLine));

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible de supprimer la dernière ligne");
    }

    @Test
    @DisplayName("Should find order line by ID")
    void shouldFindOrderLineById() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.of(testOrderLine));
        when(supplyOrderLineMapper.toResponseDTO(testOrderLine)).thenReturn(responseDTO);

        // When
        SupplyOrderLineResponseDTO result = supplyOrderLineService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when finding non-existent line by ID")
    void shouldThrowExceptionWhenFindingNonExistentLineById() {
        // Given
        when(supplyOrderLineRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find lines by supply order")
    void shouldFindLinesBySupplyOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderLineRepository.findBySupplyOrderId(1L))
                .thenReturn(Collections.singletonList(testOrderLine));
        when(supplyOrderLineMapper.toResponseDTOList(anyList()))
                .thenReturn(Collections.singletonList(responseDTO));

        // When
        List<SupplyOrderLineResponseDTO> result = supplyOrderLineService.findBySupplyOrder(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when finding lines for non-existent order")
    void shouldThrowExceptionWhenFindingLinesForNonExistentOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.findBySupplyOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find lines by material")
    void shouldFindLinesByMaterial() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderLineRepository.findByMaterialId(1L))
                .thenReturn(Collections.singletonList(testOrderLine));
        when(supplyOrderLineMapper.toResponseDTOList(anyList()))
                .thenReturn(Collections.singletonList(responseDTO));

        // When
        List<SupplyOrderLineResponseDTO> result = supplyOrderLineService.findByMaterial(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when finding lines for non-existent material")
    void shouldThrowExceptionWhenFindingLinesForNonExistentMaterial() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.findByMaterial(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find line by supply order and material")
    void shouldFindLineBySupplyOrderAndMaterial() {
        // Given
        when(supplyOrderLineRepository.findBySupplyOrderIdAndMaterialId(1L, 1L))
                .thenReturn(Collections.singletonList(testOrderLine));
        when(supplyOrderLineMapper.toResponseDTO(testOrderLine)).thenReturn(responseDTO);

        // When
        SupplyOrderLineResponseDTO result = supplyOrderLineService.findBySupplyOrderAndMaterial(1L, 1L);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when no line found for order and material")
    void shouldThrowExceptionWhenNoLineFoundForOrderAndMaterial() {
        // Given
        when(supplyOrderLineRepository.findBySupplyOrderIdAndMaterialId(1L, 1L))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.findBySupplyOrderAndMaterial(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should sum quantity by material in active orders")
    void shouldSumQuantityByMaterialInActiveOrders() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderLineRepository.sumQuantityByMaterialInActiveOrders(1L)).thenReturn(500);

        // When
        Integer result = supplyOrderLineService.sumQuantityByMaterialInActiveOrders(1L);

        // Then
        assertThat(result).isEqualTo(500);
    }

    @Test
    @DisplayName("Should throw exception when summing quantity for non-existent material")
    void shouldThrowExceptionWhenSummingQuantityForNonExistentMaterial() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.sumQuantityByMaterialInActiveOrders(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should sum total amount by order")
    void shouldSumTotalAmountByOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderLineRepository.sumTotalAmountByOrder(1L)).thenReturn(1000.0);

        // When
        Double result = supplyOrderLineService.sumTotalAmountByOrder(1L);

        // Then
        assertThat(result).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Should throw exception when summing amount for non-existent order")
    void shouldThrowExceptionWhenSummingAmountForNonExistentOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.sumTotalAmountByOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should check if material has active order lines")
    void shouldCheckIfMaterialHasActiveOrderLines() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderLineRepository.materialHasActiveOrderLines(1L)).thenReturn(true);

        // When
        boolean result = supplyOrderLineService.materialHasActiveOrderLines(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when checking active lines for non-existent material")
    void shouldThrowExceptionWhenCheckingActiveLinesForNonExistentMaterial() {
        // Given
        when(rawMaterialRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.materialHasActiveOrderLines(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete all lines by supply order")
    void shouldDeleteAllLinesBySupplyOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(true);

        // When
        supplyOrderLineService.deleteBySupplyOrder(1L);

        // Then
        verify(supplyOrderLineRepository).deleteBySupplyOrderId(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting lines for non-existent order")
    void shouldThrowExceptionWhenDeletingLinesForNonExistentOrder() {
        // Given
        when(supplyOrderRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> supplyOrderLineService.deleteBySupplyOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
