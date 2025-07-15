package com.example.productmanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    @JsonIgnore
    private Long id;

    @Size(min = 10, max = 10, message = "Code must be exactly 10 characters long")
    @NotBlank(message = "Code is required")
    @NotNull(message = "Code must not be null")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @DecimalMin(value = "0.01", message = "Price must be a positive value, at least 0.01")
    @NotNull(message = "Price in EUR is required")
    private BigDecimal priceEur;

    private BigDecimal priceUsd;

    private boolean isAvailable;

}