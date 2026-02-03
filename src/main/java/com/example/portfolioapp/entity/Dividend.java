package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dividend entity matching the simplified dividends table.
 * 
 * CRITICAL CALCULATION LOGIC:
 * ✅ shares_held - Retrieved from assets.quantity (not from API)
 * ✅ total_amount - Calculated as amount_per_share × shares_held
 * 
 * Yahoo Finance API only provides:
 * - payment_date (Date)
 * - amount_per_share (Dividend per share)
 * 
 * This entity implements the same calculation logic as the Python script.
 */
@Entity
@Table(name = "dividends",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_dividend_asset_payment", 
           columnNames = {"asset_id", "payment_date"}
       ),
       indexes = {
           @Index(name = "idx_dividend_asset", columnList = "asset_id"),
           @Index(name = "idx_dividend_payment_date", columnList = "payment_date")
       })
public class Dividend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount_per_share", nullable = false, precision = 15, scale = 8)
    private BigDecimal amountPerShare;

    /**
     * shares_held: Retrieved from assets.quantity at the time of dividend recording.
     * This is NOT from the Yahoo Finance API.
     */
    @Column(name = "shares_held", precision = 15, scale = 8)
    private BigDecimal sharesHeld;

    /**
     * total_amount: Calculated as amount_per_share × shares_held.
     * This is NOT from the Yahoo Finance API.
     */
    @Column(name = "total_amount", precision = 15, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Dividend() {}

    public Dividend(Asset asset, LocalDate paymentDate, BigDecimal amountPerShare) {
        this.asset = asset;
        this.paymentDate = paymentDate;
        this.amountPerShare = amountPerShare;
    }

    // Business logic methods
    
    /**
     * Calculate total dividend amount based on shares held.
     * This implements the core calculation logic: total = amount_per_share × shares_held
     * 
     * CRITICAL: This method must be called before saving the dividend to ensure
     * sharesHeld and totalAmount are properly set.
     * 
     * @param sharesHeldAtPayment The number of shares the user held at payment date
     */
    public void calculateTotalAmount(BigDecimal sharesHeldAtPayment) {
        if (sharesHeldAtPayment == null || amountPerShare == null) {
            throw new IllegalStateException("Cannot calculate total amount: sharesHeld or amountPerShare is null");
        }
        
        this.sharesHeld = sharesHeldAtPayment;
        this.totalAmount = amountPerShare.multiply(sharesHeld)
                                        .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Automatically calculate total amount from the asset's current quantity.
     * Assumes the asset's quantity represents the shares held at payment date.
     */
    public void calculateTotalAmountFromAsset() {
        if (asset == null) {
            throw new IllegalStateException("Cannot calculate total amount: asset is null");
        }
        
        BigDecimal assetQuantity = asset.getQuantity();
        if (assetQuantity == null) {
            throw new IllegalStateException(
                "Cannot calculate total amount: asset.quantity is null. " +
                "Ensure the asset has a valid quantity before recording dividends."
            );
        }
        
        calculateTotalAmount(assetQuantity);
    }

    /**
     * Validate that the dividend has been properly calculated before saving.
     * This ensures data integrity.
     */
    public void validate() {
        if (sharesHeld == null || totalAmount == null) {
            throw new IllegalStateException(
                "Dividend must be calculated before saving. " +
                "Call calculateTotalAmount() or calculateTotalAmountFromAsset() first."
            );
        }
        
        if (amountPerShare == null || amountPerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount_per_share must be greater than zero");
        }
        
        if (sharesHeld.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("shares_held must be greater than zero");
        }
    }

    /**
     * Static factory method to create a properly calculated dividend.
     * This is the recommended way to create dividend instances.
     * 
     * @param asset The asset receiving the dividend
     * @param paymentDate The dividend payment date
     * @param amountPerShare The dividend amount per share (from Yahoo Finance)
     * @return A fully calculated Dividend instance ready to save
     */
    public static Dividend create(Asset asset, LocalDate paymentDate, BigDecimal amountPerShare) {
        if (!asset.isOwned()) {
            throw new IllegalArgumentException(
                "Cannot create dividend for non-owned asset. Status: " + asset.getStatus()
            );
        }
        
        Dividend dividend = new Dividend(asset, paymentDate, amountPerShare);
        dividend.setCurrency(asset.getMarketData().getCurrency());
        dividend.calculateTotalAmountFromAsset();
        dividend.validate();
        
        return dividend;
    }

    /**
     * Get the annualized dividend estimate (assuming quarterly payments).
     * This is a convenience method for analysis.
     */
    public BigDecimal getEstimatedAnnualDividend() {
        if (amountPerShare == null) {
            return BigDecimal.ZERO;
        }
        // Assume quarterly dividends (4 per year)
        return amountPerShare.multiply(BigDecimal.valueOf(4));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmountPerShare() {
        return amountPerShare;
    }

    public void setAmountPerShare(BigDecimal amountPerShare) {
        this.amountPerShare = amountPerShare;
    }

    public BigDecimal getSharesHeld() {
        return sharesHeld;
    }

    public void setSharesHeld(BigDecimal sharesHeld) {
        this.sharesHeld = sharesHeld;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Dividend{" +
                "id=" + id +
                ", paymentDate=" + paymentDate +
                ", amountPerShare=" + amountPerShare +
                ", sharesHeld=" + sharesHeld +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dividend)) return false;
        Dividend dividend = (Dividend) o;
        return asset.getId().equals(dividend.asset.getId()) && 
               paymentDate.equals(dividend.paymentDate);
    }

    @Override
    public int hashCode() {
        return asset.getId().hashCode() + paymentDate.hashCode();
    }
}
