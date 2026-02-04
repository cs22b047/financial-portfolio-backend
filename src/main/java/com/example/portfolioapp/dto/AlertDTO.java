package com.example.portfolioapp.dto;

import com.example.portfolioapp.entity.Alert;
import com.example.portfolioapp.entity.AlertDirection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Alert with asset information included
 */
public class AlertDTO {
    private Long id;
    private AssetInfo asset;
    private BigDecimal targetPrice;
    private AlertDirection aboveOrBelow;
    private Boolean triggered;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static class AssetInfo {
        private Long id;
        private String symbol;
        private String name;

        public AssetInfo(Long id, String symbol, String name) {
            this.id = id;
            this.symbol = symbol;
            this.name = name;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static AlertDTO fromEntity(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAsset(new AssetInfo(
            alert.getAsset().getId(),
            alert.getAsset().getSymbol(),
            alert.getAsset().getName()
        ));
        dto.setTargetPrice(alert.getTargetPrice());
        dto.setAboveOrBelow(alert.getAboveOrBelow());
        dto.setTriggered(alert.getTriggered());
        dto.setCreatedDate(alert.getCreatedDate());
        dto.setUpdatedDate(alert.getUpdatedDate());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssetInfo getAsset() {
        return asset;
    }

    public void setAsset(AssetInfo asset) {
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
}
