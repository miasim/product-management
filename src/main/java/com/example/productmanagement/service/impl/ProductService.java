package com.example.productmanagement.service.impl;

import com.example.productmanagement.exception.ProductNotFoundException;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.repo.ProductRepository;
import com.example.productmanagement.service.ExchangeRateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ExchangeRateService exchangeRateService;

    public ProductService(ProductRepository productRepository, ExchangeRateService exchangeRateService) {
        this.productRepository = productRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public Product save(Product product) {
        product = productRepository.save(product);
        product.setPriceUsd(exchangeRateService.convertEurToUsd(product.getPriceEur()));
        return product;
    }

    public Product findById(Long id) {
        if (id == null) {
            throw new ProductNotFoundException("Product ID cannot be null");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));
        product.setPriceUsd(exchangeRateService.convertEurToUsd(product.getPriceEur()));
        return product;
    }

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        products.forEach(p -> p.setPriceUsd(exchangeRateService.convertEurToUsd(p.getPriceEur())));
        return products;
    }
}