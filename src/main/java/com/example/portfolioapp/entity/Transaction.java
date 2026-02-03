package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity matching the simplified transactions table.
 * Records all transaction activities (BUY, SELL, DIVIDEND, etc.)
 * 
 * Relationships:
 * - Many Transactions -> One Asset
 */
@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_transaction_asset", columnList = "asset_id"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type"),
           @Index(name = "idx_transaction_date", columnList = "transaction_date")
       })
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "quantity", precision = 15, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price", precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "fees", precision = 15, scale = 4)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Constructors
    public Transaction() {}

    public Transaction(Asset asset, TransactionType transactionType, BigDecimal quantity, 
                      BigDecimal price, LocalDateTime transactionDate) {
        this.asset = asset;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
    }

    // Business logic methods
    /**
     * Calculate total transaction value (quantity × price)
     */
    public BigDecimal getTotalValue() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(price);
    }

    /**
     * Calculate total cost including fees
     */
    public BigDecimal getTotalCostWithFees() {
        BigDecimal totalValue = getTotalValue();
        if (fees != null) {
            return totalValue.add(fees);
        }
        return totalValue;
    }

    /**
     * Check if this is a buy transaction
     */
    public boolean isBuy() {
        return transactionType == TransactionType.BUY;
    }

    /**
     * Check if this is a sell transaction
     */
    public boolean isSell() {
        return transactionType == TransactionType.SELL;
    }

    /**
     * Check if this is a dividend transaction
     */
    public boolean isDividend() {
        return transactionType == TransactionType.DIVIDEND;
    }

    // Static factory methods
    public static Transaction createBuy(Asset asset, BigDecimal quantity, BigDecimal price, 
                                       LocalDateTime date, BigDecimal fees) {
        Transaction transaction = new Transaction(asset, TransactionType.BUY, quantity, price, date);
        transaction.setFees(fees);
        return transaction;
    }

    public static Transaction createSell(Asset asset, BigDecimal quantity, BigDecimal price, 
                                        LocalDateTime date, BigDecimal fees) {
        Transaction transaction = new Transaction(asset, TransactionType.SELL, quantity, price, date);
        transaction.setFees(fees);
        return transaction;
    }

    public static Transaction createDividend(Asset asset, BigDecimal totalAmount, LocalDateTime date) {
        // For dividend transactions, quantity and price are not applicable
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setTransactionType(TransactionType.DIVIDEND);
        transaction.setPrice(totalAmount); // Store total dividend amount in price field
        transaction.setTransactionDate(date);
        return transaction;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
