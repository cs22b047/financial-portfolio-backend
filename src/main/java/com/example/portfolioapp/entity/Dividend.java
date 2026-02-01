package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dividends")
public class Dividend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dividend_type", nullable = false)
    private DividendType dividendType;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "ex_dividend_date")
    private LocalDate exDividendDate;
    
    @Column(name = "record_date")
    private LocalDate recordDate;
    
    @Column(name = "declaration_date")
    private LocalDate declarationDate;
    
    @Column(name = "amount_per_share", precision = 8, scale = 4, nullable = false)
    private BigDecimal amountPerShare;
    
    @Column(name = "shares_held", precision = 15, scale = 4, nullable = false)
    private BigDecimal sharesHeld;
    
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "tax_withheld", precision = 8, scale = 2)
    private BigDecimal taxWithheld;
    
    @Column(length = 3)
    private String currency;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Stock-specific dividend fields
    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;
    
    @Column(name = "is_special_dividend")
    private Boolean isSpecialDividend = false;
    
    @Column(name = "is_qualified")
    private Boolean isQualified = true;
    
    // Bond-specific income fields
    @Column(name = "coupon_rate", precision = 5, scale = 2)
    private BigDecimal couponRate;
    
    @Column(name = "accrued_interest", precision = 8, scale = 2)
    private BigDecimal accruedInterest;
    
    @Column(name = "face_value", precision = 15, scale = 2)
    private BigDecimal faceValue;
    
    // Cash-specific interest fields
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "compounding_frequency")
    private Integer compoundingFrequency;
    
    @Column(name = "account_balance", precision = 15, scale = 2)
    private BigDecimal accountBalance;
    
    // Crypto-specific reward fields
    @Column(name = "staking_apy", precision = 5, scale = 2)
    private BigDecimal stakingApy;
    
    @Column(name = "validator_fee", precision = 5, scale = 2)
    private BigDecimal validatorFee;
    
    @Column(name = "staked_amount", precision = 15, scale = 8)
    private BigDecimal stakedAmount;
    
    @Column(name = "reward_period_days")
    private Integer rewardPeriodDays;
    
    @Column(name = "blockchain_network", length = 50)
    private String blockchainNetwork;
    
    @Column(name = "transaction_hash", length = 100)
    private String transactionHash;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Constructors
    public Dividend() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public Dividend(Asset asset, DividendType dividendType, LocalDate paymentDate, 
                   BigDecimal amountPerShare, BigDecimal sharesHeld, BigDecimal totalAmount) {
        this();
        this.asset = asset;
        this.dividendType = dividendType;
        this.paymentDate = paymentDate;
        this.amountPerShare = amountPerShare;
        this.sharesHeld = sharesHeld;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    
    public DividendType getDividendType() { return dividendType; }
    public void setDividendType(DividendType dividendType) { this.dividendType = dividendType; }
    
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    
    public LocalDate getExDividendDate() { return exDividendDate; }
    public void setExDividendDate(LocalDate exDividendDate) { this.exDividendDate = exDividendDate; }
    
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    
    public LocalDate getDeclarationDate() { return declarationDate; }
    public void setDeclarationDate(LocalDate declarationDate) { this.declarationDate = declarationDate; }
    
    public BigDecimal getAmountPerShare() { return amountPerShare; }
    public void setAmountPerShare(BigDecimal amountPerShare) { this.amountPerShare = amountPerShare; }
    
    public BigDecimal getSharesHeld() { return sharesHeld; }
    public void setSharesHeld(BigDecimal sharesHeld) { this.sharesHeld = sharesHeld; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getTaxWithheld() { return taxWithheld; }
    public void setTaxWithheld(BigDecimal taxWithheld) { this.taxWithheld = taxWithheld; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Stock-specific getters/setters
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    
    public Boolean getIsSpecialDividend() { return isSpecialDividend; }
    public void setIsSpecialDividend(Boolean isSpecialDividend) { this.isSpecialDividend = isSpecialDividend; }
    
    public Boolean getIsQualified() { return isQualified; }
    public void setIsQualified(Boolean isQualified) { this.isQualified = isQualified; }
    
    // Bond-specific getters/setters
    public BigDecimal getCouponRate() { return couponRate; }
    public void setCouponRate(BigDecimal couponRate) { this.couponRate = couponRate; }
    
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    
    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }
    
    // Cash-specific getters/setters
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public Integer getCompoundingFrequency() { return compoundingFrequency; }
    public void setCompoundingFrequency(Integer compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }
    
    public BigDecimal getAccountBalance() { return accountBalance; }
    public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
    
    // Crypto-specific getters/setters
    public BigDecimal getStakingApy() { return stakingApy; }
    public void setStakingApy(BigDecimal stakingApy) { this.stakingApy = stakingApy; }
    
    public BigDecimal getValidatorFee() { return validatorFee; }
    public void setValidatorFee(BigDecimal validatorFee) { this.validatorFee = validatorFee; }
    
    public BigDecimal getStakedAmount() { return stakedAmount; }
    public void setStakedAmount(BigDecimal stakedAmount) { this.stakedAmount = stakedAmount; }
    
    public Integer getRewardPeriodDays() { return rewardPeriodDays; }
    public void setRewardPeriodDays(Integer rewardPeriodDays) { this.rewardPeriodDays = rewardPeriodDays; }
    
    public String getBlockchainNetwork() { return blockchainNetwork; }
    public void setBlockchainNetwork(String blockchainNetwork) { this.blockchainNetwork = blockchainNetwork; }
    
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    // Calculated methods
    public BigDecimal getNetAmount() {
        if (totalAmount != null) {
            BigDecimal net = totalAmount;
            if (taxWithheld != null) {
                net = net.subtract(taxWithheld);
            }
            return net;
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getAnnualizedYield() {
        if (dividendYield != null) {
            return dividendYield;
        } else if (stakingApy != null) {
            return stakingApy;
        } else if (interestRate != null) {
            return interestRate;
        } else if (couponRate != null) {
            return couponRate;
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return "Dividend{" +
                "id=" + id +
                ", dividendType=" + dividendType +
                ", paymentDate=" + paymentDate +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                '}';
    }
}