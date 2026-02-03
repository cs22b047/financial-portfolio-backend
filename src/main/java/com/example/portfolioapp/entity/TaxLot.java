package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Individual tax lot for cost basis tracking.
 * Each purchase creates a new tax lot. Sells reduce lots based on cost basis method.
 *
 * Essential for FIFO/LIFO/Specific ID cost basis calculations and wash sale tracking.
 */
@Entity
@Table(name = "tax_lots",
       indexes = {
           @Index(name = "idx_tax_lots_position", columnList = "position_id"),
           @Index(name = "idx_tax_lots_purchase_date", columnList = "purchase_date")
       })
public class TaxLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    // Purchase details
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 18, scale = 8, nullable = false)
    private BigDecimal purchasePrice;

    @Column(name = "original_quantity", precision = 18, scale = 8, nullable = false)
    private BigDecimal originalQuantity;

    @Column(name = "remaining_quantity", precision = 18, scale = 8, nullable = false)
    private BigDecimal remainingQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal fees; // Purchase fees/commissions

    // Cost basis
    @Column(name = "cost_basis_per_unit", precision = 18, scale = 8, nullable = false)
    private BigDecimal costBasisPerUnit; // purchasePrice + (fees / quantity)

    @Column(name = "total_cost_basis", precision = 18, scale = 2)
    private BigDecimal totalCostBasis;

    // Wash sale tracking
    @Column(name = "is_wash_sale")
    private Boolean isWashSale = false;

    @Column(name = "wash_sale_disallowed_loss", precision = 10, scale = 2)
    private BigDecimal washSaleDisallowedLoss;

    @Column(name = "wash_sale_adjusted_basis", precision = 18, scale = 8)
    private BigDecimal washSaleAdjustedBasis;

    // Corporate action adjustments
    @Column(name = "split_adjusted")
    private Boolean splitAdjusted = false;

    @Column(name = "adjustment_factor", precision = 10, scale = 6)
    private BigDecimal adjustmentFactor; // For stock splits

    // Reference to buy transaction
    @Column(name = "buy_transaction_id")
    private Long buyTransactionId;

    // Status
    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public TaxLot() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public TaxLot(Position position, LocalDate purchaseDate, BigDecimal purchasePrice,
                  BigDecimal quantity, BigDecimal fees) {
        this();
        this.position = position;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.fees = fees;
        calculateCostBasis();
    }

    private void calculateCostBasis() {
        BigDecimal feePerUnit = BigDecimal.ZERO;
        if (fees != null && originalQuantity != null && originalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            feePerUnit = fees.divide(originalQuantity, 8, RoundingMode.HALF_UP);
        }
        this.costBasisPerUnit = purchasePrice.add(feePerUnit);
        this.totalCostBasis = costBasisPerUnit.multiply(originalQuantity);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
        calculateCostBasis();
    }

    public BigDecimal getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(BigDecimal originalQuantity) { this.originalQuantity = originalQuantity; }

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            this.isClosed = true;
            this.closedDate = LocalDate.now();
        }
    }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) {
        this.fees = fees;
        calculateCostBasis();
    }

    public BigDecimal getCostBasisPerUnit() { return costBasisPerUnit; }
    public void setCostBasisPerUnit(BigDecimal costBasisPerUnit) { this.costBasisPerUnit = costBasisPerUnit; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public Boolean getIsWashSale() { return isWashSale; }
    public void setIsWashSale(Boolean isWashSale) { this.isWashSale = isWashSale; }

    public BigDecimal getWashSaleDisallowedLoss() { return washSaleDisallowedLoss; }
    public void setWashSaleDisallowedLoss(BigDecimal washSaleDisallowedLoss) { this.washSaleDisallowedLoss = washSaleDisallowedLoss; }

    public BigDecimal getWashSaleAdjustedBasis() { return washSaleAdjustedBasis; }
    public void setWashSaleAdjustedBasis(BigDecimal washSaleAdjustedBasis) { this.washSaleAdjustedBasis = washSaleAdjustedBasis; }

    public Boolean getSplitAdjusted() { return splitAdjusted; }
    public void setSplitAdjusted(Boolean splitAdjusted) { this.splitAdjusted = splitAdjusted; }

    public BigDecimal getAdjustmentFactor() { return adjustmentFactor; }
    public void setAdjustmentFactor(BigDecimal adjustmentFactor) { this.adjustmentFactor = adjustmentFactor; }

    public Long getBuyTransactionId() { return buyTransactionId; }
    public void setBuyTransactionId(Long buyTransactionId) { this.buyTransactionId = buyTransactionId; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public LocalDate getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // ============ Calculated Methods ============

    /**
     * Get effective cost basis (adjusted for wash sales if applicable).
     */
    public BigDecimal getEffectiveCostBasisPerUnit() {
        if (isWashSale && washSaleAdjustedBasis != null) {
            return washSaleAdjustedBasis;
        }
        return costBasisPerUnit;
    }

    /**
     * Get remaining cost basis (for remaining shares).
     */
    public BigDecimal getRemainingCostBasis() {
        return getEffectiveCostBasisPerUnit().multiply(remainingQuantity);
    }

    /**
     * Calculate days held from purchase.
     */
    public long getDaysHeld() {
        LocalDate endDate = isClosed && closedDate != null ? closedDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(purchaseDate, endDate);
    }

    /**
     * Check if this lot qualifies for long-term capital gains.
     */
    public boolean isLongTerm() {
        return getDaysHeld() > 365;
    }

    /**
     * Calculate unrealized gain/loss for remaining shares.
     */
    public BigDecimal getUnrealizedGainLoss(BigDecimal currentPrice) {
        if (currentPrice == null || remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal marketValue = remainingQuantity.multiply(currentPrice);
        return marketValue.subtract(getRemainingCostBasis());
    }

    /**
     * Sell shares from this lot and return the cost basis of sold shares.
     */
    public BigDecimal sellShares(BigDecimal quantityToSell) {
        if (quantityToSell.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException("Cannot sell more than remaining quantity");
        }
        BigDecimal soldCostBasis = getEffectiveCostBasisPerUnit().multiply(quantityToSell);
        setRemainingQuantity(remainingQuantity.subtract(quantityToSell));
        return soldCostBasis;
    }

    /**
     * Adjust for stock split.
     */
    public void applySplit(BigDecimal splitRatio) {
        this.remainingQuantity = remainingQuantity.multiply(splitRatio);
        this.originalQuantity = originalQuantity.multiply(splitRatio);
        this.purchasePrice = purchasePrice.divide(splitRatio, 8, RoundingMode.HALF_UP);
        this.costBasisPerUnit = costBasisPerUnit.divide(splitRatio, 8, RoundingMode.HALF_UP);
        this.splitAdjusted = true;
        this.adjustmentFactor = splitRatio;
    }

    @Override
    public String toString() {
        return "TaxLot{" +
                "id=" + id +
                ", purchaseDate=" + purchaseDate +
                ", remaining=" + remainingQuantity + "/" + originalQuantity +
                ", costBasis=" + costBasisPerUnit +
                ", isLongTerm=" + isLongTerm() +
                '}';
    }
}
