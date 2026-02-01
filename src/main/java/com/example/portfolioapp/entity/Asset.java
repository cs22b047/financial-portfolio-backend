package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"asset_type_id", "symbol", "status"}))
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id")
    private MarketData marketData;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.WATCHLIST;
    
    @Column(nullable = false, length = 20)
    private String symbol;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(precision = 15, scale = 4)
    private BigDecimal quantity; // nullable - only for owned assets
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice; // nullable - only for owned assets
    
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice; // nullable - derived from marketData
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate; // nullable - only for owned assets
    
    // Watchlist and research fields
    @Column(name = "target_price", precision = 10, scale = 2)
    private BigDecimal targetPrice; // price user wants to buy at
    
    @Column(name = "added_to_watchlist_date")
    private LocalDate addedToWatchlistDate;
    
    @Column(name = "price_alerts_enabled")
    private Boolean priceAlertsEnabled = false;
    
    @Column(columnDefinition = "TEXT")
    private String notes; // user notes about the asset
    
    @Column(name = "priority_rank")
    private Integer priorityRank; // 1-5 ranking for purchase priority
    
    // Stock-specific fields (nullable)
    @Column(length = 50)
    private String sector;
    
    @Column(name = "market_cap")
    private Long marketCap;
    
    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;
    
    @Column(name = "pe_ratio", precision = 8, scale = 2)
    private BigDecimal peRatio;
    
    @Column(precision = 6, scale = 3)
    private BigDecimal beta;
    
    // Bond-specific fields (nullable)
    @Column(name = "coupon_rate", precision = 5, scale = 2)
    private BigDecimal couponRate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Column(name = "credit_rating", length = 10)
    private String creditRating;
    
    @Column(name = "face_value", precision = 15, scale = 2)
    private BigDecimal faceValue;
    
    @Column(precision = 6, scale = 2)
    private BigDecimal duration;
    
    // Cash-specific fields (nullable)
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;
    
    // Crypto-specific fields (nullable)
    @Column(length = 50)
    private String blockchain;
    
    @Column(name = "max_supply")
    private Long maxSupply;
    
    @Column(name = "circulating_supply")
    private Long circulatingSupply;
    
    @Column(name = "market_cap_crypto")
    private Long marketCapCrypto;
    
    @Column(name = "staking_apy", precision = 5, scale = 2)
    private BigDecimal stakingApy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Dividend> dividends;
    
    // Constructors
    public Asset() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.addedToWatchlistDate = LocalDate.now();
    }
    
    public Asset(AssetType assetType, String symbol, String name) {
        this();
        this.assetType = assetType;
        this.symbol = symbol;
        this.name = name;
    }
    
    public Asset(AssetType assetType, String symbol, String name, AssetStatus status) {
        this(assetType, symbol, name);
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    
    public MarketData getMarketData() { return marketData; }
    public void setMarketData(MarketData marketData) { this.marketData = marketData; }
    
    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    // Watchlist and research getters/setters
    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
    
    public LocalDate getAddedToWatchlistDate() { return addedToWatchlistDate; }
    public void setAddedToWatchlistDate(LocalDate addedToWatchlistDate) { this.addedToWatchlistDate = addedToWatchlistDate; }
    
    public Boolean getPriceAlertsEnabled() { return priceAlertsEnabled; }
    public void setPriceAlertsEnabled(Boolean priceAlertsEnabled) { this.priceAlertsEnabled = priceAlertsEnabled; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Integer getPriorityRank() { return priorityRank; }
    public void setPriorityRank(Integer priorityRank) { this.priorityRank = priorityRank; }
    
    // Stock-specific getters/setters
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    
    public Long getMarketCap() { return marketCap; }
    public void setMarketCap(Long marketCap) { this.marketCap = marketCap; }
    
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    
    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    
    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }
    
    // Bond-specific getters/setters
    public BigDecimal getCouponRate() { return couponRate; }
    public void setCouponRate(BigDecimal couponRate) { this.couponRate = couponRate; }
    
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    
    public String getCreditRating() { return creditRating; }
    public void setCreditRating(String creditRating) { this.creditRating = creditRating; }
    
    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }
    
    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }
    
    // Cash-specific getters/setters
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    // Crypto-specific getters/setters
    public String getBlockchain() { return blockchain; }
    public void setBlockchain(String blockchain) { this.blockchain = blockchain; }
    
    public Long getMaxSupply() { return maxSupply; }
    public void setMaxSupply(Long maxSupply) { this.maxSupply = maxSupply; }
    
    public Long getCirculatingSupply() { return circulatingSupply; }
    public void setCirculatingSupply(Long circulatingSupply) { this.circulatingSupply = circulatingSupply; }
    
    public Long getMarketCapCrypto() { return marketCapCrypto; }
    public void setMarketCapCrypto(Long marketCapCrypto) { this.marketCapCrypto = marketCapCrypto; }
    
    public BigDecimal getStakingApy() { return stakingApy; }
    public void setStakingApy(BigDecimal stakingApy) { this.stakingApy = stakingApy; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    
    public List<Dividend> getDividends() { return dividends; }
    public void setDividends(List<Dividend> dividends) { this.dividends = dividends; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    // Calculated methods
    public BigDecimal getTotalValue() {
        if (status.isOwned() && quantity != null) {
            BigDecimal price = getEffectiveCurrentPrice();
            if (price != null) {
                return quantity.multiply(price);
            }
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getGainLoss() {
        if (status.isOwned() && quantity != null && purchasePrice != null) {
            BigDecimal price = getEffectiveCurrentPrice();
            if (price != null) {
                return (price.subtract(purchasePrice)).multiply(quantity);
            }
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getGainLossPercentage() {
        if (status.isOwned() && purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal price = getEffectiveCurrentPrice();
            if (price != null) {
                return ((price.subtract(purchasePrice)).divide(purchasePrice, 4, BigDecimal.ROUND_HALF_UP))
                        .multiply(BigDecimal.valueOf(100));
            }
        }
        return BigDecimal.ZERO;
    }
    
    // Helper methods
    public BigDecimal getEffectiveCurrentPrice() {
        // Try market data first, then fallback to stored current price
        if (marketData != null && marketData.getCurrentPrice() != null) {
            return marketData.getCurrentPrice();
        }
        return currentPrice;
    }
    
    public boolean isOwned() {
        return status.isOwned();
    }
    
    public boolean isWatchlistItem() {
        return status == AssetStatus.WATCHLIST || status == AssetStatus.RESEARCH || status == AssetStatus.PENDING_PURCHASE;
    }
    
    public boolean isTargetPriceMet() {
        if (targetPrice == null) return false;
        BigDecimal price = getEffectiveCurrentPrice();
        return price != null && price.compareTo(targetPrice) <= 0;
    }
    
    public boolean hasStaleMarketData(int maxAgeMinutes) {
        return marketData == null || marketData.isStale(maxAgeMinutes);
    }
    
    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", quantity=" + quantity +
                ", currentPrice=" + getEffectiveCurrentPrice() +
                '}';
    }
}