package com.supplychainx.supply.service;

import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.entity.Supplier;
import com.supplychainx.supply.mapper.SupplierMapper;
import com.supplychainx.supply.repository.SupplierRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Unit Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private SupplierRequestDTO requestDTO;
    private Supplier supplier;
    private SupplierResponseDTO responseDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        requestDTO = SupplierRequestDTO.builder()
                .code("SUP001")
                .name("Test Supplier")
                .email("test@supplier.com")
                .phone("+1234567890")
                .address("123 Test St")
                .rating(4.5)
                .leadTime(7)
                .build();

        supplier = Supplier.builder()
                .code("SUP001")
                .name("Test Supplier")
                .email("test@supplier.com")
                .phone("+1234567890")
                .address("123 Test St")
                .rating(4.5)
                .leadTime(7)
                .build();

        responseDTO = SupplierResponseDTO.builder()
                .code("SUP001")
                .name("Test Supplier")
                .email("test@supplier.com")
                .phone("+1234567890")
                .rating(4.5)
                .leadTime(7)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should create supplier successfully")
    void shouldCreateSupplierSuccessfully() {
        when(supplierRepository.existsByCode(anyString())).thenReturn(false);
        when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierMapper.toEntity(any(SupplierRequestDTO.class))).thenReturn(supplier);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(any(Supplier.class))).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.create(requestDTO);

        assertNotNull(result);
        assertEquals("SUP001", result.getCode());
        assertEquals("Test Supplier", result.getName());
        verify(supplierRepository).existsByCode("SUP001");
        verify(supplierRepository).existsByEmail("test@supplier.com");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw exception when creating supplier with duplicate code")
    void shouldThrowExceptionWhenCreatingSupplierWithDuplicateCode() {
        when(supplierRepository.existsByCode(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            supplierService.create(requestDTO);
        });

        verify(supplierRepository).existsByCode("SUP001");
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw exception when creating supplier with duplicate email")
    void shouldThrowExceptionWhenCreatingSupplierWithDuplicateEmail() {
        when(supplierRepository.existsByCode(anyString())).thenReturn(false);
        when(supplierRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            supplierService.create(requestDTO);
        });

        verify(supplierRepository).existsByCode("SUP001");
        verify(supplierRepository).existsByEmail("test@supplier.com");
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should update supplier successfully")
    void shouldUpdateSupplierSuccessfully() {
    when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
    // make these lenient as update flow may not call both existence checks depending on DTO values
    lenient().when(supplierRepository.existsByCode(anyString())).thenReturn(false);
    lenient().when(supplierRepository.existsByEmail(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(any(Supplier.class))).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.update(1L, requestDTO);

        assertNotNull(result);
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).updateEntityFromDTO(requestDTO, supplier);
        verify(supplierRepository).save(supplier);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent supplier")
    void shouldThrowExceptionWhenUpdatingNonExistentSupplier() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.update(999L, requestDTO);
        });

        verify(supplierRepository).findById(999L);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should delete supplier successfully")
    void shouldDeleteSupplierSuccessfully() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.hasActiveOrders(1L)).thenReturn(false);

        assertDoesNotThrow(() -> supplierService.delete(1L));

        verify(supplierRepository).findById(1L);
        verify(supplierRepository).hasActiveOrders(1L);
        verify(supplierRepository).delete(supplier);
    }

    @Test
    @DisplayName("Should throw exception when deleting supplier with active orders")
    void shouldThrowExceptionWhenDeletingSupplierWithActiveOrders() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.hasActiveOrders(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            supplierService.delete(1L);
        });

        verify(supplierRepository).findById(1L);
        verify(supplierRepository).hasActiveOrders(1L);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    @DisplayName("Should find supplier by ID successfully")
    void shouldFindSupplierByIdSuccessfully() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponseDTO(any(Supplier.class))).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.findById(1L);

        assertNotNull(result);
        assertEquals("SUP001", result.getCode());
        verify(supplierRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when supplier not found by ID")
    void shouldThrowExceptionWhenSupplierNotFoundById() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.findById(999L);
        });

        verify(supplierRepository).findById(999L);
    }

    @Test
    @DisplayName("Should find supplier by code successfully")
    void shouldFindSupplierByCodeSuccessfully() {
        when(supplierRepository.findByCode("SUP001")).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponseDTO(any(Supplier.class))).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.findByCode("SUP001");

        assertNotNull(result);
        assertEquals("SUP001", result.getCode());
        verify(supplierRepository).findByCode("SUP001");
    }

    @Test
    @DisplayName("Should find all suppliers with pagination")
    void shouldFindAllSuppliersWithPagination() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        List<SupplierResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplierRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        PageResponse<SupplierResponseDTO> result = supplierService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(supplierRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should search suppliers by name")
    void shouldSearchSuppliersByName() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        List<SupplierResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplierRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(supplierPage);
        when(supplierMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplierResponseDTO> result = supplierService.searchByName("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository).findByNameContainingIgnoreCase("Test", pageable);
    }

    @Test
    @DisplayName("Should find suppliers by minimum rating")
    void shouldFindSuppliersByMinimumRating() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        List<SupplierResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplierRepository.findByRatingGreaterThanEqual(4.0)).thenReturn(suppliers);
        when(supplierMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplierResponseDTO> result = supplierService.findByMinimumRating(4.0);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository).findByRatingGreaterThanEqual(4.0);
    }

    @Test
    @DisplayName("Should find suppliers by max lead time")
    void shouldFindSuppliersByMaxLeadTime() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        List<SupplierResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplierRepository.findByLeadTimeLessThanEqual(10)).thenReturn(suppliers);
        when(supplierMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplierResponseDTO> result = supplierService.findByMaxLeadTime(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository).findByLeadTimeLessThanEqual(10);
    }

    @Test
    @DisplayName("Should find all suppliers ordered by rating")
    void shouldFindAllSuppliersOrderedByRating() {
        List<Supplier> suppliers = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        List<SupplierResponseDTO> responseDTOs = Arrays.asList(responseDTO);

        when(supplierRepository.findAllOrderByRatingDesc(pageable)).thenReturn(supplierPage);
        when(supplierMapper.toResponseDTOList(anyList())).thenReturn(responseDTOs);

        List<SupplierResponseDTO> result = supplierService.findAllOrderedByRating(pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository).findAllOrderByRatingDesc(pageable);
    }

    @Test
    @DisplayName("Should check if supplier can be deleted")
    void shouldCheckIfSupplierCanBeDeleted() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.hasActiveOrders(1L)).thenReturn(false);

        boolean canBeDeleted = supplierService.canBeDeleted(1L);

        assertTrue(canBeDeleted);
        verify(supplierRepository).existsById(1L);
        verify(supplierRepository).hasActiveOrders(1L);
    }

    @Test
    @DisplayName("Should return false when supplier has active orders")
    void shouldReturnFalseWhenSupplierHasActiveOrders() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(supplierRepository.hasActiveOrders(1L)).thenReturn(true);

        boolean canBeDeleted = supplierService.canBeDeleted(1L);

        assertFalse(canBeDeleted);
        verify(supplierRepository).existsById(1L);
        verify(supplierRepository).hasActiveOrders(1L);
    }

    @Test
    @DisplayName("Should throw exception when checking deletion for non-existent supplier")
    void shouldThrowExceptionWhenCheckingDeletionForNonExistentSupplier() {
        when(supplierRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.canBeDeleted(999L);
        });

        verify(supplierRepository).existsById(999L);
        verify(supplierRepository, never()).hasActiveOrders(anyLong());
    }
}
