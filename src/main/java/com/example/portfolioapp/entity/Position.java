package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a portfolio position (holding or watchlist item).
 * Replaces the old Asset entity with cleaner separation of concerns.
 *
 * Portfolio data only - market data comes from linked Instrument.
 */
@Entity
@Table(name = "positions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"instrument_id", "status"}))
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositionStatus status = PositionStatus.WATCHLIST;

    // Holding data (for OPEN positions)
    @Column(name = "total_quantity", precision = 18, scale = 8)
    private BigDecimal totalQuantity;

    @Column(name = "average_cost_basis", precision = 18, scale = 8)
    private BigDecimal averageCostBasis; // Weighted average cost per unit

    @Column(name = "total_cost_basis", precision = 18, scale = 2)
    private BigDecimal totalCostBasis; // Total invested amount

    @Column(name = "first_purchase_date")
    private LocalDate firstPurchaseDate;

    @Column(name = "last_purchase_date")
    private LocalDate lastPurchaseDate;

    // Watchlist data (for WATCHLIST/RESEARCH positions)
    @Column(name = "target_buy_price", precision = 18, scale = 8)
    private BigDecimal targetBuyPrice;

    @Column(name = "target_sell_price", precision = 18, scale = 8)
    private BigDecimal targetSellPrice;

    @Column(name = "added_to_watchlist_date")
    private LocalDate addedToWatchlistDate;

    @Column(name = "priority_rank")
    private Integer priorityRank; // 1-5 for purchase priority

    // User notes and settings
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "alerts_enabled")
    private Boolean alertsEnabled = false;

    // Tax settings for this position
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_basis_method", length = 20)
    private CostBasisMethod costBasisMethod;

    // Timestamps
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relationships
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<TaxLot> taxLots;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PortfolioTransaction> transactions;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PriceAlert> priceAlerts;

    // Constructors
    public Position() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.costBasisMethod = CostBasisMethod.FIFO;
    }

    public Position(Instrument instrument, PositionStatus status) {
        this();
        this.instrument = instrument;
        this.status = status;
        if (status.isWatching()) {
            this.addedToWatchlistDate = LocalDate.now();
        }
    }

    // For creating an owned position
    public static Position createOwned(Instrument instrument, BigDecimal quantity, BigDecimal costBasis) {
        Position position = new Position(instrument, PositionStatus.OPEN);
        position.setTotalQuantity(quantity);
        position.setAverageCostBasis(costBasis);
        position.setTotalCostBasis(quantity.multiply(costBasis));
        position.setFirstPurchaseDate(LocalDate.now());
        position.setLastPurchaseDate(LocalDate.now());
        return position;
    }

    // For creating a watchlist position
    public static Position createWatchlist(Instrument instrument, BigDecimal targetPrice) {
        Position position = new Position(instrument, PositionStatus.WATCHLIST);
        position.setTargetBuyPrice(targetPrice);
        return position;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public PositionStatus getStatus() { return status; }
    public void setStatus(PositionStatus status) { this.status = status; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getAverageCostBasis() { return averageCostBasis; }
    public void setAverageCostBasis(BigDecimal averageCostBasis) { this.averageCostBasis = averageCostBasis; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public LocalDate getFirstPurchaseDate() { return firstPurchaseDate; }
    public void setFirstPurchaseDate(LocalDate firstPurchaseDate) { this.firstPurchaseDate = firstPurchaseDate; }

    public LocalDate getLastPurchaseDate() { return lastPurchaseDate; }
    public void setLastPurchaseDate(LocalDate lastPurchaseDate) { this.lastPurchaseDate = lastPurchaseDate; }

    public BigDecimal getTargetBuyPrice() { return targetBuyPrice; }
    public void setTargetBuyPrice(BigDecimal targetBuyPrice) { this.targetBuyPrice = targetBuyPrice; }

    public BigDecimal getTargetSellPrice() { return targetSellPrice; }
    public void setTargetSellPrice(BigDecimal targetSellPrice) { this.targetSellPrice = targetSellPrice; }

    public LocalDate getAddedToWatchlistDate() { return addedToWatchlistDate; }
    public void setAddedToWatchlistDate(LocalDate addedToWatchlistDate) { this.addedToWatchlistDate = addedToWatchlistDate; }

    public Integer getPriorityRank() { return priorityRank; }
    public void setPriorityRank(Integer priorityRank) { this.priorityRank = priorityRank; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(Boolean alertsEnabled) { this.alertsEnabled = alertsEnabled; }

    public CostBasisMethod getCostBasisMethod() { return costBasisMethod; }
    public void setCostBasisMethod(CostBasisMethod costBasisMethod) { this.costBasisMethod = costBasisMethod; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public List<TaxLot> getTaxLots() { return taxLots; }
    public void setTaxLots(List<TaxLot> taxLots) { this.taxLots = taxLots; }

    public List<PortfolioTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<PortfolioTransaction> transactions) { this.transactions = transactions; }

    public List<PriceAlert> getPriceAlerts() { return priceAlerts; }
    public void setPriceAlerts(List<PriceAlert> priceAlerts) { this.priceAlerts = priceAlerts; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // ============ Calculated Methods ============

    /**
     * Current market value of the position.
     */
    public BigDecimal getMarketValue() {
        if (!status.isOwned() || totalQuantity == null || instrument == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = instrument.getCurrentPrice();
        if (price == null) return BigDecimal.ZERO;
        return totalQuantity.multiply(price);
    }

    /**
     * Unrealized gain/loss in currency.
     */
    public BigDecimal getUnrealizedGainLoss() {
        if (!status.isOwned() || totalCostBasis == null) {
            return BigDecimal.ZERO;
        }
        return getMarketValue().subtract(totalCostBasis);
    }

    /**
     * Unrealized gain/loss as percentage.
     */
    public BigDecimal getUnrealizedGainLossPercent() {
        if (!status.isOwned() || totalCostBasis == null || totalCostBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealizedGainLoss()
                .divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if target buy price has been reached (for watchlist).
     */
    public boolean isTargetBuyPriceReached() {
        if (targetBuyPrice == null || instrument == null || instrument.getCurrentPrice() == null) {
            return false;
        }
        return instrument.getCurrentPrice().compareTo(targetBuyPrice) <= 0;
    }

    /**
     * Check if target sell price has been reached (for owned positions).
     */
    public boolean isTargetSellPriceReached() {
        if (targetSellPrice == null || instrument == null || instrument.getCurrentPrice() == null) {
            return false;
        }
        return instrument.getCurrentPrice().compareTo(targetSellPrice) >= 0;
    }

    /**
     * Days held since first purchase.
     */
    public long getDaysHeld() {
        if (firstPurchaseDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(firstPurchaseDate, LocalDate.now());
    }

    /**
     * Check if position qualifies for long-term capital gains (held > 1 year).
     */
    public boolean isLongTermHolding() {
        return getDaysHeld() > 365;
    }

    // Helper methods for convenience
    public String getSymbol() {
        return instrument != null ? instrument.getSymbol() : null;
    }

    public String getName() {
        return instrument != null ? instrument.getName() : null;
    }

    public BigDecimal getCurrentPrice() {
        return instrument != null ? instrument.getCurrentPrice() : null;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", symbol=" + getSymbol() +
                ", status=" + status +
                ", quantity=" + totalQuantity +
                ", marketValue=" + getMarketValue() +
                '}';
    }
}
