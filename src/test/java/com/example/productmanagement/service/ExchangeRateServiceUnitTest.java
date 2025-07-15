package com.example.productmanagement.service;


import com.example.productmanagement.exception.ExchangeRateUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest {

    private RestTemplate restTemplate;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        exchangeRateService = new ExchangeRateService(restTemplate);
    }

    @Test
    void testGetEurToUsdRate_whenValidResponse_thenReturnsRate() {
        Map<String, String> responseItem = new HashMap<>();
        responseItem.put("srednji_tecaj", "7,53450");

        Map[] response = new Map[]{responseItem};
        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(response);

        BigDecimal result = exchangeRateService.getEurToUsdRate();
        assertEquals(new BigDecimal("7.53450"), result);
    }

    @Test
    void testGetEurToUsdRate_whenResponseIsNull_thenThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(null);

        ExchangeRateUnavailableException ex = assertThrows(
                ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getEurToUsdRate()
        );
        assertTrue(ex.getMessage().contains("Exchange rate not found"));
    }

    @Test
    void testGetEurToUsdRate_whenResponseMissingRate_thenThrowsException() {
        Map<String, String> responseItem = new HashMap<>();
        Map[] response = new Map[]{responseItem};

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(response);

        ExchangeRateUnavailableException ex = assertThrows(
                ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getEurToUsdRate()
        );
        assertTrue(ex.getMessage().contains("Exchange rate not found"));
    }

    @Test
    void testGetEurToUsdRate_whenRateHasInvalidFormat_thenThrowsException() {
        Map<String, String> responseItem = new HashMap<>();
        responseItem.put("srednji_tecaj", "invalid_number");
        Map[] response = new Map[]{responseItem};

        when(restTemplate.getForObject(anyString(), eq(Map[].class))).thenReturn(response);

        ExchangeRateUnavailableException ex = assertThrows(
                ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getEurToUsdRate()
        );
        assertTrue(ex.getMessage().contains("Invalid exchange rate format"));
    }

    @Test
    void testGetEurToUsdRate_whenRestClientThrowsException_thenThrowsExchangeRateUnavailable() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenThrow(new RestClientException("Connection error"));

        ExchangeRateUnavailableException ex = assertThrows(
                ExchangeRateUnavailableException.class,
                () -> exchangeRateService.getEurToUsdRate()
        );
        assertTrue(ex.getMessage().contains("Error occurred while calling HNB API"));
    }
}