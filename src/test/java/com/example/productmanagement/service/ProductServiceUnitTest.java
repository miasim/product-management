package com.example.productmanagement.service;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

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
        Product input = new Product(null, "Product A", BigDecimal.valueOf(100), true);
        Product saved = new Product("0123456789", "Product A", BigDecimal.valueOf(100), true);
        saved.setId(1L);

        when(productRepository.save(input)).thenReturn(saved);
       // when(exchangeRateService.getEurToUsdRate()).thenReturn(BigDecimal.valueOf(1.2));

        Product result = productService.save(input);

        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(120.0), result.getPriceUsd());
    }

    @Test
    void findById_shouldReturnProductWithUsdPrice() {
        Product product = new Product("0123456789", "Product A", BigDecimal.valueOf(50), true);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
      //  when(exchangeRateService.getEurToUsdRate()).thenReturn(BigDecimal.valueOf(1.1));

        Product result = productService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(55.0), result.getPriceUsd());
    }

    @Test
    void findById_shouldThrowIfNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void findAll_shouldReturnAllWithUsdPrices() {
        List<Product> products = Arrays.asList(
                new Product("0123456789", "Product A", BigDecimal.valueOf(10), true),
                new Product("9876543210", "Product B", BigDecimal.valueOf(20), true)
        );

        when(productRepository.findAll()).thenReturn(products);
      //  when(exchangeRateService.getEurToUsdRate()).thenReturn(BigDecimal.valueOf(1.5));

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(15.0), result.get(0).getPriceUsd());
        assertEquals(BigDecimal.valueOf(30.0), result.get(1).getPriceUsd());
    }
}