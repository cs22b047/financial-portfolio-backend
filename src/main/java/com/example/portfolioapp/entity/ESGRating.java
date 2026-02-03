package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ESGRating entity for Environmental, Social, and Governance ratings
 */
@Entity
@Table(name = "esg_ratings",
       uniqueConstraints = @UniqueConstraint(name = "uk_esg_symbol", columnNames = "symbol"),
       indexes = {
           @Index(name = "idx_esg_total_score", columnList = "total_score"),
           @Index(name = "idx_esg_controversy", columnList = "controversy_level")
       })
public class ESGRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id", nullable = false)
    private MarketData marketData;

    @Column(name = "symbol", nullable = false, length = 20, unique = true)
    private String symbol;

    // Total ESG Score
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "total_grade", length = 20)
    private String totalGrade;

    // Environmental Pillar
    @Column(name = "environment_score", precision = 5, scale = 2)
    private BigDecimal environmentScore;

    @Column(name = "environment_grade", length = 20)
    private String environmentGrade;

    // Social Pillar
    @Column(name = "social_score", precision = 5, scale = 2)
    private BigDecimal socialScore;

    @Column(name = "social_grade", length = 20)
    private String socialGrade;

    // Governance Pillar
    @Column(name = "governance_score", precision = 5, scale = 2)
    private BigDecimal governanceScore;

    @Column(name = "governance_grade", length = 20)
    private String governanceGrade;

    // Risk and Controversy
    @Column(name = "controversy_level")
    private Integer controversyLevel;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    // Metadata
    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Constructors
    public ESGRating() {}

    public ESGRating(MarketData marketData, String symbol) {
        this.marketData = marketData;
        this.symbol = symbol;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getTotalGrade() {
        return totalGrade;
    }

    public void setTotalGrade(String totalGrade) {
        this.totalGrade = totalGrade;
    }

    public BigDecimal getEnvironmentScore() {
        return environmentScore;
    }

    public void setEnvironmentScore(BigDecimal environmentScore) {
        this.environmentScore = environmentScore;
    }

    public String getEnvironmentGrade() {
        return environmentGrade;
    }

    public void setEnvironmentGrade(String environmentGrade) {
        this.environmentGrade = environmentGrade;
    }

    public BigDecimal getSocialScore() {
        return socialScore;
    }

    public void setSocialScore(BigDecimal socialScore) {
        this.socialScore = socialScore;
    }

    public String getSocialGrade() {
        return socialGrade;
    }

    public void setSocialGrade(String socialGrade) {
        this.socialGrade = socialGrade;
    }

    public BigDecimal getGovernanceScore() {
        return governanceScore;
    }

    public void setGovernanceScore(BigDecimal governanceScore) {
        this.governanceScore = governanceScore;
    }

    public String getGovernanceGrade() {
        return governanceGrade;
    }

    public void setGovernanceGrade(String governanceGrade) {
        this.governanceGrade = governanceGrade;
    }

    public Integer getControversyLevel() {
        return controversyLevel;
    }

    public void setControversyLevel(Integer controversyLevel) {
        this.controversyLevel = controversyLevel;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "ESGRating{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", totalScore=" + totalScore +
                ", totalGrade='" + totalGrade + '\'' +
                ", controversyLevel=" + controversyLevel +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
