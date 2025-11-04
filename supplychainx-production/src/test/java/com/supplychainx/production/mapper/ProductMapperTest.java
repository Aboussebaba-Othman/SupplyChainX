package com.supplychainx.production.mapper;

import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntity_shouldMapFields() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .code("P-001")
                .name("Widget")
                .description("A sample widget")
                .category("Gadgets")
                .stock(100.0)
                .stockMin(10.0)
                .productionTime(5)
                .cost(12.5)
                .build();

        Product entity = mapper.toEntity(dto);

        assertEquals(dto.getCode(), entity.getCode());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getCategory(), entity.getCategory());
        assertEquals(dto.getStock(), entity.getStock());
        assertEquals(dto.getStockMin(), entity.getStockMin());
        assertEquals(dto.getProductionTime(), entity.getProductionTime());
        assertEquals(dto.getCost(), entity.getCost());
    }

    @Test
    void toResponseDTO_shouldComputeLowStock() {
        Product p = Product.builder()
                .code("P-002")
                .name("LowStockProduct")
                .description("desc")
                .category("cat")
                .stock(5.0)
                .stockMin(10.0)
                .productionTime(2)
                .cost(3.0)
                .build();
        // set auditing fields to ensure mapping doesn't fail
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        ProductResponseDTO resp = mapper.toResponseDTO(p);

        assertEquals(p.getCode(), resp.getCode());
        assertEquals(p.getName(), resp.getName());
        assertEquals(p.getStock(), resp.getStock());
        assertTrue(resp.isLowStock(), "lowStock should be true when stock < stockMin");
    }
}
