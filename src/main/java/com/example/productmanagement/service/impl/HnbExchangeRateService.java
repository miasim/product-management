package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ExchangeRateUnavailableException;
import com.example.productmanagement.service.ExchangeRateService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.example.productmanagement.model.Currency.EUR;
import static com.example.productmanagement.model.Currency.USD;

@Service
public class HnbExchangeRateService implements ExchangeRateService {

    private final RestTemplate restTemplate;

    private final String EXCHANGE_RATE_NOT_FOUND = "Exchange rate not found in HNB API response.";

    private final String INVALID_EXCHANGE_RATE_FORMAT = "Invalid exchange rate format.";

    private final String HNB_CALL_ERROR = "Error occurred while calling HNB API: ";

    private final String MID_MARKET_EXCHANGE_RATE = "srednji_tecaj";

    private final String EUR_URL = "https://api.hnb.hr/tecajn/v2?valuta=EUR";

    private final String USD_URL = "https://api.hnb.hr/tecajn/v2?valuta=USD";

    public HnbExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#currency")
    public BigDecimal getExchangeRate(String currency) {
        String url = getExchangeUrl(currency);
        try {
            Map[] response = restTemplate.getForObject(url, Map[].class);

            if (response == null || response.length == 0 || response[0].get(MID_MARKET_EXCHANGE_RATE) == null) {
                throw new ExchangeRateUnavailableException(EXCHANGE_RATE_NOT_FOUND);
            }

            String rateStr = (String) response[0].get(MID_MARKET_EXCHANGE_RATE);

            try {
                return new BigDecimal(rateStr.replace(",", "."));
            } catch (NumberFormatException e) {
                throw new ExchangeRateUnavailableException(INVALID_EXCHANGE_RATE_FORMAT + rateStr, e);
            }

        } catch (RestClientException e) {
            throw new ExchangeRateUnavailableException(HNB_CALL_ERROR + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "eurToUsd", key = "#priceEur")
    public BigDecimal convertEurToUsd(BigDecimal priceEur) {
        BigDecimal usdToHrkRate = getExchangeRate(USD.name());
        BigDecimal eurToHrkRate = getExchangeRate(EUR.name());

        BigDecimal priceHrk = priceEur.multiply(eurToHrkRate);
        return priceHrk.divide(usdToHrkRate, 4, RoundingMode.HALF_UP);
    }

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void clearExchangeRatesCache() {
        System.out.println("Exchange rates cache cleared");
    }

    @CacheEvict(value = "eurToUsd", allEntries = true)
    public void clearConversionCache() {
        System.out.println("Conversion cache cleared");
    }

    @CacheEvict(value = {"exchangeRates", "eurToUsd"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("All exchange rate caches cleared");
    }

    private String getExchangeUrl(String currency) {
        String url;
        if (currency.equals(EUR.name())) {
            url = EUR_URL;
        } else {
            url = USD_URL;
        }
        return url;
    }
}
