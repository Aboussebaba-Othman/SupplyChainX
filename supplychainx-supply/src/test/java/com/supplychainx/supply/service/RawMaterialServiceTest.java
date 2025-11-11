package com.supplychainx.supply.service;

import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.RawMaterialRequestDTO;
import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.mapper.RawMaterialMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RawMaterialService Unit Tests")
class RawMaterialServiceTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RawMaterialMapper rawMaterialMapper;

    @InjectMocks
    private RawMaterialService rawMaterialService;

    private RawMaterialRequestDTO requestDTO;
    private RawMaterial rawMaterial;
    private RawMaterialResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = RawMaterialRequestDTO.builder()
                .code("RM001")
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .unitPrice(10.5)
                .build();

        rawMaterial = RawMaterial.builder()
                .code("RM001")
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .unitPrice(10.5)
                .build();

        responseDTO = RawMaterialResponseDTO.builder()
                .id(1L)
                .code("RM001")
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .build();
    }

    @Test
    @DisplayName("Should create raw material successfully")
    void shouldCreateRawMaterialSuccessfully() {
        when(rawMaterialRepository.existsByCode(anyString())).thenReturn(false);
        when(rawMaterialMapper.toEntity(any(RawMaterialRequestDTO.class))).thenReturn(rawMaterial);
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toResponseDTO(any(RawMaterial.class))).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.create(requestDTO);

        assertNotNull(result);
        assertEquals("RM001", result.getCode());
        assertEquals("Steel", result.getName());
        verify(rawMaterialRepository).existsByCode("RM001");
        verify(rawMaterialRepository).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate raw material")
    void shouldThrowExceptionWhenCreatingDuplicateRawMaterial() {
        when(rawMaterialRepository.existsByCode(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            rawMaterialService.create(requestDTO);
        });

        verify(rawMaterialRepository).existsByCode("RM001");
        verify(rawMaterialRepository, never()).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should find raw material by ID successfully")
    void shouldFindRawMaterialByIdSuccessfully() {
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(rawMaterialMapper.toResponseDTO(any(RawMaterial.class))).thenReturn(responseDTO);

        RawMaterialResponseDTO result = rawMaterialService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("RM001", result.getCode());
        verify(rawMaterialRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when raw material not found")
    void shouldThrowExceptionWhenRawMaterialNotFound() {
        when(rawMaterialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            rawMaterialService.findById(999L);
        });

        verify(rawMaterialRepository).findById(999L);
    }

    @Test
    @DisplayName("Should delete raw material successfully")
    void shouldDeleteRawMaterialSuccessfully() {
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(rawMaterialRepository.isUsedInOrders(1L)).thenReturn(false);

        assertDoesNotThrow(() -> rawMaterialService.delete(1L));

        verify(rawMaterialRepository).findById(1L);
        verify(rawMaterialRepository).isUsedInOrders(1L);
        verify(rawMaterialRepository).delete(rawMaterial);
    }
}
