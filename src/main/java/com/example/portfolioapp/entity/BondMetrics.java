package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bond-specific metrics. One-to-one relationship with Instrument.
 * Only created for instruments where instrumentType is BOND or TREASURY.
 */
@Entity
@Table(name = "bond_metrics")
public class BondMetrics {

    @Id
    private Long id; // Same as instrument_id (shared primary key)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    // Bond fundamentals
    @Column(name = "coupon_rate", precision = 6, scale = 3)
    private BigDecimal couponRate;

    @Column(name = "face_value", precision = 15, scale = 2)
    private BigDecimal faceValue;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_frequency", length = 20)
    private CouponFrequency couponFrequency;

    // Yield metrics
    @Column(name = "yield_to_maturity", precision = 8, scale = 4)
    private BigDecimal yieldToMaturity;

    @Column(name = "yield_to_call", precision = 8, scale = 4)
    private BigDecimal yieldToCall;

    @Column(name = "yield_to_worst", precision = 8, scale = 4)
    private BigDecimal yieldToWorst;

    @Column(name = "current_yield", precision = 8, scale = 4)
    private BigDecimal currentYield;

    // Risk metrics
    @Column(precision = 8, scale = 4)
    private BigDecimal duration; // Modified duration

    @Column(name = "macaulay_duration", precision = 8, scale = 4)
    private BigDecimal macaulayDuration;

    @Column(precision = 10, scale = 4)
    private BigDecimal convexity;

    // Credit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_rating_id")
    private CreditRating creditRating;

    @Column(name = "credit_rating_string", length = 10)
    private String creditRatingString; // Fallback if not linked

    // Call/Put features
    @Column(name = "is_callable")
    private Boolean isCallable = false;

    @Column(name = "call_date")
    private LocalDate callDate;

    @Column(name = "call_price", precision = 10, scale = 4)
    private BigDecimal callPrice;

    @Column(name = "is_puttable")
    private Boolean isPuttable = false;

    @Column(name = "put_date")
    private LocalDate putDate;

    @Column(name = "put_price", precision = 10, scale = 4)
    private BigDecimal putPrice;

    // Bond type classification
    @Enumerated(EnumType.STRING)
    @Column(name = "bond_type", length = 30)
    private BondType bondType;

    @Column(name = "issuer", length = 200)
    private String issuer;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public BondMetrics() {
        this.updatedDate = LocalDateTime.now();
    }

    public BondMetrics(Instrument instrument) {
        this();
        this.instrument = instrument;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public BigDecimal getCouponRate() { return couponRate; }
    public void setCouponRate(BigDecimal couponRate) { this.couponRate = couponRate; }

    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public CouponFrequency getCouponFrequency() { return couponFrequency; }
    public void setCouponFrequency(CouponFrequency couponFrequency) { this.couponFrequency = couponFrequency; }

    public BigDecimal getYieldToMaturity() { return yieldToMaturity; }
    public void setYieldToMaturity(BigDecimal yieldToMaturity) { this.yieldToMaturity = yieldToMaturity; }

    public BigDecimal getYieldToCall() { return yieldToCall; }
    public void setYieldToCall(BigDecimal yieldToCall) { this.yieldToCall = yieldToCall; }

    public BigDecimal getYieldToWorst() { return yieldToWorst; }
    public void setYieldToWorst(BigDecimal yieldToWorst) { this.yieldToWorst = yieldToWorst; }

    public BigDecimal getCurrentYield() { return currentYield; }
    public void setCurrentYield(BigDecimal currentYield) { this.currentYield = currentYield; }

    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }

    public BigDecimal getMacaulayDuration() { return macaulayDuration; }
    public void setMacaulayDuration(BigDecimal macaulayDuration) { this.macaulayDuration = macaulayDuration; }

    public BigDecimal getConvexity() { return convexity; }
    public void setConvexity(BigDecimal convexity) { this.convexity = convexity; }

    public CreditRating getCreditRating() { return creditRating; }
    public void setCreditRating(CreditRating creditRating) { this.creditRating = creditRating; }

    public String getCreditRatingString() { return creditRatingString; }
    public void setCreditRatingString(String creditRatingString) { this.creditRatingString = creditRatingString; }

    public Boolean getIsCallable() { return isCallable; }
    public void setIsCallable(Boolean isCallable) { this.isCallable = isCallable; }

    public LocalDate getCallDate() { return callDate; }
    public void setCallDate(LocalDate callDate) { this.callDate = callDate; }

    public BigDecimal getCallPrice() { return callPrice; }
    public void setCallPrice(BigDecimal callPrice) { this.callPrice = callPrice; }

    public Boolean getIsPuttable() { return isPuttable; }
    public void setIsPuttable(Boolean isPuttable) { this.isPuttable = isPuttable; }

    public LocalDate getPutDate() { return putDate; }
    public void setPutDate(LocalDate putDate) { this.putDate = putDate; }

    public BigDecimal getPutPrice() { return putPrice; }
    public void setPutPrice(BigDecimal putPrice) { this.putPrice = putPrice; }

    public BondType getBondType() { return bondType; }
    public void setBondType(BondType bondType) { this.bondType = bondType; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Helper methods
    public long getDaysToMaturity() {
        if (maturityDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), maturityDate);
    }

    public BigDecimal getYearsToMaturity() {
        long days = getDaysToMaturity();
        if (days < 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP);
    }

    public boolean isMatured() {
        return maturityDate != null && LocalDate.now().isAfter(maturityDate);
    }

    public boolean isInvestmentGrade() {
        return creditRating != null && creditRating.isInvestmentGrade();
    }

    @Override
    public String toString() {
        return "BondMetrics{instrumentId=" + id + ", couponRate=" + couponRate + ", maturityDate=" + maturityDate + "}";
    }

    // Nested enums for bond specifics
    public enum CouponFrequency {
        ANNUAL(1), SEMI_ANNUAL(2), QUARTERLY(4), MONTHLY(12), ZERO_COUPON(0);

        private final int paymentsPerYear;

        CouponFrequency(int paymentsPerYear) {
            this.paymentsPerYear = paymentsPerYear;
        }

        public int getPaymentsPerYear() {
            return paymentsPerYear;
        }
    }

    public enum BondType {
        TREASURY, MUNICIPAL, CORPORATE, AGENCY, MORTGAGE_BACKED, ASSET_BACKED,
        CONVERTIBLE, HIGH_YIELD, INVESTMENT_GRADE, INFLATION_LINKED, ZERO_COUPON
    }
}
