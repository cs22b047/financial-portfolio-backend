package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Summary statistics for stock historical price data.
 * Stores aggregated metrics like total return, volatility, Sharpe ratio, VaR, etc.
 *
 * Relationships:
 * - Many StockSummary -> One MarketData
 */
@Entity
@Table(name = "stock_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"market_data_id", "period", "start_date", "end_date"}),
       indexes = {
           @Index(name = "idx_stock_summary_market_data", columnList = "market_data_id"),
           @Index(name = "idx_stock_summary_period", columnList = "period"),
           @Index(name = "idx_stock_summary_dates", columnList = "start_date, end_date")
       })
public class StockSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id", nullable = false)
    private MarketData marketData;

    @Column(name = "period", nullable = false, length = 10)
    private String period; // e.g., "1y", "5y", "10y"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "trading_days")
    private Integer tradingDays;

    // Price Metrics
    @Column(name = "start_price", precision = 18, scale = 8)
    private BigDecimal startPrice;

    @Column(name = "end_price", precision = 18, scale = 8)
    private BigDecimal endPrice;

    @Column(name = "min_price", precision = 18, scale = 8)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 18, scale = 8)
    private BigDecimal maxPrice;

    @Column(name = "avg_price", precision = 18, scale = 8)
    private BigDecimal avgPrice;

    // Return Metrics
    @Column(name = "total_return", precision = 10, scale = 4)
    private BigDecimal totalReturn; // Percentage

    @Column(name = "avg_daily_return", precision = 10, scale = 6)
    private BigDecimal avgDailyReturn; // Percentage

    @Column(name = "annualized_return", precision = 10, scale = 4)
    private BigDecimal annualizedReturn; // Percentage

    // Risk Metrics
    @Column(name = "daily_volatility", precision = 10, scale = 6)
    private BigDecimal dailyVolatility; // Percentage

    @Column(name = "annualized_volatility", precision = 10, scale = 4)
    private BigDecimal annualizedVolatility; // Percentage

    @Column(name = "sharpe_ratio", precision = 10, scale = 4)
    private BigDecimal sharpeRatio;

    // Extreme Values
    @Column(name = "max_daily_gain", precision = 10, scale = 4)
    private BigDecimal maxDailyGain; // Percentage

    @Column(name = "max_daily_loss", precision = 10, scale = 4)
    private BigDecimal maxDailyLoss; // Percentage

    // Volume Metrics
    @Column(name = "avg_volume")
    private Long avgVolume;

    @Column(name = "max_volume")
    private Long maxVolume;

    @Column(name = "min_volume")
    private Long minVolume;

    // Value at Risk
    @Column(name = "var_95", precision = 10, scale = 4)
    private BigDecimal var95; // 95% VaR, Percentage

    @Column(name = "var_99", precision = 10, scale = 4)
    private BigDecimal var99; // 99% VaR, Percentage

    // Drawdown
    @Column(name = "max_drawdown", precision = 10, scale = 4)
    private BigDecimal maxDrawdown; // Percentage

    @Column(name = "created_date")
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
    public StockSummary() {}

    public StockSummary(MarketData marketData, String period, LocalDate startDate, LocalDate endDate) {
        this.marketData = marketData;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarketData getMarketData() {
        return marketData;
    }

    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTradingDays() {
        return tradingDays;
    }

    public void setTradingDays(Integer tradingDays) {
        this.tradingDays = tradingDays;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public BigDecimal getEndPrice() {
        return endPrice;
    }

    public void setEndPrice(BigDecimal endPrice) {
        this.endPrice = endPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(BigDecimal totalReturn) {
        this.totalReturn = totalReturn;
    }

    public BigDecimal getAvgDailyReturn() {
        return avgDailyReturn;
    }

    public void setAvgDailyReturn(BigDecimal avgDailyReturn) {
        this.avgDailyReturn = avgDailyReturn;
    }

    public BigDecimal getAnnualizedReturn() {
        return annualizedReturn;
    }

    public void setAnnualizedReturn(BigDecimal annualizedReturn) {
        this.annualizedReturn = annualizedReturn;
    }

    public BigDecimal getDailyVolatility() {
        return dailyVolatility;
    }

    public void setDailyVolatility(BigDecimal dailyVolatility) {
        this.dailyVolatility = dailyVolatility;
    }

    public BigDecimal getAnnualizedVolatility() {
        return annualizedVolatility;
    }

    public void setAnnualizedVolatility(BigDecimal annualizedVolatility) {
        this.annualizedVolatility = annualizedVolatility;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(BigDecimal sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public BigDecimal getMaxDailyGain() {
        return maxDailyGain;
    }

    public void setMaxDailyGain(BigDecimal maxDailyGain) {
        this.maxDailyGain = maxDailyGain;
    }

    public BigDecimal getMaxDailyLoss() {
        return maxDailyLoss;
    }

    public void setMaxDailyLoss(BigDecimal maxDailyLoss) {
        this.maxDailyLoss = maxDailyLoss;
    }

    public Long getAvgVolume() {
        return avgVolume;
    }

    public void setAvgVolume(Long avgVolume) {
        this.avgVolume = avgVolume;
    }

    public Long getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(Long maxVolume) {
        this.maxVolume = maxVolume;
    }

    public Long getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(Long minVolume) {
        this.minVolume = minVolume;
    }

    public BigDecimal getVar95() {
        return var95;
    }

    public void setVar95(BigDecimal var95) {
        this.var95 = var95;
    }

    public BigDecimal getVar99() {
        return var99;
    }

    public void setVar99(BigDecimal var99) {
        this.var99 = var99;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
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
        return "StockSummary{" +
                "id=" + id +
                ", period='" + period + '\'' +
                ", totalReturn=" + totalReturn +
                ", sharpeRatio=" + sharpeRatio +
                '}';
    }
}
