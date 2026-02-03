package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reference table for currencies (ISO 4217).
 * Supports both fiat (USD, EUR) and crypto base currencies (BTC, ETH).
 */
@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @Column(length = 10)
    private String code; // ISO code: USD, EUR, BTC

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 5)
    private String symbol; // $, €, ₿

    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    @Column(name = "is_fiat")
    private Boolean isFiat = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "exchange_rate_to_usd", precision = 18, scale = 8)
    private BigDecimal exchangeRateToUsd;

    @Column(name = "rate_updated_at")
    private LocalDateTime rateUpdatedAt;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public Currency() {
        this.createdDate = LocalDateTime.now();
    }

    public Currency(String code, String name, String symbol, Boolean isFiat) {
        this();
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.isFiat = isFiat;
        this.decimalPlaces = isFiat ? 2 : 8;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public Boolean getIsFiat() { return isFiat; }
    public void setIsFiat(Boolean isFiat) { this.isFiat = isFiat; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public BigDecimal getExchangeRateToUsd() { return exchangeRateToUsd; }
    public void setExchangeRateToUsd(BigDecimal exchangeRateToUsd) {
        this.exchangeRateToUsd = exchangeRateToUsd;
        this.rateUpdatedAt = LocalDateTime.now();
    }

    public LocalDateTime getRateUpdatedAt() { return rateUpdatedAt; }
    public void setRateUpdatedAt(LocalDateTime rateUpdatedAt) { this.rateUpdatedAt = rateUpdatedAt; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public BigDecimal convertToUsd(BigDecimal amount) {
        if (exchangeRateToUsd == null || amount == null) return null;
        return amount.multiply(exchangeRateToUsd);
    }

    @Override
    public String toString() {
        return "Currency{code='" + code + "', name='" + name + "', symbol='" + symbol + "'}";
    }
}
