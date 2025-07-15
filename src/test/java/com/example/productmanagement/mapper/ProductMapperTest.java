package com.example.productmanagement.mapper;

import com.example.productmanagement.dto.ProductDto;
import com.example.productmanagement.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    void testToDto_whenProductIsValid_thenCorrectlyMapped() {
        Product product = new Product();
        product.setId(1L);
        product.setCode("CODE000001");
        product.setName("Test Product");
        product.setPriceEur(BigDecimal.valueOf(10.99));
        product.setPriceUsd(BigDecimal.valueOf(11.99));
        product.setAvailable(true);

        ProductDto dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals("CODE000001", dto.getCode());
        assertEquals("Test Product", dto.getName());
        assertEquals(BigDecimal.valueOf(10.99), dto.getPriceEur());
        assertEquals(BigDecimal.valueOf(11.99), dto.getPriceUsd());
        assertTrue(dto.isAvailable());
    }

    @Test
    void testToDto_whenProductIsNull_thenReturnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToEntity_whenDtoIsValid_thenCorrectlyMapped() {
        ProductDto dto = ProductDto.builder()
                .id(2L)
                .code("CODE000001")
                .name("Another Product")
                .priceEur(BigDecimal.valueOf(20.0))
                .priceUsd(BigDecimal.valueOf(21.0))
                .isAvailable(false)
                .build();

        Product product = mapper.toEntity(dto);

        assertNotNull(product);
        assertEquals(2L, product.getId());
        assertEquals("CODE000001", product.getCode());
        assertEquals("Another Product", product.getName());
        assertEquals(BigDecimal.valueOf(20.0), product.getPriceEur());
        assertFalse(product.isAvailable());
    }

    @Test
    void testToEntity_whenDtoIsNull_thenReturnsNull() {
        assertNull(mapper.toEntity(null));
    }
}
