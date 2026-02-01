package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal fees;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(length = 3)
    private String currency;
    
    // Stock-specific transaction fields
    @Column(name = "dividend_per_share", precision = 8, scale = 4)
    private BigDecimal dividendPerShare;
    
    @Column(name = "split_ratio", precision = 6, scale = 2)
    private BigDecimal splitRatio;
    
    // Bond-specific transaction fields
    @Column(name = "accrued_interest", precision = 8, scale = 2)
    private BigDecimal accruedInterest;
    
    @Column(name = "yield_to_maturity", precision = 6, scale = 3)
    private BigDecimal yieldToMaturity;
    
    // Cash-specific transaction fields
    @Column(name = "account_balance", precision = 15, scale = 2)
    private BigDecimal accountBalance;
    
    @Column(name = "transfer_type", length = 50)
    private String transferType;
    
    // Crypto-specific transaction fields
    @Column(name = "gas_fee", precision = 12, scale = 8)
    private BigDecimal gasFee;
    
    @Column(name = "transaction_hash", length = 100)
    private String transactionHash;
    
    @Column(name = "block_number")
    private Long blockNumber;
    
    @Column(name = "network_fee", precision = 12, scale = 8)
    private BigDecimal networkFee;
    
    @Column(name = "staking_reward", precision = 12, scale = 8)
    private BigDecimal stakingReward;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Constructors
    public Transaction() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public Transaction(Asset asset, TransactionType transactionType, BigDecimal quantity, 
                      BigDecimal price, LocalDate transactionDate) {
        this();
        this.asset = asset;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    // Stock-specific getters/setters
    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }
    
    public BigDecimal getSplitRatio() { return splitRatio; }
    public void setSplitRatio(BigDecimal splitRatio) { this.splitRatio = splitRatio; }
    
    // Bond-specific getters/setters
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    
    public BigDecimal getYieldToMaturity() { return yieldToMaturity; }
    public void setYieldToMaturity(BigDecimal yieldToMaturity) { this.yieldToMaturity = yieldToMaturity; }
    
    // Cash-specific getters/setters
    public BigDecimal getAccountBalance() { return accountBalance; }
    public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
    
    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }
    
    // Crypto-specific getters/setters
    public BigDecimal getGasFee() { return gasFee; }
    public void setGasFee(BigDecimal gasFee) { this.gasFee = gasFee; }
    
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    
    public BigDecimal getNetworkFee() { return networkFee; }
    public void setNetworkFee(BigDecimal networkFee) { this.networkFee = networkFee; }
    
    public BigDecimal getStakingReward() { return stakingReward; }
    public void setStakingReward(BigDecimal stakingReward) { this.stakingReward = stakingReward; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    // Calculated methods
    public BigDecimal getTotalValue() {
        if (quantity != null && price != null) {
            return quantity.multiply(price);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalCost() {
        BigDecimal totalValue = getTotalValue();
        if (fees != null) {
            return totalValue.add(fees);
        }
        return totalValue;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionType=" + transactionType +
                ", quantity=" + quantity +
                ", price=" + price +
                ", transactionDate=" + transactionDate +
                '}';
    }
}