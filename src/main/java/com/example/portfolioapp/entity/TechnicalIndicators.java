package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Technical indicators calculated from historical price data.
 * Stores various technical analysis metrics like moving averages, RSI, MACD, Bollinger Bands, etc.
 *
 * Relationships:
 * - Many TechnicalIndicators -> One PriceHistory (one-to-one relationship)
 */
@Entity
@Table(name = "technical_indicators",
       indexes = {
           @Index(name = "idx_technical_indicators_price_history", columnList = "price_history_id"),
           @Index(name = "idx_technical_indicators_date", columnList = "indicator_date")
       })
public class TechnicalIndicators {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_history_id", nullable = false, unique = true)
    private PriceHistory priceHistory;

    @Column(name = "indicator_date", nullable = false)
    private LocalDate indicatorDate;

    // Return Metrics
    @Column(name = "daily_return", precision = 18, scale = 8)
    private BigDecimal dailyReturn;

    @Column(name = "cumulative_return", precision = 18, scale = 8)
    private BigDecimal cumulativeReturn;

    @Column(name = "log_return", precision = 18, scale = 8)
    private BigDecimal logReturn;

    // Moving Averages
    @Column(name = "sma_20", precision = 18, scale = 8)
    private BigDecimal sma20;

    @Column(name = "sma_50", precision = 18, scale = 8)
    private BigDecimal sma50;

    @Column(name = "sma_200", precision = 18, scale = 8)
    private BigDecimal sma200;

    @Column(name = "ema_12", precision = 18, scale = 8)
    private BigDecimal ema12;

    @Column(name = "ema_26", precision = 18, scale = 8)
    private BigDecimal ema26;

    // MACD (Moving Average Convergence Divergence)
    @Column(name = "macd", precision = 18, scale = 8)
    private BigDecimal macd;

    @Column(name = "macd_signal", precision = 18, scale = 8)
    private BigDecimal macdSignal;

    @Column(name = "macd_histogram", precision = 18, scale = 8)
    private BigDecimal macdHistogram;

    // Bollinger Bands
    @Column(name = "bb_middle", precision = 18, scale = 8)
    private BigDecimal bbMiddle;

    @Column(name = "bb_upper", precision = 18, scale = 8)
    private BigDecimal bbUpper;

    @Column(name = "bb_lower", precision = 18, scale = 8)
    private BigDecimal bbLower;

    @Column(name = "bb_width", precision = 18, scale = 8)
    private BigDecimal bbWidth;

    // RSI (Relative Strength Index)
    @Column(name = "rsi", precision = 8, scale = 4)
    private BigDecimal rsi;

    // ATR (Average True Range) - Volatility indicator
    @Column(name = "atr", precision = 18, scale = 8)
    private BigDecimal atr;

    // Volume Indicators
    @Column(name = "volume_sma_20")
    private Long volumeSma20;

    @Column(name = "volume_ratio", precision = 10, scale = 4)
    private BigDecimal volumeRatio;

    // Momentum Indicators
    @Column(name = "momentum_10", precision = 18, scale = 8)
    private BigDecimal momentum10;

    @Column(name = "roc_10", precision = 10, scale = 4)
    private BigDecimal roc10;

    // Stochastic Oscillator
    @Column(name = "stochastic_k", precision = 8, scale = 4)
    private BigDecimal stochasticK;

    @Column(name = "stochastic_d", precision = 8, scale = 4)
    private BigDecimal stochasticD;

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
    public TechnicalIndicators() {}

