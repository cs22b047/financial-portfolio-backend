package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Single investor settings and preferences.
 * Since this is for one big investor, no multi-user support needed.
 */
@Entity
@Table(name = "investor_settings")
public class InvestorSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investor_name", length = 100)
    private String investorName;

    // Display preferences
    @Column(name = "default_currency", length = 10)
    private String defaultCurrency = "USD";

    @Column(length = 50)
    private String timezone = "America/New_York";

    @Column(name = "date_format", length = 20)
    private String dateFormat = "MM/dd/yyyy";

    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    @Column(length = 20)
    private String theme = "light"; // light, dark, system

    // Tax settings
    @Enumerated(EnumType.STRING)
    @Column(name = "default_cost_basis_method", length = 20)
    private CostBasisMethod defaultCostBasisMethod = CostBasisMethod.FIFO;

    @Column(name = "tax_rate_short_term", precision = 5, scale = 2)
    private java.math.BigDecimal taxRateShortTerm;

    @Column(name = "tax_rate_long_term", precision = 5, scale = 2)
    private java.math.BigDecimal taxRateLongTerm;

    @Column(name = "tax_rate_qualified_dividends", precision = 5, scale = 2)
    private java.math.BigDecimal taxRateQualifiedDividends;

    // Notification preferences
    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;

    @Column(name = "email_alerts_enabled")
    private Boolean emailAlertsEnabled = false;

    @Column(name = "alert_email", length = 200)
    private String alertEmail;

    @Column(name = "daily_summary_enabled")
    private Boolean dailySummaryEnabled = false;

    // Market data preferences
    @Column(name = "auto_refresh_enabled")
    private Boolean autoRefreshEnabled = true;

    @Column(name = "refresh_interval_minutes")
    private Integer refreshIntervalMinutes = 15;

    @Column(name = "after_hours_data_enabled")
    private Boolean afterHoursDataEnabled = true;

    // Portfolio display preferences
    @Column(name = "show_cents")
    private Boolean showCents = true;

    @Column(name = "show_percentages")
    private Boolean showPercentages = true;

    @Column(name = "hide_small_positions")
    private Boolean hideSmallPositions = false;

    @Column(name = "small_position_threshold", precision = 10, scale = 2)
    private java.math.BigDecimal smallPositionThreshold;

    @Column(name = "default_chart_period", length = 10)
    private String defaultChartPeriod = "1Y"; // 1D, 1W, 1M, 3M, 6M, 1Y, 5Y, MAX

    // Privacy
    @Column(name = "hide_balances_by_default")
    private Boolean hideBalancesByDefault = false;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public InvestorSettings() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvestorName() { return investorName; }
    public void setInvestorName(String investorName) { this.investorName = investorName; }

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

    public CostBasisMethod getDefaultCostBasisMethod() { return defaultCostBasisMethod; }
    public void setDefaultCostBasisMethod(CostBasisMethod defaultCostBasisMethod) { this.defaultCostBasisMethod = defaultCostBasisMethod; }

    public java.math.BigDecimal getTaxRateShortTerm() { return taxRateShortTerm; }
    public void setTaxRateShortTerm(java.math.BigDecimal taxRateShortTerm) { this.taxRateShortTerm = taxRateShortTerm; }

    public java.math.BigDecimal getTaxRateLongTerm() { return taxRateLongTerm; }
    public void setTaxRateLongTerm(java.math.BigDecimal taxRateLongTerm) { this.taxRateLongTerm = taxRateLongTerm; }

    public java.math.BigDecimal getTaxRateQualifiedDividends() { return taxRateQualifiedDividends; }
    public void setTaxRateQualifiedDividends(java.math.BigDecimal taxRateQualifiedDividends) { this.taxRateQualifiedDividends = taxRateQualifiedDividends; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public Boolean getEmailAlertsEnabled() { return emailAlertsEnabled; }
    public void setEmailAlertsEnabled(Boolean emailAlertsEnabled) { this.emailAlertsEnabled = emailAlertsEnabled; }

    public String getAlertEmail() { return alertEmail; }
    public void setAlertEmail(String alertEmail) { this.alertEmail = alertEmail; }

    public Boolean getDailySummaryEnabled() { return dailySummaryEnabled; }
    public void setDailySummaryEnabled(Boolean dailySummaryEnabled) { this.dailySummaryEnabled = dailySummaryEnabled; }

    public Boolean getAutoRefreshEnabled() { return autoRefreshEnabled; }
    public void setAutoRefreshEnabled(Boolean autoRefreshEnabled) { this.autoRefreshEnabled = autoRefreshEnabled; }

    public Integer getRefreshIntervalMinutes() { return refreshIntervalMinutes; }
    public void setRefreshIntervalMinutes(Integer refreshIntervalMinutes) { this.refreshIntervalMinutes = refreshIntervalMinutes; }

    public Boolean getAfterHoursDataEnabled() { return afterHoursDataEnabled; }
    public void setAfterHoursDataEnabled(Boolean afterHoursDataEnabled) { this.afterHoursDataEnabled = afterHoursDataEnabled; }

    public Boolean getShowCents() { return showCents; }
    public void setShowCents(Boolean showCents) { this.showCents = showCents; }

    public Boolean getShowPercentages() { return showPercentages; }
    public void setShowPercentages(Boolean showPercentages) { this.showPercentages = showPercentages; }

    public Boolean getHideSmallPositions() { return hideSmallPositions; }
    public void setHideSmallPositions(Boolean hideSmallPositions) { this.hideSmallPositions = hideSmallPositions; }

    public java.math.BigDecimal getSmallPositionThreshold() { return smallPositionThreshold; }
    public void setSmallPositionThreshold(java.math.BigDecimal smallPositionThreshold) { this.smallPositionThreshold = smallPositionThreshold; }

    public String getDefaultChartPeriod() { return defaultChartPeriod; }
    public void setDefaultChartPeriod(String defaultChartPeriod) { this.defaultChartPeriod = defaultChartPeriod; }

    public Boolean getHideBalancesByDefault() { return hideBalancesByDefault; }
    public void setHideBalancesByDefault(Boolean hideBalancesByDefault) { this.hideBalancesByDefault = hideBalancesByDefault; }

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
        return "InvestorSettings{" +
                "id=" + id +
                ", name='" + investorName + '\'' +
                ", currency=" + defaultCurrency +
                ", costBasisMethod=" + defaultCostBasisMethod +
                '}';
    }
}
