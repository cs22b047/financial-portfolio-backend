package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks corporate actions affecting positions (splits, mergers, spin-offs, etc.).
 */
@Entity
@Table(name = "corporate_actions",
       indexes = {
           @Index(name = "idx_corp_actions_instrument", columnList = "instrument_id"),
           @Index(name = "idx_corp_actions_date", columnList = "effective_date")
       })
public class CorporateAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private CorporateActionType actionType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    @Column(name = "record_date")
    private LocalDate recordDate;

    // Split details
    @Column(name = "split_ratio_from", precision = 10, scale = 4)
    private BigDecimal splitRatioFrom; // e.g., 1 (for 4:1 split)

    @Column(name = "split_ratio_to", precision = 10, scale = 4)
    private BigDecimal splitRatioTo; // e.g., 4 (for 4:1 split)

    // Merger/Acquisition details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_instrument_id")
    private Instrument targetInstrument; // New instrument received

    @Column(name = "exchange_ratio", precision = 10, scale = 6)
    private BigDecimal exchangeRatio; // Shares of new stock per old share

    @Column(name = "cash_component", precision = 18, scale = 2)
    private BigDecimal cashComponent; // Cash received per share

    // Spin-off details
    @Column(name = "spinoff_ratio", precision = 10, scale = 6)
    private BigDecimal spinoffRatio; // Shares of spinoff per original share

    @Column(name = "cost_basis_allocation", precision = 6, scale = 4)
    private BigDecimal costBasisAllocation; // % of cost basis to allocate to spinoff

    // Status tracking
    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "positions_affected")
    private Integer positionsAffected;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public CorporateAction() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Factory method for stock split
    public static CorporateAction createStockSplit(Instrument instrument, LocalDate effectiveDate,
                                                   int from, int to) {
        CorporateAction action = new CorporateAction();
        action.setInstrument(instrument);
        action.setActionType(from < to ? CorporateActionType.STOCK_SPLIT : CorporateActionType.REVERSE_SPLIT);
        action.setEffectiveDate(effectiveDate);
        action.setSplitRatioFrom(BigDecimal.valueOf(from));
        action.setSplitRatioTo(BigDecimal.valueOf(to));
        action.setDescription(to + "-for-" + from + " stock split");
        return action;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public CorporateActionType getActionType() { return actionType; }
    public void setActionType(CorporateActionType actionType) { this.actionType = actionType; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public BigDecimal getSplitRatioFrom() { return splitRatioFrom; }
    public void setSplitRatioFrom(BigDecimal splitRatioFrom) { this.splitRatioFrom = splitRatioFrom; }

    public BigDecimal getSplitRatioTo() { return splitRatioTo; }
    public void setSplitRatioTo(BigDecimal splitRatioTo) { this.splitRatioTo = splitRatioTo; }

    public Instrument getTargetInstrument() { return targetInstrument; }
    public void setTargetInstrument(Instrument targetInstrument) { this.targetInstrument = targetInstrument; }

    public BigDecimal getExchangeRatio() { return exchangeRatio; }
    public void setExchangeRatio(BigDecimal exchangeRatio) { this.exchangeRatio = exchangeRatio; }

    public BigDecimal getCashComponent() { return cashComponent; }
    public void setCashComponent(BigDecimal cashComponent) { this.cashComponent = cashComponent; }

    public BigDecimal getSpinoffRatio() { return spinoffRatio; }
    public void setSpinoffRatio(BigDecimal spinoffRatio) { this.spinoffRatio = spinoffRatio; }

    public BigDecimal getCostBasisAllocation() { return costBasisAllocation; }
    public void setCostBasisAllocation(BigDecimal costBasisAllocation) { this.costBasisAllocation = costBasisAllocation; }

    public Boolean getIsProcessed() { return isProcessed; }
    public void setIsProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; }

    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }

    public Integer getPositionsAffected() { return positionsAffected; }
    public void setPositionsAffected(Integer positionsAffected) { this.positionsAffected = positionsAffected; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    // Helper methods
    public BigDecimal getSplitMultiplier() {
        if (splitRatioFrom == null || splitRatioTo == null ||
            splitRatioFrom.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return splitRatioTo.divide(splitRatioFrom, 6, java.math.RoundingMode.HALF_UP);
    }

    public boolean isSplit() {
        return actionType == CorporateActionType.STOCK_SPLIT ||
               actionType == CorporateActionType.REVERSE_SPLIT;
    }

    public boolean isMergerOrAcquisition() {
        return actionType == CorporateActionType.MERGER ||
               actionType == CorporateActionType.ACQUISITION;
    }

    public void markProcessed(int positionsAffected) {
        this.isProcessed = true;
        this.processedDate = LocalDateTime.now();
        this.positionsAffected = positionsAffected;
    }

    @Override
    public String toString() {
        return "CorporateAction{" +
                "id=" + id +
                ", type=" + actionType +
                ", symbol=" + (instrument != null ? instrument.getSymbol() : null) +
                ", effectiveDate=" + effectiveDate +
                ", processed=" + isProcessed +
                '}';
    }
}
