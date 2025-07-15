package com.example.productmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 10, nullable = false)
    @Size(min = 10, max = 10, message = "Incorrect code length, code must contain exactly 10 characters")
    private String code;

    @Column(nullable = false)
    private String name;

    @DecimalMin(value = "0.0")
    @Column(name = "price_eur", nullable = false)
    private BigDecimal priceEur;

    @Transient
    private BigDecimal priceUsd;

    @Column(name = "is_available")
    private boolean isAvailable;

    public Product(String code, String name, BigDecimal priceEur, boolean isAvailable) {
        this.code = code;
        this.name = name;
        this.priceEur = priceEur;
        this.isAvailable = isAvailable;
    }

}
