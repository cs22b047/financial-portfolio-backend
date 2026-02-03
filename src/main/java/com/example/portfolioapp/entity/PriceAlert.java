package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Price alerts for positions on watchlist or owned.
 */
@Entity
@Table(name = "price_alerts",
       indexes = {
           @Index(name = "idx_price_alerts_position", columnList = "position_id"),
           @Index(name = "idx_price_alerts_active", columnList = "is_active")
       })
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private AlertType alertType;

    @Column(name = "target_price", precision = 18, scale = 8)
    private BigDecimal targetPrice;

    @Column(name = "target_percent", precision = 8, scale = 4)
    private BigDecimal targetPercent; // For percentage-based alerts

    @Column(name = "reference_price", precision = 18, scale = 8)
    private BigDecimal referencePrice; // Price when alert was created

    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_triggered")
    private Boolean isTriggered = false;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "triggered_price", precision = 18, scale = 8)
    private BigDecimal triggeredPrice;

    // Notification settings
    @Column(name = "notify_email")
    private Boolean notifyEmail = false;

    @Column(name = "notify_push")
    private Boolean notifyPush = true;

    @Column(name = "notify_sms")
    private Boolean notifySms = false;

    // Repeat settings
    @Column(name = "is_recurring")
    private Boolean isRecurring = false; // Alert resets after triggering

    @Column(name = "times_triggered")
    private Integer timesTriggered = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public PriceAlert() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public PriceAlert(Position position, AlertType alertType, BigDecimal targetPrice) {
        this();
        this.position = position;
        this.alertType = alertType;
        this.targetPrice = targetPrice;
        if (position.getInstrument() != null) {
            this.referencePrice = position.getInstrument().getCurrentPrice();
        }
    }

    // Factory methods
    public static PriceAlert createPriceAboveAlert(Position position, BigDecimal targetPrice) {
        return new PriceAlert(position, AlertType.PRICE_ABOVE, targetPrice);
    }

    public static PriceAlert createPriceBelowAlert(Position position, BigDecimal targetPrice) {
        return new PriceAlert(position, AlertType.PRICE_BELOW, targetPrice);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public BigDecimal getTargetPercent() { return targetPercent; }
    public void setTargetPercent(BigDecimal targetPercent) { this.targetPercent = targetPercent; }

    public BigDecimal getReferencePrice() { return referencePrice; }
    public void setReferencePrice(BigDecimal referencePrice) { this.referencePrice = referencePrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsTriggered() { return isTriggered; }
    public void setIsTriggered(Boolean isTriggered) { this.isTriggered = isTriggered; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public BigDecimal getTriggeredPrice() { return triggeredPrice; }
    public void setTriggeredPrice(BigDecimal triggeredPrice) { this.triggeredPrice = triggeredPrice; }

    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }

    public Boolean getNotifyPush() { return notifyPush; }
    public void setNotifyPush(Boolean notifyPush) { this.notifyPush = notifyPush; }

    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean notifySms) { this.notifySms = notifySms; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }

    public Integer getTimesTriggered() { return timesTriggered; }
    public void setTimesTriggered(Integer timesTriggered) { this.timesTriggered = timesTriggered; }

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

    /**
     * Check if alert condition is met based on current price.
     */
    public boolean isConditionMet(BigDecimal currentPrice) {
        if (currentPrice == null || targetPrice == null) return false;

        switch (alertType) {
            case PRICE_ABOVE:
                return currentPrice.compareTo(targetPrice) >= 0;
            case PRICE_BELOW:
                return currentPrice.compareTo(targetPrice) <= 0;
            case TARGET_REACHED:
                return currentPrice.compareTo(targetPrice) == 0;
            case PERCENT_CHANGE_UP:
                if (referencePrice == null || targetPercent == null) return false;
                BigDecimal changeUp = currentPrice.subtract(referencePrice)
                        .divide(referencePrice, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                return changeUp.compareTo(targetPercent) >= 0;
            case PERCENT_CHANGE_DOWN:
                if (referencePrice == null || targetPercent == null) return false;
                BigDecimal changeDown = referencePrice.subtract(currentPrice)
                        .divide(referencePrice, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                return changeDown.compareTo(targetPercent) >= 0;
            default:
                return false;
        }
    }

    /**
     * Trigger the alert.
     */
    public void trigger(BigDecimal currentPrice) {
        this.isTriggered = true;
        this.triggeredAt = LocalDateTime.now();
        this.triggeredPrice = currentPrice;
        this.timesTriggered++;

        if (!isRecurring) {
            this.isActive = false;
        } else {
            // Reset for recurring alerts
            this.isTriggered = false;
            this.referencePrice = currentPrice;
        }
    }

    public String getSymbol() {
        return position != null && position.getInstrument() != null
                ? position.getInstrument().getSymbol() : null;
    }

    @Override
    public String toString() {
        return "PriceAlert{" +
                "id=" + id +
                ", symbol=" + getSymbol() +
                ", type=" + alertType +
                ", target=" + targetPrice +
                ", active=" + isActive +
                '}';
    }
}
