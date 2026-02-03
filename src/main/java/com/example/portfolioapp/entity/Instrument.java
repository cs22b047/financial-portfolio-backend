package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Base entity for all tradeable instruments (stocks, bonds, crypto, etc.).
 * Contains common market data fields. Type-specific data in separate tables.
 *
 * This replaces the bloated MarketData entity with a cleaner, normalized structure.
 */
@Entity
@Table(name = "instruments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"instrument_type", "symbol"}))
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, length = 20)
    private InstrumentType instrumentType;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "current_price", precision = 18, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", precision = 18, scale = 8)
    private BigDecimal previousClose;

    @Column(name = "day_change", precision = 18, scale = 8)
    private BigDecimal dayChange;

    @Column(name = "day_change_percent", precision = 8, scale = 4)
    private BigDecimal dayChangePercent;

    @Column(name = "day_high", precision = 18, scale = 8)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 18, scale = 8)
    private BigDecimal dayLow;

    @Column(name = "week_52_high", precision = 18, scale = 8)
    private BigDecimal week52High;

    @Column(name = "week_52_low", precision = 18, scale = 8)
    private BigDecimal week52Low;

    @Column(precision = 20)
    private Long volume;

    @Column(name = "market_cap")
    private Long marketCap;

    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    // Data freshness tracking
    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "market_status", length = 20)
    private String marketStatus;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // One-to-one relationships with type-specific metrics
    @OneToOne(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StockMetrics stockMetrics;

    @OneToOne(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BondMetrics bondMetrics;

    @OneToOne(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CryptoMetrics cryptoMetrics;

    // Relationships
    @OneToMany(mappedBy = "instrument", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Position> positions;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PriceHistory> priceHistory;

    // Constructors
    public Instrument() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public Instrument(InstrumentType instrumentType, String symbol, String name) {
        this();
        this.instrumentType = instrumentType;
        this.symbol = symbol;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InstrumentType getInstrumentType() { return instrumentType; }
    public void setInstrumentType(InstrumentType instrumentType) { this.instrumentType = instrumentType; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        this.lastUpdated = LocalDateTime.now();
    }

    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }

    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }

    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }

    public BigDecimal getDayHigh() { return dayHigh; }
    public void setDayHigh(BigDecimal dayHigh) { this.dayHigh = dayHigh; }

    public BigDecimal getDayLow() { return dayLow; }
    public void setDayLow(BigDecimal dayLow) { this.dayLow = dayLow; }

    public BigDecimal getWeek52High() { return week52High; }
    public void setWeek52High(BigDecimal week52High) { this.week52High = week52High; }

    public BigDecimal getWeek52Low() { return week52Low; }
    public void setWeek52Low(BigDecimal week52Low) { this.week52Low = week52Low; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public Long getMarketCap() { return marketCap; }
    public void setMarketCap(Long marketCap) { this.marketCap = marketCap; }

    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }

    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }

    public Industry getIndustry() { return industry; }
    public void setIndustry(Industry industry) { this.industry = industry; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getMarketStatus() { return marketStatus; }
    public void setMarketStatus(String marketStatus) { this.marketStatus = marketStatus; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public StockMetrics getStockMetrics() { return stockMetrics; }
    public void setStockMetrics(StockMetrics stockMetrics) { this.stockMetrics = stockMetrics; }

    public BondMetrics getBondMetrics() { return bondMetrics; }
    public void setBondMetrics(BondMetrics bondMetrics) { this.bondMetrics = bondMetrics; }

    public CryptoMetrics getCryptoMetrics() { return cryptoMetrics; }
    public void setCryptoMetrics(CryptoMetrics cryptoMetrics) { this.cryptoMetrics = cryptoMetrics; }

    public List<Position> getPositions() { return positions; }
    public void setPositions(List<Position> positions) { this.positions = positions; }

    public List<PriceHistory> getPriceHistory() { return priceHistory; }
    public void setPriceHistory(List<PriceHistory> priceHistory) { this.priceHistory = priceHistory; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Helper methods
    public boolean isStale(int maxAgeMinutes) {
        if (lastUpdated == null) return true;
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(maxAgeMinutes));
    }

    public boolean isMarketOpen() {
        return "OPEN".equalsIgnoreCase(marketStatus);
    }

    public boolean isStock() {
        return instrumentType.isEquity();
    }

    public boolean isBond() {
        return instrumentType.isFixedIncome();
    }

    public boolean isCrypto() {
        return instrumentType.isCrypto();
    }

    @Override
    public String toString() {
        return "Instrument{" +
                "id=" + id +
                ", type=" + instrumentType +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                '}';
    }
}
