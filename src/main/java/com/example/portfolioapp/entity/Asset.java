package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Asset entity matching the simplified assets table.
 * User's relationship with a stock - either WATCHLIST or OWNED.
 * 
 * Relationships:
 * - Many Assets -> One AssetType
 * - Many Assets -> One MarketData (unique constraint)
 * - One Asset -> Many Transactions
 */
@Entity
@Table(name = "assets",
       uniqueConstraints = @UniqueConstraint(name = "uk_asset_market_data", columnNames = "market_data_id"),
       indexes = {
           @Index(name = "idx_asset_status", columnList = "status"),
           @Index(name = "idx_asset_symbol", columnList = "symbol")
       })
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id", nullable = false, unique = true)
    private MarketData marketData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssetStatus status = AssetStatus.WATCHLIST;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "name", length = 200)
    private String name;

    // Ownership details (NULL for WATCHLIST status)
    @Column(name = "quantity", precision = 15, scale = 8)
    private BigDecimal quantity;

    @Column(name = "purchase_price", precision = 15, scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    // Analysis fields
    @Column(name = "target_price", precision = 15, scale = 4)
    private BigDecimal targetPrice;

    @Column(name = "added_to_watchlist_date")
    private LocalDate addedToWatchlistDate;

    @Column(name = "price_alerts_enabled")
    private Boolean priceAlertsEnabled = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "priority_rank")
    private Integer priorityRank;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alert> alerts = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (addedToWatchlistDate == null && status == AssetStatus.WATCHLIST) {
            addedToWatchlistDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Constructors
    public Asset() {}

    public Asset(MarketData marketData, AssetStatus status) {
        this.marketData = marketData;
        this.status = status;
        this.symbol = marketData.getSymbol();
        this.name = marketData.getName();
        this.assetType = marketData.getAssetType();
    }

    // Business logic methods
    public boolean isOwned() {
        return status == AssetStatus.OWNED;
    }

    public boolean isWatchlist() {
        return status == AssetStatus.WATCHLIST;
    }

    /**
     * Calculate total investment cost
     */
    public BigDecimal getTotalCost() {
        if (quantity == null || purchasePrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(purchasePrice);
    }

    /**
     * Calculate current market value
     */
    public BigDecimal getCurrentValue() {
        if (quantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice);
    }

    /**
     * Calculate unrealized gain/loss
     */
    public BigDecimal getUnrealizedGainLoss() {
        return getCurrentValue().subtract(getTotalCost());
    }

    /**
     * Calculate unrealized gain/loss percentage
     */
    public BigDecimal getUnrealizedGainLossPercent() {
        BigDecimal totalCost = getTotalCost();
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealizedGainLoss()
                .divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get average cost (same as purchase price for now)
     */
    public BigDecimal getAverageCost() {
        return purchasePrice;
    }

    /**
     * Set average cost (sets purchase price)
     */
    public void setAverageCost(BigDecimal averageCost) {
        this.purchasePrice = averageCost;
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

    public MarketData getMarketData() {
        return marketData;
    }

    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public LocalDate getAddedToWatchlistDate() {
        return addedToWatchlistDate;
    }

    public void setAddedToWatchlistDate(LocalDate addedToWatchlistDate) {
        this.addedToWatchlistDate = addedToWatchlistDate;
    }

    public Boolean getPriceAlertsEnabled() {
        return priceAlertsEnabled;
    }

    public void setPriceAlertsEnabled(Boolean priceAlertsEnabled) {
        this.priceAlertsEnabled = priceAlertsEnabled;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getPriorityRank() {
        return priorityRank;
    }

    public void setPriorityRank(Integer priorityRank) {
        this.priorityRank = priorityRank;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", status=" + status +
                ", quantity=" + quantity +
                ", currentPrice=" + currentPrice +
                '}';
    }
}
