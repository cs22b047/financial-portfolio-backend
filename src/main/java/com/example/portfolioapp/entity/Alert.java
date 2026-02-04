package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Alert entity representing a price alert for an asset.
 * Users can set alerts to be notified when an asset's price reaches a target.
 * 
 * Relationships:
 * - Many Alerts -> One Asset
 */
@Entity
@Table(name = "alerts",
       indexes = {
           @Index(name = "idx_alerts_asset_id", columnList = "asset_id"),
           @Index(name = "idx_alerts_triggered", columnList = "triggered"),
           @Index(name = "idx_alerts_asset_triggered", columnList = "asset_id, triggered")
       })
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnore
    private Asset asset;

    @Column(name = "target_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "above_or_below", nullable = false, length = 10)
    private AlertDirection aboveOrBelow;

    @Column(name = "triggered", nullable = false)
    private Boolean triggered = false;

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
    public Alert() {
    }

    public Alert(Asset asset, BigDecimal targetPrice, AlertDirection aboveOrBelow) {
        this.asset = asset;
        this.targetPrice = targetPrice;
        this.aboveOrBelow = aboveOrBelow;
        this.triggered = false;
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

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public AlertDirection getAboveOrBelow() {
        return aboveOrBelow;
    }

    public void setAboveOrBelow(AlertDirection aboveOrBelow) {
        this.aboveOrBelow = aboveOrBelow;
    }

    public Boolean getTriggered() {
        return triggered;
    }

    public void setTriggered(Boolean triggered) {
        this.triggered = triggered;
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
        return "Alert{" +
                "id=" + id +
                ", targetPrice=" + targetPrice +
                ", aboveOrBelow=" + aboveOrBelow +
                ", triggered=" + triggered +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
