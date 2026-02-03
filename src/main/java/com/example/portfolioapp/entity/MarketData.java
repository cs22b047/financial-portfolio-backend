package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MarketData entity matching the simplified market_data table.
 * Master table for ALL stocks/assets data, regardless of user ownership.
 * 
 * Relationships:
 * - Many MarketData -> One AssetType
 * - One MarketData -> Many Assets
 * - One MarketData -> Many News
 * - One MarketData -> Many ESGRatings
 * - One MarketData -> Many PriceHistory
 */
@Entity
@Table(name = "market_data",
       uniqueConstraints = @UniqueConstraint(name = "uk_market_data_symbol", columnNames = "symbol"),
       indexes = {
           @Index(name = "idx_market_data_sector", columnList = "sector"),
           @Index(name = "idx_market_data_last_updated", columnList = "last_updated")
       })
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    @Column(name = "symbol", nullable = false, length = 20, unique = true)
    private String symbol;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // Price data
    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", precision = 15, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "day_change", precision = 15, scale = 4)
    private BigDecimal dayChange;

    @Column(name = "day_change_percent", precision = 8, scale = 4)
    private BigDecimal dayChangePercent;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "day_high", precision = 15, scale = 4)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 15, scale = 4)
    private BigDecimal dayLow;

    @Column(name = "week_52_high", precision = 15, scale = 4)
    private BigDecimal week52High;

    @Column(name = "week_52_low", precision = 15, scale = 4)
    private BigDecimal week52Low;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "bid_price", precision = 15, scale = 4)
    private BigDecimal bidPrice;

    @Column(name = "ask_price", precision = 15, scale = 4)
    private BigDecimal askPrice;

    // Company info
    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "industry", length = 150)
    private String industry;

    @Column(name = "exchange", length = 50)
    private String exchange;

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    // Financial metrics
    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "beta", precision = 8, scale = 4)
    private BigDecimal beta;

    @Column(name = "eps", precision = 10, scale = 4)
    private BigDecimal eps;

    // Metadata
    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "market_status", length = 20)
    private String marketStatus;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relationships (prevent circular JSON serialization)
    @JsonIgnore
    @OneToMany(mappedBy = "marketData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Asset> assets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "marketData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<News> news = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "marketData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ESGRating> esgRatings = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "marketData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceHistory> priceHistories = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    // Constructors
    public MarketData() {}

    public MarketData(String symbol, String name, AssetType assetType) {
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(BigDecimal dayHigh) {
        this.dayHigh = dayHigh;
    }

    public BigDecimal getDayLow() {
        return dayLow;
    }

    public void setDayLow(BigDecimal dayLow) {
        this.dayLow = dayLow;
    }

    public BigDecimal getWeek52High() {
        return week52High;
    }

    public void setWeek52High(BigDecimal week52High) {
        this.week52High = week52High;
    }

    public BigDecimal getWeek52Low() {
        return week52Low;
    }

    public void setWeek52Low(BigDecimal week52Low) {
        this.week52Low = week52Low;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public BigDecimal getPeRatio() {
        return peRatio;
    }

    public void setPeRatio(BigDecimal peRatio) {
        this.peRatio = peRatio;
    }

    public BigDecimal getBeta() {
        return beta;
    }

    public void setBeta(BigDecimal beta) {
        this.beta = beta;
    }

    public BigDecimal getEps() {
        return eps;
    }

    public void setEps(BigDecimal eps) {
        this.eps = eps;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getMarketStatus() {
        return marketStatus;
    }

    public void setMarketStatus(String marketStatus) {
        this.marketStatus = marketStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    // TODO: Fix PriceHistory getters/setters
    // public List<PriceHistory> getPriceHistories() {
    //     return priceHistories;
    // }

    // public void setPriceHistories(List<PriceHistory> priceHistories) {
    //     this.priceHistories = priceHistories;
    // }

    @Override
    public String toString() {
        return "MarketData{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                ", sector='" + sector + '\'' +
                '}';
    }
}
