package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ExchangeRateUnavailableException;
import com.example.productmanagement.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static com.example.productmanagement.model.Currency.EUR;
import static com.example.productmanagement.model.Currency.USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HnbExchangeRateServiceIntegrationTest {

    // URL Constants
    private static final String EUR_API_URL = "https://api.hnb.hr/tecajn/v2?valuta=EUR";
    private static final String USD_API_URL = "https://api.hnb.hr/tecajn/v2?valuta=USD";

    // JSON Response Constants
    private static final String EUR_SUCCESS_RESPONSE = "[{\"valuta\":\"EUR\",\"srednji_tecaj\":\"7,5345\"}]";
    private static final String USD_SUCCESS_RESPONSE = "[{\"valuta\":\"USD\",\"srednji_tecaj\":\"6,6350\"}]";
    private static final String EMPTY_RESPONSE = "[]";
    private static final String INVALID_FORMAT_RESPONSE = "[{\"valuta\":\"USD\",\"srednji_tecaj\":\"abc\"}]";

    // Expected Values Constants
    private static final BigDecimal EXPECTED_EUR_RATE = new BigDecimal("7.5345");
    private static final BigDecimal EXPECTED_USD_RATE = new BigDecimal("6.6350");
    private static final BigDecimal TEST_EUR_AMOUNT = new BigDecimal("10");

    // Error Message Constants
    private static final String EXCHANGE_RATE_NOT_FOUND_MESSAGE = "Exchange rate not found";
    private static final String INVALID_EXCHANGE_RATE_FORMAT_MESSAGE = "Invalid exchange rate format.";

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CaffeineCacheManager cacheManager;


    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache( "eurToUsd").clear();

    }


    @Test
    void testGetEurRate_Success() {
        // Arrange
        mockServer.expect(once(), requestTo(EUR_API_URL))
                .andRespond(withSuccess(EUR_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        // Act
        BigDecimal rate = exchangeRateService.getExchangeRate(EUR.name());

        // Assert
        assertEquals(EXPECTED_EUR_RATE, rate);
        mockServer.verify();
    }

    @Test
    void testGetUsdRate_Success() {
        // Arrange
        mockServer.expect(once(), requestTo(USD_API_URL))
                .andRespond(withSuccess(USD_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        // Act
        BigDecimal rate = exchangeRateService.getExchangeRate(USD.name());

        // Assert
        assertEquals(EXPECTED_USD_RATE, rate);
        mockServer.verify();
    }

    @Test
    void testConvertEurToUsd_Success() {
        // Arrange
        mockServer.expect(once(), requestTo(USD_API_URL))
                .andRespond(withSuccess(USD_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        mockServer.expect(once(), requestTo(EUR_API_URL))
                .andRespond(withSuccess(EUR_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        BigDecimal expectedUsd = TEST_EUR_AMOUNT.multiply(EXPECTED_EUR_RATE)
                .divide(EXPECTED_USD_RATE, 4, BigDecimal.ROUND_HALF_UP);

        // Act
        BigDecimal actualUsd = exchangeRateService.convertEurToUsd(TEST_EUR_AMOUNT);

        // Assert
        assertEquals(expectedUsd, actualUsd);
        mockServer.verify();
    }

    @Test
    void testGetEurRate_ThrowsExceptionWhenResponseIsEmpty() {
        // Arrange
        mockServer.expect(once(), requestTo(EUR_API_URL))
                .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

        // Act & Assert
        ExchangeRateUnavailableException ex = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(EUR.name()));

        assertTrue(ex.getMessage().contains(EXCHANGE_RATE_NOT_FOUND_MESSAGE));
        mockServer.verify();
    }

    @Test
    void testGetUsdRate_ThrowsExceptionWhenInvalidFormat() {
        // Arrange
        mockServer.expect(once(), requestTo(USD_API_URL))
                .andRespond(withSuccess(INVALID_FORMAT_RESPONSE, MediaType.APPLICATION_JSON));

        // Act & Assert
        ExchangeRateUnavailableException ex = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(USD.name()));

        assertTrue(ex.getMessage().contains(INVALID_EXCHANGE_RATE_FORMAT_MESSAGE));
        System.out.println(ex.getMessage());
        mockServer.verify();
    }

    @Test
    void testGetEurRate_ThrowsExceptionWhenMissingRateField() {
        // Arrange
        String responseWithoutRate = "[{\"valuta\":\"EUR\"}]";

        mockServer.expect(once(), requestTo(EUR_API_URL))
                .andRespond(withSuccess(responseWithoutRate, MediaType.APPLICATION_JSON));

        // Act & Assert
        ExchangeRateUnavailableException ex = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(EUR.name()));

        assertTrue(ex.getMessage().contains(EXCHANGE_RATE_NOT_FOUND_MESSAGE));
        mockServer.verify();
    }


    @Test
    void testGetExchangeRate_HandlesZeroAmount() {
        // Arrange
        mockServer.expect(once(), requestTo(USD_API_URL))
                .andRespond(withSuccess(USD_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        mockServer.expect(once(), requestTo(EUR_API_URL))
                .andRespond(withSuccess(EUR_SUCCESS_RESPONSE, MediaType.APPLICATION_JSON));

        // Act
        BigDecimal result = exchangeRateService.convertEurToUsd(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO.setScale(4), result);
        mockServer.verify();
    }
}