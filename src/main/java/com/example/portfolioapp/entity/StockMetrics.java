package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stock-specific metrics. One-to-one relationship with Instrument.
 * Only created for instruments where instrumentType is STOCK or ETF.
 */
@Entity
@Table(name = "stock_metrics")
public class StockMetrics {

    @Id
    private Long id; // Same as instrument_id (shared primary key)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    // Valuation metrics
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "forward_pe", precision = 10, scale = 2)
    private BigDecimal forwardPe;

    @Column(name = "peg_ratio", precision = 8, scale = 3)
    private BigDecimal pegRatio;

    @Column(name = "price_to_book", precision = 10, scale = 2)
    private BigDecimal priceToBook;

    @Column(name = "price_to_sales", precision = 10, scale = 2)
    private BigDecimal priceToSales;

    @Column(name = "enterprise_value")
    private Long enterpriseValue;

    @Column(name = "ev_to_ebitda", precision = 10, scale = 2)
    private BigDecimal evToEbitda;

    // Earnings & profitability
    @Column(name = "earnings_per_share", precision = 10, scale = 4)
    private BigDecimal earningsPerShare;

    @Column(name = "revenue")
    private Long revenue;

    @Column(name = "gross_margin", precision = 6, scale = 2)
    private BigDecimal grossMargin;

    @Column(name = "operating_margin", precision = 6, scale = 2)
    private BigDecimal operatingMargin;

    @Column(name = "profit_margin", precision = 6, scale = 2)
    private BigDecimal profitMargin;

    @Column(name = "return_on_equity", precision = 6, scale = 2)
    private BigDecimal returnOnEquity;

    @Column(name = "return_on_assets", precision = 6, scale = 2)
    private BigDecimal returnOnAssets;

    // Dividend metrics
    @Column(name = "dividend_yield", precision = 6, scale = 3)
    private BigDecimal dividendYield;

    @Column(name = "dividend_per_share", precision = 10, scale = 4)
    private BigDecimal dividendPerShare;

    @Column(name = "payout_ratio", precision = 6, scale = 2)
    private BigDecimal payoutRatio;

    @Column(name = "ex_dividend_date")
    private java.time.LocalDate exDividendDate;

    // Risk metrics
    @Column(precision = 8, scale = 4)
    private BigDecimal beta;

    @Column(name = "volatility_52w", precision = 6, scale = 2)
    private BigDecimal volatility52w;

    // Shares info
    @Column(name = "shares_outstanding")
    private Long sharesOutstanding;

    @Column(name = "float_shares")
    private Long floatShares;

    @Column(name = "short_ratio", precision = 8, scale = 2)
    private BigDecimal shortRatio;

    @Column(name = "short_percent_of_float", precision = 6, scale = 2)
    private BigDecimal shortPercentOfFloat;

    // Analyst data
    @Column(name = "analyst_target_price", precision = 10, scale = 2)
    private BigDecimal analystTargetPrice;

    @Column(name = "analyst_rating", length = 20)
    private String analystRating; // BUY, HOLD, SELL

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public StockMetrics() {
        this.updatedDate = LocalDateTime.now();
    }

    public StockMetrics(Instrument instrument) {
        this();
        this.instrument = instrument;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }

    public BigDecimal getForwardPe() { return forwardPe; }
    public void setForwardPe(BigDecimal forwardPe) { this.forwardPe = forwardPe; }

    public BigDecimal getPegRatio() { return pegRatio; }
    public void setPegRatio(BigDecimal pegRatio) { this.pegRatio = pegRatio; }

    public BigDecimal getPriceToBook() { return priceToBook; }
    public void setPriceToBook(BigDecimal priceToBook) { this.priceToBook = priceToBook; }

    public BigDecimal getPriceToSales() { return priceToSales; }
    public void setPriceToSales(BigDecimal priceToSales) { this.priceToSales = priceToSales; }

    public Long getEnterpriseValue() { return enterpriseValue; }
    public void setEnterpriseValue(Long enterpriseValue) { this.enterpriseValue = enterpriseValue; }

    public BigDecimal getEvToEbitda() { return evToEbitda; }
    public void setEvToEbitda(BigDecimal evToEbitda) { this.evToEbitda = evToEbitda; }

    public BigDecimal getEarningsPerShare() { return earningsPerShare; }
    public void setEarningsPerShare(BigDecimal earningsPerShare) { this.earningsPerShare = earningsPerShare; }

    public Long getRevenue() { return revenue; }
    public void setRevenue(Long revenue) { this.revenue = revenue; }

    public BigDecimal getGrossMargin() { return grossMargin; }
    public void setGrossMargin(BigDecimal grossMargin) { this.grossMargin = grossMargin; }

    public BigDecimal getOperatingMargin() { return operatingMargin; }
    public void setOperatingMargin(BigDecimal operatingMargin) { this.operatingMargin = operatingMargin; }

    public BigDecimal getProfitMargin() { return profitMargin; }
    public void setProfitMargin(BigDecimal profitMargin) { this.profitMargin = profitMargin; }

    public BigDecimal getReturnOnEquity() { return returnOnEquity; }
    public void setReturnOnEquity(BigDecimal returnOnEquity) { this.returnOnEquity = returnOnEquity; }

    public BigDecimal getReturnOnAssets() { return returnOnAssets; }
    public void setReturnOnAssets(BigDecimal returnOnAssets) { this.returnOnAssets = returnOnAssets; }

    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }

    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public BigDecimal getPayoutRatio() { return payoutRatio; }
    public void setPayoutRatio(BigDecimal payoutRatio) { this.payoutRatio = payoutRatio; }

    public java.time.LocalDate getExDividendDate() { return exDividendDate; }
    public void setExDividendDate(java.time.LocalDate exDividendDate) { this.exDividendDate = exDividendDate; }

    public BigDecimal getBeta() { return beta; }
    public void setBeta(BigDecimal beta) { this.beta = beta; }

    public BigDecimal getVolatility52w() { return volatility52w; }
    public void setVolatility52w(BigDecimal volatility52w) { this.volatility52w = volatility52w; }

    public Long getSharesOutstanding() { return sharesOutstanding; }
    public void setSharesOutstanding(Long sharesOutstanding) { this.sharesOutstanding = sharesOutstanding; }

    public Long getFloatShares() { return floatShares; }
    public void setFloatShares(Long floatShares) { this.floatShares = floatShares; }

    public BigDecimal getShortRatio() { return shortRatio; }
    public void setShortRatio(BigDecimal shortRatio) { this.shortRatio = shortRatio; }

    public BigDecimal getShortPercentOfFloat() { return shortPercentOfFloat; }
    public void setShortPercentOfFloat(BigDecimal shortPercentOfFloat) { this.shortPercentOfFloat = shortPercentOfFloat; }

    public BigDecimal getAnalystTargetPrice() { return analystTargetPrice; }
    public void setAnalystTargetPrice(BigDecimal analystTargetPrice) { this.analystTargetPrice = analystTargetPrice; }

    public String getAnalystRating() { return analystRating; }
    public void setAnalystRating(String analystRating) { this.analystRating = analystRating; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Helper methods
    public boolean isDividendPayer() {
        return dividendYield != null && dividendYield.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isUndervalued(BigDecimal benchmarkPe) {
        return peRatio != null && benchmarkPe != null && peRatio.compareTo(benchmarkPe) < 0;
    }

    @Override
    public String toString() {
        return "StockMetrics{instrumentId=" + id + ", peRatio=" + peRatio + ", dividendYield=" + dividendYield + "}";
    }
}
