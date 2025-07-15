# Product Management API

A RESTful API for managing products with automatic conversion of prices from EUR to USD using the Croatian National Bank (HNB) exchange rate.

## Project Overview

This Spring Boot application provides CRUD operations on `Product` entities. Each product has a unique code, name, price in EUR, and a calculated price in USD based on the latest exchange rate fetched from the HNB API.

### Technologies used:
- Java 17+
- Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL database
- RestTemplate for external API calls (HNB exchange rates)
- Bean Validation (Jakarta Validation)
- Lombok (for reducing boilerplate code)

---

## Product Entity Fields

- 'id' (Long, auto-generated)
- 'code' (String, unique, exactly 10 characters)
- 'name' (String)
- 'priceEur' (BigDecimal, non-negative)
- 'priceUsd' (BigDecimal, calculated dynamically)
- 'isAvailable' (boolean)

---

## API Endpoints
- POST /products @PostMapping("/{product}") — Create new product
- GET /products @GetMapping("/{id}") — Get product by id
- GET /products — Get all products

### Cache Management
- GET /api/cache/names - List all available cache names
- POST /api/cache/clear - Clear all caches
- POST /api/cache/clear/{cacheName} - Clear specific cache by name

Cache Behavior:
Caches automatically expire after configured TTL (5 minutes)
Manual cache clearing available for debugging and testing
Cache names can be listed via API endpoint

---

## Local Environment Setup

### Prerequisites

- Java 17 or higher installed
- Gradle build tool installed
- PostgreSQL installed and running
- Internet connection (to fetch exchange rates from HNB)

### Step-by-step setup

1. **Install PostgreSQL**  
   Download and install PostgreSQL from [https://www.postgresql.org/download/](https://www.postgresql.org/download/).

2. **Create the database**  
   After installation, create a database named 'product_db':
3. 
   CREATE DATABASE product_db;

## Swagger:
http://localhost:8080/swagger-ui.html
