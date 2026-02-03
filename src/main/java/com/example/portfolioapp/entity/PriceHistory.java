package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historical price data (OHLCV) for market data.
 * Used for charting and historical portfolio value calculations.
 * 
 * Relationships:
 * - Many PriceHistory -> One MarketData
 */
@Entity
@Table(name = "price_history",
       uniqueConstraints = @UniqueConstraint(columnNames = {"market_data_id", "price_date"}),
       indexes = {
           @Index(name = "idx_price_history_date", columnList = "price_date"),
           @Index(name = "idx_price_history_market_data", columnList = "market_data_id")
       })
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id", nullable = false)
    private MarketData marketData;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price", precision = 18, scale = 8)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 18, scale = 8)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 8)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 18, scale = 8, nullable = false)
    private BigDecimal closePrice;

    @Column(name = "adjusted_close", precision = 18, scale = 8)
    private BigDecimal adjustedClose; // Adjusted for splits/dividends

    @Column(precision = 20)
    private Long volume;

    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public PriceHistory() {
        this.createdDate = LocalDateTime.now();
    }

    public PriceHistory(MarketData marketData, LocalDate priceDate, BigDecimal closePrice) {
        this();
        this.marketData = marketData;
        this.priceDate = priceDate;
        this.closePrice = closePrice;
    }

    public PriceHistory(MarketData marketData, LocalDate priceDate,
                        BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        this(marketData, priceDate, close);
        this.openPrice = open;
        this.highPrice = high;
        this.lowPrice = low;
        this.volume = volume;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MarketData getMarketData() { return marketData; }
    public void setMarketData(MarketData marketData) { this.marketData = marketData; }

    public LocalDate getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDate priceDate) { this.priceDate = priceDate; }

    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }

    public BigDecimal getAdjustedClose() { return adjustedClose; }
    public void setAdjustedClose(BigDecimal adjustedClose) { this.adjustedClose = adjustedClose; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    // Helper methods
    public BigDecimal getDayRange() {
        if (highPrice == null || lowPrice == null) return null;
        return highPrice.subtract(lowPrice);
    }

    public BigDecimal getDayChangePercent() {
        if (openPrice == null || closePrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) return null;
        return closePrice.subtract(openPrice)
                         .divide(openPrice, 4, java.math.RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getEffectiveClose() {
        return adjustedClose != null ? adjustedClose : closePrice;
    }

    @Override
    public String toString() {
        return "PriceHistory{marketDataId=" + (marketData != null ? marketData.getId() : null) +
               ", date=" + priceDate + ", close=" + closePrice + "}";
    }
}
