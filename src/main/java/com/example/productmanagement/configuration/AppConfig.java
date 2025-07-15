package com.example.productmanagement.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                //.expireAfterWrite(24, TimeUnit.HOURS)
                .removalListener((key, value, cause) -> {
                    System.out.println("🔄 Cache entry removed: " + key + " (reason: " + cause + ")");
                }));

        cacheManager.setCacheNames(java.util.Arrays.asList("exchangeRates", "eurToUsd"));

        return cacheManager;
    }
}