package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UserSettings entity for storing user preferences and settings.
 * Single-row design - only one user settings record exists.
 * ID is always 1 (enforced by unique constraint and initialization logic).
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", length = 100, unique = true)
    private String userName;

    @Column(name = "default_currency", length = 3)
    private String defaultCurrency = "USD";

    @Column(name = "time_zone", length = 50)
    private String timeZone = "UTC";

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "theme", length = 20)
    private String theme = "light";

    @Column(name = "wallet", precision = 15, scale = 2)
    private BigDecimal wallet = BigDecimal.ZERO;

    @Column(name = "target", precision = 15, scale = 2)
    private BigDecimal target = new BigDecimal("150000");

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
    public UserSettings() {}

    public UserSettings(String userName) {
        this.userName = userName;
    }

    public UserSettings(String userName, String defaultCurrency, String timeZone, 
                       String currency, String theme, BigDecimal wallet) {
        this.userName = userName;
        this.defaultCurrency = defaultCurrency;
        this.timeZone = timeZone;
        this.currency = currency;
        this.theme = theme;
        this.wallet = wallet;
    }

    public UserSettings(String userName, String defaultCurrency, String timeZone, 
                       String currency, String theme, BigDecimal wallet, BigDecimal target) {
        this.userName = userName;
        this.defaultCurrency = defaultCurrency;
        this.timeZone = timeZone;
        this.currency = currency;
        this.theme = theme;
        this.wallet = wallet;
        this.target = target;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public BigDecimal getWallet() {
        return wallet;
    }

    public void setWallet(BigDecimal wallet) {
        this.wallet = wallet;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public void setTarget(BigDecimal target) {
        this.target = target;
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
        return "UserSettings{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", defaultCurrency='" + defaultCurrency + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", currency='" + currency + '\'' +
                ", theme='" + theme + '\'' +
                ", wallet=" + wallet +
                ", target=" + target +
                '}';
    }
}
