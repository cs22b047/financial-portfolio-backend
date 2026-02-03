package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simplified transaction entity for buy/sell operations.
 * Replaces the bloated old Transaction entity.
 *
 * Corporate actions and income are tracked separately.
 */
@Entity
@Table(name = "portfolio_transactions",
       indexes = {
           @Index(name = "idx_transactions_position", columnList = "position_id"),
           @Index(name = "idx_transactions_date", columnList = "transaction_date")
       })
public class PortfolioTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private SimpleTransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(name = "price_per_unit", precision = 18, scale = 8, nullable = false)
    private BigDecimal pricePerUnit;

    @Column(precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount; // quantity * price + fees (for buy) or - fees (for sell)

    // Currency handling
    @Column(name = "currency_code", length = 10)
    private String currencyCode = "USD";

    @Column(name = "exchange_rate", precision = 12, scale = 6)
    private BigDecimal exchangeRate; // Rate to base currency if different

    // Tax lot reference (for sells)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_lot_id")
    private TaxLot taxLot; // Which lot was sold from

    @Column(name = "realized_gain_loss", precision = 18, scale = 2)
    private BigDecimal realizedGainLoss; // For sell transactions

    @Column(name = "is_long_term")
    private Boolean isLongTerm; // For sell transactions

    // Crypto-specific fields (only if needed)
    @Column(name = "blockchain_tx_hash", length = 100)
    private String blockchainTxHash;

    @Column(name = "gas_fee", precision = 18, scale = 8)
    private BigDecimal gasFee;

    // Metadata
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "broker", length = 50)
    private String broker;

    @Column(name = "order_id", length = 50)
    private String orderId; // Broker's order ID

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public PortfolioTransaction() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public PortfolioTransaction(Position position, SimpleTransactionType type,
                                LocalDate date, BigDecimal quantity, BigDecimal price) {
        this();
        this.position = position;
        this.transactionType = type;
        this.transactionDate = date;
        this.quantity = quantity;
        this.pricePerUnit = price;
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        BigDecimal subtotal = quantity.multiply(pricePerUnit);
        BigDecimal feeAmount = fees != null ? fees : BigDecimal.ZERO;

        if (transactionType.increasesPosition()) {
            this.totalAmount = subtotal.add(feeAmount);
        } else {
            this.totalAmount = subtotal.subtract(feeAmount);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public SimpleTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(SimpleTransactionType transactionType) { this.transactionType = transactionType; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
    }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        calculateTotalAmount();
    }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) {
        this.fees = fees;
        calculateTotalAmount();
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public TaxLot getTaxLot() { return taxLot; }
    public void setTaxLot(TaxLot taxLot) { this.taxLot = taxLot; }

    public BigDecimal getRealizedGainLoss() { return realizedGainLoss; }
    public void setRealizedGainLoss(BigDecimal realizedGainLoss) { this.realizedGainLoss = realizedGainLoss; }

    public Boolean getIsLongTerm() { return isLongTerm; }
    public void setIsLongTerm(Boolean isLongTerm) { this.isLongTerm = isLongTerm; }

    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }

    public BigDecimal getGasFee() { return gasFee; }
    public void setGasFee(BigDecimal gasFee) { this.gasFee = gasFee; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Helper methods
    public boolean isBuy() {
        return transactionType == SimpleTransactionType.BUY;
    }

    public boolean isSell() {
        return transactionType == SimpleTransactionType.SELL;
    }

    public BigDecimal getCostBasisPerUnit() {
        if (!isBuy() || quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return pricePerUnit;
        }
        BigDecimal feePerUnit = fees != null ? fees.divide(quantity, 8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return pricePerUnit.add(feePerUnit);
    }

    public BigDecimal getTotalFees() {
        BigDecimal total = fees != null ? fees : BigDecimal.ZERO;
        if (gasFee != null) {
            total = total.add(gasFee);
        }
        return total;
    }

    public String getSymbol() {
        return position != null && position.getInstrument() != null
                ? position.getInstrument().getSymbol() : null;
    }

    @Override
    public String toString() {
        return "PortfolioTransaction{" +
                "id=" + id +
                ", type=" + transactionType +
                ", symbol=" + getSymbol() +
                ", quantity=" + quantity +
                ", price=" + pricePerUnit +
                ", date=" + transactionDate +
                '}';
    }
}
