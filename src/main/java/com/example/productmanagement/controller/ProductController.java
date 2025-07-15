package com.example.productmanagement.controller;

import com.example.productmanagement.dto.ProductDto;
import com.example.productmanagement.mapper.ProductMapper;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.service.impl.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProductMapper mapper;

    @PostMapping("/{product}")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto productDto) {
        Product productEntity = mapper.toEntity(productDto);
        Product savedProduct = service.save(productEntity);
        ProductDto responseDto = mapper.toDto(savedProduct);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        List<ProductDto> dtos = service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

}