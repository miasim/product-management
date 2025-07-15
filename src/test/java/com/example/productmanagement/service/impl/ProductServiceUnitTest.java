package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.repo.ProductRepository;
import com.example.productmanagement.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceUnitTest {

    private ProductRepository productRepository;
    private ExchangeRateService exchangeRateService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        exchangeRateService = mock(ExchangeRateService.class);
        productService = new ProductService(productRepository, exchangeRateService);
    }

    @Test
    void save_shouldConvertPriceToUsd() {
        Product input = new Product(null, "Product", BigDecimal.valueOf(100), true);
        Product saved = new Product("CODE000001", "Product A", BigDecimal.valueOf(100), true);
        saved.setId(1L);

        when(productRepository.save(input)).thenReturn(saved);
        when(exchangeRateService.convertEurToUsd(any())).thenReturn(BigDecimal.valueOf(100.6));

        Product result = productService.save(input);

        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(100.6), result.getPriceUsd());
    }

    @Test
    void findById_shouldReturnProductWithUsdPrice() {
        Product product = new Product("CODE000001", "Product", BigDecimal.valueOf(50), true);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(exchangeRateService.convertEurToUsd(any())).thenReturn(BigDecimal.valueOf(50.6));

        Product result = productService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(50.6), result.getPriceUsd());
    }

    @Test
    void findById_shouldThrowExceptionIfNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void findAll_shouldReturnAllWithUsdPrices() {
        List<Product> products = Arrays.asList(
                new Product("CODE000001", "Product A", BigDecimal.valueOf(10), true),
                new Product("CODE000002", "Product B", BigDecimal.valueOf(10), true)
        );

        when(productRepository.findAll()).thenReturn(products);
        when(exchangeRateService.convertEurToUsd(any())).thenReturn(BigDecimal.valueOf(10.6));

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(10.6), result.get(0).getPriceUsd());
        assertEquals(BigDecimal.valueOf(10.6), result.get(1).getPriceUsd());
    }
}