package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ExchangeRateUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.example.productmanagement.model.Currency.EUR;
import static com.example.productmanagement.model.Currency.USD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HnbExchangeRateServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HnbExchangeRateService exchangeRateService;

    @Test
    void getEurRate_ReturnsCorrectRate() {
        // Arrange
        Map<String, Object> mockResponse = Map.of("srednji_tecaj", "7,1234");
        Map[] responseArray = new Map[] { mockResponse };

        when(restTemplate.getForObject(eq("https://api.hnb.hr/tecajn/v2?valuta=EUR"), eq(Map[].class)))
                .thenReturn(responseArray);

        // Act
        BigDecimal rate = exchangeRateService.getExchangeRate(EUR.name());

        // Assert
        assertEquals(new BigDecimal("7.1234"), rate);
        verify(restTemplate, times(1)).getForObject(eq("https://api.hnb.hr/tecajn/v2?valuta=EUR"), eq(Map[].class));
    }

    @Test
    void getUsdRate_ReturnsCorrectRate() {
        // Arrange
        Map<String, Object> mockResponse = Map.of("srednji_tecaj", "6,9876");
        Map[] responseArray = new Map[] { mockResponse };

        System.out.println("Setting up mock for USD rate test");

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(responseArray);

        // Act
        System.out.println("Calling getExchangeRate for USD");
        BigDecimal rate = exchangeRateService.getExchangeRate(USD.name());

        // Assert
        assertEquals(new BigDecimal("6.9876"), rate);
        verify(restTemplate, times(1)).getForObject(contains("valuta=USD"), eq(Map[].class));
    }

    @Test
    void debugMockSetup() {
        Map<String, Object> mockResponse = Map.of("srednji_tecaj", "7,1234");
        Map[] responseArray = new Map[] { mockResponse };

        System.out.println("Mock response: " + java.util.Arrays.toString(responseArray));
        System.out.println("Response[0]: " + responseArray[0]);
        System.out.println("MID_MARKET_EXCHANGE_RATE value: " + responseArray[0].get("srednji_tecaj"));

        when(restTemplate.getForObject(eq("https://api.hnb.hr/tecajn/v2?valuta=EUR"), eq(Map[].class)))
                .thenReturn(responseArray);

        // Test
        BigDecimal rate = exchangeRateService.getExchangeRate("EUR");
        assertEquals(new BigDecimal("7.1234"), rate);
    }

    @Test
    void getEurRate_ThrowsExceptionWhenResponseIsNull() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(null);

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(EUR.name()));

        assertTrue(exception.getMessage().contains("Exchange rate not found"));
    }

    @Test
    void getUsdRate_ThrowsExceptionWhenRateMissing() {
        // Arrange
        Map<String, Object> mockResponse = Map.of();
        Map[] responseArray = new Map[] { mockResponse };

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(responseArray);

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(USD.name()));

        assertTrue(exception.getMessage().contains("Exchange rate not found"));
    }

    @Test
    void getUsdRate_ThrowsExceptionWhenRateFormatInvalid() {
        // Arrange
        Map<String, Object> mockResponse = Map.of("srednji_tecaj", "abc");
        Map[] responseArray = new Map[] { mockResponse };

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(responseArray);

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(USD.name()));

        assertTrue(exception.getMessage().contains("Invalid exchange rate format"));
    }

    @Test
    void getEurRate_ThrowsExceptionOnRestClientException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenThrow(new RestClientException("Connection error"));

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(EUR.name()));

        assertTrue(exception.getMessage().contains("Error occurred while calling HNB API"));
    }

    @Test
    void getExchangeRate_ThrowsExceptionWhenResponseArrayIsEmpty() {
        // Arrange
        Map[] emptyResponseArray = new Map[0];
        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(emptyResponseArray);

        // Act & Assert
        ExchangeRateUnavailableException exception = assertThrows(ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getExchangeRate(EUR.name()));

        assertTrue(exception.getMessage().contains("Exchange rate not found"));
    }

    @Test
    void convertEurToUsd_ReturnsCorrectValue() {
        // Arrange
        Map<String, Object> usdResponse = Map.of("srednji_tecaj", "6,5000");
        Map<String, Object> eurResponse = Map.of("srednji_tecaj", "7,5000");

        when(restTemplate.getForObject(contains("valuta=USD"), eq(Map[].class)))
                .thenReturn(new Map[]{usdResponse});
        when(restTemplate.getForObject(contains("valuta=EUR"), eq(Map[].class)))
                .thenReturn(new Map[]{eurResponse});

        BigDecimal priceEur = new BigDecimal("10");
        BigDecimal expectedUsd = priceEur.multiply(new BigDecimal("7.5000"))
                .divide(new BigDecimal("6.5000"), 4, RoundingMode.HALF_UP);

        // Act
        BigDecimal actualUsd = exchangeRateService.convertEurToUsd(priceEur);

        // Assert
        assertEquals(expectedUsd, actualUsd);
        verify(restTemplate, times(1)).getForObject(contains("valuta=USD"), eq(Map[].class));
        verify(restTemplate, times(1)).getForObject(contains("valuta=EUR"), eq(Map[].class));
    }

    @Test
    void convertEurToUsd_HandlesZeroAmount() {
        // Arrange
        Map<String, Object> usdResponse = Map.of("srednji_tecaj", "6,5000");
        Map<String, Object> eurResponse = Map.of("srednji_tecaj", "7,5000");

        when(restTemplate.getForObject(contains("valuta=USD"), eq(Map[].class)))
                .thenReturn(new Map[]{usdResponse});
        when(restTemplate.getForObject(contains("valuta=EUR"), eq(Map[].class)))
                .thenReturn(new Map[]{eurResponse});

        // Act
        BigDecimal result = exchangeRateService.convertEurToUsd(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO.setScale(4), result);
    }

    @Test
    void getExchangeRate_HandlesCommaToDecimalConversion() {
        // Arrange
        Map<String, Object> mockResponse = Map.of("srednji_tecaj", "1,234567");
        Map[] responseArray = new Map[] { mockResponse };

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(responseArray);

        // Act
        BigDecimal rate = exchangeRateService.getExchangeRate(EUR.name());

        // Assert
        assertEquals(new BigDecimal("1.234567"), rate);
    }
}