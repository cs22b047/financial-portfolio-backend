package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Reference table for exchanges/trading venues.
 * Examples: NYSE, NASDAQ, LSE, Coinbase, Binance
 */
@Entity
@Table(name = "exchanges")
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "mic_code", length = 10)
    private String micCode; // Market Identifier Code (ISO 10383)

    @Column(length = 3)
    private String country;

    @Column(length = 50)
    private String timezone;

    @Column(name = "market_open")
    private LocalTime marketOpen;

    @Column(name = "market_close")
    private LocalTime marketClose;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", length = 20)
    private ExchangeType exchangeType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Instrument> instruments;

    public Exchange() {
        this.createdDate = LocalDateTime.now();
    }

    public Exchange(String code, String name, ExchangeType exchangeType) {
        this();
        this.code = code;
        this.name = name;
        this.exchangeType = exchangeType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMicCode() { return micCode; }
    public void setMicCode(String micCode) { this.micCode = micCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public LocalTime getMarketOpen() { return marketOpen; }
    public void setMarketOpen(LocalTime marketOpen) { this.marketOpen = marketOpen; }

    public LocalTime getMarketClose() { return marketClose; }
    public void setMarketClose(LocalTime marketClose) { this.marketClose = marketClose; }

    public ExchangeType getExchangeType() { return exchangeType; }
    public void setExchangeType(ExchangeType exchangeType) { this.exchangeType = exchangeType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<Instrument> getInstruments() { return instruments; }
    public void setInstruments(List<Instrument> instruments) { this.instruments = instruments; }

    public boolean isCryptoExchange() {
        return exchangeType == ExchangeType.CRYPTO;
    }

    @Override
    public String toString() {
        return "Exchange{code='" + code + "', name='" + name + "'}";
    }
}
