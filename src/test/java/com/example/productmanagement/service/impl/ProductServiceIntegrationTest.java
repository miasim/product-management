package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ExchangeRateUnavailableException;
import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.repo.ProductRepository;
import com.example.productmanagement.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    private static final String TEST_CODE_001 = "CODE000001";
    private static final String TEST_CODE_002 = "CODE000002";
    private static final String TIMEOUT_CODE = "TIMEOUT001";

    private static final String INTEGRATION_TEST_PRODUCT = "Integration Test Product";
    private static final String PRODUCT_1_NAME = "P1";
    private static final String PRODUCT_2_NAME = "P2";
    private static final String TEST_PRODUCT = "Test Product";
    private static final String ZERO_PRICE_PRODUCT = "Zero Price Product";
    private static final String CALL_ONCE_PRODUCT = "Call Once Product";
    private static final String NULL_USD_PRODUCT = "Null USD Product";
    private static final String MULTI_CALL_PRODUCT = "Multi Call Product";
    private static final String TIMEOUT_PRODUCT = "Timeout Product";

    private static final BigDecimal PRICE_100 = BigDecimal.valueOf(100);
    private static final BigDecimal PRICE_10 = BigDecimal.valueOf(10);
    private static final BigDecimal PRICE_20 = BigDecimal.valueOf(20);
    private static final BigDecimal PRICE_100_50 = BigDecimal.valueOf(100.50);
    private static final BigDecimal PRICE_50 = BigDecimal.valueOf(50.00);
    private static final BigDecimal PRICE_42_42 = BigDecimal.valueOf(42.42);
    private static final BigDecimal PRICE_30 = BigDecimal.valueOf(30.00);
    private static final BigDecimal PRICE_60 = BigDecimal.valueOf(60.00);
    private static final BigDecimal PRICE_88_88 = BigDecimal.valueOf(88.88);
    private static final BigDecimal PRICE_25 = BigDecimal.valueOf(25.00);

    private static final BigDecimal USD_PRICE_10_6 = BigDecimal.valueOf(10.6);
    private static final BigDecimal USD_PRICE_20_6 = BigDecimal.valueOf(20.6);
    private static final BigDecimal USD_PRICE_106_53 = BigDecimal.valueOf(106.53);
    private static final BigDecimal USD_PRICE_44_96 = BigDecimal.valueOf(44.96);
    private static final BigDecimal USD_PRICE_63_60 = BigDecimal.valueOf(63.60);
    private static final BigDecimal USD_PRICE_94_21 = BigDecimal.valueOf(94.21);

    private static final Long NOT_EXISTENT_ID = 999L;
    private static final Long NEGATIVE_ID = -1L;

    private static final String PRODUCT_WITH_ID_999_NOT_FOUND_MESSAGE = "Product with ID 999 not found";
    private static final String PRODUCT_WITH_NEGATIVE_ID_NOT_FOUND_MESSAGE = "Product with ID -1 not found";
    private static final String HNB_API_DOWN_MESSAGE = "HNB API is down";
    private static final String CONNECTION_TIMEOUT_MESSAGE = "Connection timeout";

    private static final int EXPECTED_LIST_SIZE_2 = 2;
    private static final int EXPECTED_VERIFY_TIMES_1 = 1;
    private static final int EXPECTED_VERIFY_TIMES_3 = 3;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void save_shouldPersistProduct() {
        // Arrange
        Product product = new Product(TEST_CODE_001, INTEGRATION_TEST_PRODUCT, PRICE_100, true);

        // Act
        Product saved = productService.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(PRICE_100, saved.getPriceEur());
        assertEquals(INTEGRATION_TEST_PRODUCT, saved.getName());
        assertEquals(TEST_CODE_001, saved.getCode());
        assertNotNull(saved.getId());
    }

    @Test
    void findAll_shouldReturnAllWithUsdPrices() {
        // Arrange
        Product p1 = new Product(TEST_CODE_001, PRODUCT_1_NAME, PRICE_10, true);
        Product p2 = new Product(TEST_CODE_002, PRODUCT_2_NAME, PRICE_20, true);

        productRepository.save(p1);
        productRepository.save(p2);

        when(exchangeRateService.convertEurToUsd(p1.getPriceEur())).thenReturn(USD_PRICE_10_6);
        when(exchangeRateService.convertEurToUsd(p2.getPriceEur())).thenReturn(USD_PRICE_20_6);

        // Act
        List<Product> all = productService.findAll();

        // Assert
        assertEquals(EXPECTED_LIST_SIZE_2, all.size());
        assertEquals(USD_PRICE_10_6, all.get(0).getPriceUsd());
        assertEquals(USD_PRICE_20_6, all.get(1).getPriceUsd());
    }

    @Test
    void findById_shouldReturnProductWithUsdPrice_whenProductExists() {
        // Arrange
        Product product = new Product(TEST_CODE_001, TEST_PRODUCT, PRICE_100_50, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_100_50))
                .thenReturn(USD_PRICE_106_53);

        // Act
        Product foundProduct = productService.findById(productId);

        // Assert
        assertNotNull(foundProduct);
        assertEquals(productId, foundProduct.getId());
        assertEquals(TEST_CODE_001, foundProduct.getCode());
        assertEquals(TEST_PRODUCT, foundProduct.getName());
        assertEquals(PRICE_100_50, foundProduct.getPriceEur());
        assertEquals(USD_PRICE_106_53, foundProduct.getPriceUsd());
        assertTrue(foundProduct.isAvailable());

        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(PRICE_100_50);
    }

    @Test
    void findById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.findById(NOT_EXISTENT_ID);
        });

        assertEquals(PRODUCT_WITH_ID_999_NOT_FOUND_MESSAGE, exception.getMessage());
        verify(exchangeRateService, never()).convertEurToUsd(any(BigDecimal.class));
    }

    @Test
    void findById_shouldThrowProductNotFoundException_whenIdIsNull() {
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.findById(null);
        });

        verify(exchangeRateService, never()).convertEurToUsd(any(BigDecimal.class));
    }

    @Test
    void findById_shouldPropagateExchangeRateException_whenExchangeServiceFails() {
        // Arrange
        Product product = new Product(TEST_CODE_001, TEST_PRODUCT, PRICE_50, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_50))
                .thenThrow(new ExchangeRateUnavailableException(HNB_API_DOWN_MESSAGE));

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class, () -> {
            productService.findById(productId);
        });

        assertEquals(HNB_API_DOWN_MESSAGE, exception.getMessage());
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(PRICE_50);
    }

    @Test
    void findById_shouldHandleZeroPrice() {
        // Arrange
        Product product = new Product(TEST_CODE_001, ZERO_PRICE_PRODUCT, BigDecimal.ZERO, false);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(BigDecimal.ZERO))
                .thenReturn(BigDecimal.ZERO);

        // Act
        Product foundProduct = productService.findById(productId);

        // Assert
        assertNotNull(foundProduct);
        assertEquals(BigDecimal.ZERO, foundProduct.getPriceEur());
        assertEquals(BigDecimal.ZERO, foundProduct.getPriceUsd());
        assertFalse(foundProduct.isAvailable());
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(BigDecimal.ZERO);
    }

    @Test
    void findById_shouldWorkWithNegativeId() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.findById(NEGATIVE_ID);
        });

        assertEquals(PRODUCT_WITH_NEGATIVE_ID_NOT_FOUND_MESSAGE, exception.getMessage());
        verify(exchangeRateService, never()).convertEurToUsd(any(BigDecimal.class));
    }

    @Test
    void findById_shouldCallExchangeServiceExactlyOnce() {
        // Arrange
        Product product = new Product(TEST_CODE_001, CALL_ONCE_PRODUCT, PRICE_42_42, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_42_42))
                .thenReturn(USD_PRICE_44_96);

        // Act
        productService.findById(productId);

        // Assert
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(PRICE_42_42);
        verifyNoMoreInteractions(exchangeRateService);
    }

    @Test
    void findById_shouldHandleExchangeServiceReturningNull() {
        // Arrange
        Product product = new Product(TEST_CODE_001, NULL_USD_PRODUCT, PRICE_30, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_30))
                .thenReturn(null);

        // Act
        Product foundProduct = productService.findById(productId);

        // Assert
        assertNotNull(foundProduct);
        assertNull(foundProduct.getPriceUsd()); // Should be null if exchange service returns null
        assertEquals(PRICE_30, foundProduct.getPriceEur());
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(PRICE_30);
    }

    @Test
    void findById_shouldHandleMultipleConsecutiveCalls() {
        // Arrange
        Product product = new Product(TEST_CODE_001, MULTI_CALL_PRODUCT, PRICE_60, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_60))
                .thenReturn(USD_PRICE_63_60);

        // Act - Call multiple times
        Product foundProduct1 = productService.findById(productId);
        Product foundProduct2 = productService.findById(productId);
        Product foundProduct3 = productService.findById(productId);

        // Assert
        assertNotNull(foundProduct1);
        assertNotNull(foundProduct2);
        assertNotNull(foundProduct3);

        assertEquals(USD_PRICE_63_60, foundProduct1.getPriceUsd());
        assertEquals(USD_PRICE_63_60, foundProduct2.getPriceUsd());
        assertEquals(USD_PRICE_63_60, foundProduct3.getPriceUsd());

        // Verify exchange service was called 3 times
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_3)).convertEurToUsd(PRICE_60);
    }

    @Test
    void findById_shouldHandleExchangeServiceTimeout() {
        // Arrange
        Product product = new Product(TIMEOUT_CODE, TIMEOUT_PRODUCT, PRICE_25, true);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        when(exchangeRateService.convertEurToUsd(PRICE_25))
                .thenThrow(new ExchangeRateUnavailableException(CONNECTION_TIMEOUT_MESSAGE));

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class, () -> {
            productService.findById(productId);
        });

        assertEquals(CONNECTION_TIMEOUT_MESSAGE, exception.getMessage());
        verify(exchangeRateService, times(EXPECTED_VERIFY_TIMES_1)).convertEurToUsd(PRICE_25);
    }
}