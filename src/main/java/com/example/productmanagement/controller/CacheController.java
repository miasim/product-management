package com.example.productmanagement.controller;

import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostMapping("/clear")
    public String clearAll() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return "All caches cleared at " + LocalDateTime.now();
    }

    @PostMapping("/clear/{cacheName}")
    public String clearSpecific(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return "Cache '" + cacheName + "' cleared at " + LocalDateTime.now();
        }
        return "Cache '" + cacheName + "' not found";
    }

    @GetMapping("/names")
    public java.util.Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}