package com.example.productmanagement.controller;

import com.example.productmanagement.dto.ProductDto;
import com.example.productmanagement.mapper.ProductMapper;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.service.impl.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.example.productmanagement.ProductManagementApplication.class)
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @MockBean
    private ProductMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long ID = 1L;

    private ProductDto getSampleDto() {
        return ProductDto.builder()
                .id(1L)
                .code("CODE000001")
                .name("Test Product")
                .priceEur(new BigDecimal("100.00"))
                .isAvailable(true)
                .build();
    }

    private Product getSampleEntity() {
        Product product = new Product();
        product.setId(ID);
        product.setCode("1234567890");
        product.setName("Test Product");
        product.setPriceEur(new BigDecimal("100.00"));
        product.setAvailable(true);
        return product;
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductDto dto = getSampleDto();
        Product entity = getSampleEntity();

        Mockito.when(mapper.toEntity(any(ProductDto.class))).thenReturn(entity);
        Mockito.when(service.save(any(Product.class))).thenReturn(entity);
        Mockito.when(mapper.toDto(any(Product.class))).thenReturn(dto);

        mockMvc.perform(post("/api/products/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.priceEur").value(dto.getPriceEur().doubleValue()));
    }

    @Test
    void testGetProductById() throws Exception {
        ProductDto dto = getSampleDto();
        Product entity = getSampleEntity();

        Mockito.when(service.findById(1L)).thenReturn(entity);
        Mockito.when(mapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(dto.getName()));
    }

    @Test
    void testGetAllProducts() throws Exception {
        ProductDto dto = getSampleDto();
        Product entity = getSampleEntity();
        List<Product> entityList = List.of(entity);
        List<ProductDto> dtoList = List.of(dto);

        Mockito.when(service.findAll()).thenReturn(entityList);
        Mockito.when(mapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(dtoList.size()))
                .andExpect(jsonPath("$[0].code").value(dto.getCode()));
    }

    @Test
    void testCreateProduct_InvalidName_ShouldReturnBadRequest() throws Exception {
        ProductDto invalidDto = getSampleDto();
        invalidDto.setName("");
        mockMvc.perform(post("/api/products/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testCreateProduct_NegativePriceEur_ShouldReturnBadRequest() throws Exception {
        ProductDto invalidDto = getSampleDto();
        invalidDto.setPriceEur(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/products/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testCreateProduct_NullCode_ShouldReturnBadRequest() throws Exception {
        ProductDto invalidDto = getSampleDto();
        invalidDto.setCode(null);
        mockMvc.perform(post("/api/products/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

}