    public TechnicalIndicators(PriceHistory priceHistory, LocalDate indicatorDate) {
        this.priceHistory = priceHistory;
        this.indicatorDate = indicatorDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PriceHistory getPriceHistory() {
        return priceHistory;
    }

    public void setPriceHistory(PriceHistory priceHistory) {
        this.priceHistory = priceHistory;
    }

    public LocalDate getIndicatorDate() {
        return indicatorDate;
    }

    public void setIndicatorDate(LocalDate indicatorDate) {
        this.indicatorDate = indicatorDate;
    }

    public BigDecimal getDailyReturn() {
        return dailyReturn;
    }

    public void setDailyReturn(BigDecimal dailyReturn) {
        this.dailyReturn = dailyReturn;
    }

    public BigDecimal getCumulativeReturn() {
        return cumulativeReturn;
    }

    public void setCumulativeReturn(BigDecimal cumulativeReturn) {
        this.cumulativeReturn = cumulativeReturn;
    }

    public BigDecimal getLogReturn() {
        return logReturn;
    }

    public void setLogReturn(BigDecimal logReturn) {
        this.logReturn = logReturn;
    }

    public BigDecimal getSma20() {
        return sma20;
    }

    public void setSma20(BigDecimal sma20) {
        this.sma20 = sma20;
    }

    public BigDecimal getSma50() {
        return sma50;
    }

    public void setSma50(BigDecimal sma50) {
        this.sma50 = sma50;
    }

    public BigDecimal getSma200() {
        return sma200;
    }

    public void setSma200(BigDecimal sma200) {
        this.sma200 = sma200;
    }

    public BigDecimal getEma12() {
        return ema12;
    }

    public void setEma12(BigDecimal ema12) {
        this.ema12 = ema12;
    }

    public BigDecimal getEma26() {
        return ema26;
    }

    public void setEma26(BigDecimal ema26) {
        this.ema26 = ema26;
    }

    public BigDecimal getMacd() {
        return macd;
    }

    public void setMacd(BigDecimal macd) {
        this.macd = macd;
    }

    public BigDecimal getMacdSignal() {
        return macdSignal;
    }

    public void setMacdSignal(BigDecimal macdSignal) {
        this.macdSignal = macdSignal;
    }

    public BigDecimal getMacdHistogram() {
        return macdHistogram;
    }

    public void setMacdHistogram(BigDecimal macdHistogram) {
        this.macdHistogram = macdHistogram;
    }

    public BigDecimal getBbMiddle() {
        return bbMiddle;
    }

    public void setBbMiddle(BigDecimal bbMiddle) {
        this.bbMiddle = bbMiddle;
    }

    public BigDecimal getBbUpper() {
        return bbUpper;
    }

    public void setBbUpper(BigDecimal bbUpper) {
        this.bbUpper = bbUpper;
    }

    public BigDecimal getBbLower() {
        return bbLower;
    }

    public void setBbLower(BigDecimal bbLower) {
        this.bbLower = bbLower;
    }

    public BigDecimal getBbWidth() {
        return bbWidth;
    }

    public void setBbWidth(BigDecimal bbWidth) {
        this.bbWidth = bbWidth;
    }

    public BigDecimal getRsi() {
        return rsi;
    }

    public void setRsi(BigDecimal rsi) {
        this.rsi = rsi;
    }

    public BigDecimal getAtr() {
        return atr;
    }

    public void setAtr(BigDecimal atr) {
        this.atr = atr;
    }

    public Long getVolumeSma20() {
        return volumeSma20;
    }

    public void setVolumeSma20(Long volumeSma20) {
        this.volumeSma20 = volumeSma20;
    }

    public BigDecimal getVolumeRatio() {
        return volumeRatio;
    }

    public void setVolumeRatio(BigDecimal volumeRatio) {
        this.volumeRatio = volumeRatio;
    }

    public BigDecimal getMomentum10() {
        return momentum10;
    }

    public void setMomentum10(BigDecimal momentum10) {
        this.momentum10 = momentum10;
    }

    public BigDecimal getRoc10() {
        return roc10;
    }

    public void setRoc10(BigDecimal roc10) {
        this.roc10 = roc10;
    }

    public BigDecimal getStochasticK() {
        return stochasticK;
    }

    public void setStochasticK(BigDecimal stochasticK) {
        this.stochasticK = stochasticK;
    }

    public BigDecimal getStochasticD() {
        return stochasticD;
    }

    public void setStochasticD(BigDecimal stochasticD) {
        this.stochasticD = stochasticD;
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
        return "TechnicalIndicators{" +
                "id=" + id +
                ", indicatorDate=" + indicatorDate +
                ", rsi=" + rsi +
                ", macd=" + macd +
                '}';
    }
}
