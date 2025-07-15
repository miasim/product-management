package com.example.productmanagement.service;

import java.math.BigDecimal;

public interface ExchangeRateService {

    /**
     * Gets exchange rate for specified currency to HRK
     * @param currency currency code (EUR, USD, etc.)
     * @return exchange rate to HRK
     */
    BigDecimal getExchangeRate(String currency);

    /**
     * Converts EUR amount to USD
     * @param priceEur price in EUR
     * @return equivalent price in USD
     */
    BigDecimal convertEurToUsd(BigDecimal priceEur);
}

