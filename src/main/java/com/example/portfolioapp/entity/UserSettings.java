package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", length = 100)
    private String userName;
    
    @Column(name = "default_currency", length = 3)
    private String defaultCurrency = "USD";
    
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";
    
    @Column(name = "date_format", length = 20)
    private String dateFormat = "yyyy-MM-dd";
    
    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;
    
    @Column(name = "theme", length = 20)
    private String theme = "light";
    
    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;
    
    @Column(name = "price_alerts_enabled")
    private Boolean priceAlertsEnabled = true;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Constructors
    public UserSettings() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public UserSettings(String userName) {
        this();
        this.userName = userName;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    
    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    
    public Boolean getPriceAlertsEnabled() { return priceAlertsEnabled; }
    public void setPriceAlertsEnabled(Boolean priceAlertsEnabled) { this.priceAlertsEnabled = priceAlertsEnabled; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserSettings{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", defaultCurrency='" + defaultCurrency + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}