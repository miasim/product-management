package com.example.productmanagement.mapper;

import com.example.productmanagement.dto.ProductDto;
import com.example.productmanagement.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDto.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .priceEur(product.getPriceEur())
                .priceUsd(product.getPriceUsd())
                .isAvailable(product.isAvailable())
                .build();
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setCode(dto.getCode());
        product.setName(dto.getName());
        product.setPriceEur(dto.getPriceEur());
        product.setPriceUsd(dto.getPriceUsd());
        product.setAvailable(dto.isAvailable());

        return product;
    }
}