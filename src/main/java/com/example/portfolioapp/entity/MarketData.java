package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "market_data", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"asset_type_id", "symbol"}))
public class MarketData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;
    
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "previous_close", precision = 10, scale = 2)
    private BigDecimal previousClose;
    
    @Column(name = "day_change", precision = 10, scale = 2)
    private BigDecimal dayChange;
    
    @Column(name = "day_change_percent", precision = 5, scale = 2)
    private BigDecimal dayChangePercent;
    
    @Column(precision = 15, scale = 2)
    private Long volume;
    
    @Column(name = "day_high", precision = 10, scale = 2)
    private BigDecimal dayHigh;
    
    @Column(name = "day_low", precision = 10, scale = 2)
    private BigDecimal dayLow;
    
    @Column(name = "week_52_high", precision = 10, scale = 2)
    private BigDecimal week52High;
    
    @Column(name = "week_52_low", precision = 10, scale = 2)
    private BigDecimal week52Low;
    
    @Column(name = "market_cap")
    private Long marketCap;
    
    @Column(name = "bid_price", precision = 10, scale = 2)
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price", precision = 10, scale = 2)
    private BigDecimal askPrice;
    
    @Column(name = "bid_size")
    private Long bidSize;
    
    @Column(name = "ask_size")
    private Long askSize;
    
    // Stock-specific market data
    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;
    
    @Column(name = "pe_ratio", precision = 8, scale = 2)
    private BigDecimal peRatio;
    
    @Column(name = "earnings_per_share", precision = 8, scale = 2)
    private BigDecimal earningsPerShare;
    
    @Column(precision = 6, scale = 3)
    private BigDecimal beta;
    
    @Column(length = 50)
    private String sector;
    
    @Column(length = 100)
    private String industry;
    
    // Bond-specific market data
    @Column(precision = 6, scale = 2)
    private BigDecimal duration;
    
    @Column(name = "yield_to_maturity", precision = 6, scale = 3)
    private BigDecimal yieldToMaturity;
    
    @Column(name = "credit_rating", length = 10)
    private String creditRating;
    
    // Crypto-specific market data
    @Column(length = 50)
    private String blockchain;
    
    @Column(name = "max_supply")
    private Long maxSupply;
    
    @Column(name = "circulating_supply")
    private Long circulatingSupply;
    
    @Column(name = "total_supply")
    private Long totalSupply;
    
    @Column(name = "market_cap_rank")
    private Integer marketCapRank;
    
    @Column(name = "volume_24h")
    private Long volume24h;
    
    @Column(name = "change_24h", precision = 5, scale = 2)
    private BigDecimal change24h;
    
    @Column(name = "change_7d", precision = 5, scale = 2)
    private BigDecimal change7d;
    
    @Column(name = "change_30d", precision = 5, scale = 2)
    private BigDecimal change30d;
    
    // Data source and freshness tracking
    @Column(name = "data_source", length = 50)
    private String dataSource; // "YAHOO", "COINBASE", "BINANCE", etc.
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "market_status", length = 20)
    private String marketStatus; // "OPEN", "CLOSED", "PRE_MARKET", "AFTER_HOURS"
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "exchange", length = 50)
    private String exchange;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "marketData", fetch = FetchType.LAZY)
    private List<Asset> assets;
    
    // Constructors
    public MarketData() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public MarketData(AssetType assetType, String symbol, String name) {
        this();
        this.assetType = assetType;
        this.symbol = symbol;
        this.name = name;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }
    
    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }
    
    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }
    
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    
    public BigDecimal getDayHigh() { return dayHigh; }
    public void setDayHigh(BigDecimal dayHigh) { this.dayHigh = dayHigh; }
    
    public BigDecimal getDayLow() { return dayLow; }
    public void setDayLow(BigDecimal dayLow) { this.dayLow = dayLow; }
    
    public BigDecimal getWeek52High() { return week52High; }
    public void setWeek52High(BigDecimal week52High) { this.week52High = week52High; }
    
    public BigDecimal getWeek52Low() { return week52Low; }
    public void setWeek52Low(BigDecimal week52Low) { this.week52Low = week52Low; }
    
    public Long getMarketCap() { return marketCap; }
    public void setMarketCap(Long marketCap) { this.marketCap = marketCap; }
    
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    
    public Long getBidSize() { return bidSize; }
    public void setBidSize(Long bidSize) { this.bidSize = bidSize; }
    
    public Long getAskSize() { return askSize; }
    public void setAskSize(Long askSize) { this.askSize = askSize; }
    
    // Stock-specific getters/setters
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    
    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    
    public BigDecimal getEarningsPerShare() { return earningsPerShare; }
    public void setEarningsPerShare(BigDecimal earningsPerShare) { this.earningsPerShare = earningsPerShare; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    
    // Bond-specific getters/setters
    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }
    
    public BigDecimal getYieldToMaturity() { return yieldToMaturity; }
    public void setYieldToMaturity(BigDecimal yieldToMaturity) { this.yieldToMaturity = yieldToMaturity; }
    
    public String getCreditRating() { return creditRating; }
    public void setCreditRating(String creditRating) { this.creditRating = creditRating; }
    
    // Crypto-specific getters/setters
    public String getBlockchain() { return blockchain; }
    public void setBlockchain(String blockchain) { this.blockchain = blockchain; }
    
    public Long getMaxSupply() { return maxSupply; }
    public void setMaxSupply(Long maxSupply) { this.maxSupply = maxSupply; }
    
    public Long getCirculatingSupply() { return circulatingSupply; }
    public void setCirculatingSupply(Long circulatingSupply) { this.circulatingSupply = circulatingSupply; }
    
    public Long getTotalSupply() { return totalSupply; }
    public void setTotalSupply(Long totalSupply) { this.totalSupply = totalSupply; }
    
    public Integer getMarketCapRank() { return marketCapRank; }
    public void setMarketCapRank(Integer marketCapRank) { this.marketCapRank = marketCapRank; }
    
    public Long getVolume24h() { return volume24h; }
    public void setVolume24h(Long volume24h) { this.volume24h = volume24h; }
    
    public BigDecimal getChange24h() { return change24h; }
    public void setChange24h(BigDecimal change24h) { this.change24h = change24h; }
    
    public BigDecimal getChange7d() { return change7d; }
    public void setChange7d(BigDecimal change7d) { this.change7d = change7d; }
    
    public BigDecimal getChange30d() { return change30d; }
    public void setChange30d(BigDecimal change30d) { this.change30d = change30d; }
    
    // Data tracking getters/setters
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public String getMarketStatus() { return marketStatus; }
    public void setMarketStatus(String marketStatus) { this.marketStatus = marketStatus; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    public List<Asset> getAssets() { return assets; }
    public void setAssets(List<Asset> assets) { this.assets = assets; }
    
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
    
    @Override
    public String toString() {
        return "MarketData{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                ", lastUpdated=" + lastUpdated +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}