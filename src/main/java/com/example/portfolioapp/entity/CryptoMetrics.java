package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cryptocurrency-specific metrics. One-to-one relationship with Instrument.
 * Only created for instruments where instrumentType is CRYPTO.
 */
@Entity
@Table(name = "crypto_metrics")
public class CryptoMetrics {

    @Id
    private Long id; // Same as instrument_id (shared primary key)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    // Blockchain info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blockchain_id")
    private Blockchain blockchain;

    @Column(name = "contract_address", length = 100)
    private String contractAddress; // For tokens

    @Column(name = "token_standard", length = 20)
    private String tokenStandard; // ERC-20, BEP-20, etc.

    // Supply metrics
    @Column(name = "max_supply", precision = 30, scale = 0)
    private BigDecimal maxSupply;

    @Column(name = "total_supply", precision = 30, scale = 0)
    private BigDecimal totalSupply;

    @Column(name = "circulating_supply", precision = 30, scale = 0)
    private BigDecimal circulatingSupply;

    // Market metrics
    @Column(name = "market_cap_rank")
    private Integer marketCapRank;

    @Column(name = "fully_diluted_valuation")
    private Long fullyDilutedValuation;

    @Column(name = "volume_24h", precision = 20, scale = 2)
    private BigDecimal volume24h;

    @Column(name = "volume_change_24h", precision = 8, scale = 2)
    private BigDecimal volumeChange24h;

    // Price changes
    @Column(name = "change_1h", precision = 10, scale = 4)
    private BigDecimal change1h;

    @Column(name = "change_24h", precision = 10, scale = 4)
    private BigDecimal change24h;

    @Column(name = "change_7d", precision = 10, scale = 4)
    private BigDecimal change7d;

    @Column(name = "change_30d", precision = 10, scale = 4)
    private BigDecimal change30d;

    @Column(name = "change_1y", precision = 10, scale = 4)
    private BigDecimal change1y;

    @Column(name = "ath", precision = 18, scale = 8)
    private BigDecimal allTimeHigh;

    @Column(name = "ath_date")
    private LocalDateTime athDate;

    @Column(name = "ath_change_percent", precision = 10, scale = 2)
    private BigDecimal athChangePercent;

    @Column(name = "atl", precision = 18, scale = 8)
    private BigDecimal allTimeLow;

    @Column(name = "atl_date")
    private LocalDateTime atlDate;

    // Staking info
    @Column(name = "staking_apy", precision = 8, scale = 4)
    private BigDecimal stakingApy;

    @Column(name = "is_stakeable")
    private Boolean isStakeable = false;

    @Column(name = "min_stake_amount", precision = 20, scale = 8)
    private BigDecimal minStakeAmount;

    @Column(name = "lock_period_days")
    private Integer lockPeriodDays;

    // DeFi metrics (if applicable)
    @Column(name = "total_value_locked", precision = 20, scale = 2)
    private BigDecimal totalValueLocked;

    // Social/community
    @Column(name = "twitter_followers")
    private Long twitterFollowers;

    @Column(name = "reddit_subscribers")
    private Long redditSubscribers;

    @Column(name = "github_stars")
    private Integer githubStars;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public CryptoMetrics() {
        this.updatedDate = LocalDateTime.now();
    }

    public CryptoMetrics(Instrument instrument) {
        this();
        this.instrument = instrument;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public Blockchain getBlockchain() { return blockchain; }
    public void setBlockchain(Blockchain blockchain) { this.blockchain = blockchain; }

    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }

    public String getTokenStandard() { return tokenStandard; }
    public void setTokenStandard(String tokenStandard) { this.tokenStandard = tokenStandard; }

    public BigDecimal getMaxSupply() { return maxSupply; }
    public void setMaxSupply(BigDecimal maxSupply) { this.maxSupply = maxSupply; }

    public BigDecimal getTotalSupply() { return totalSupply; }
    public void setTotalSupply(BigDecimal totalSupply) { this.totalSupply = totalSupply; }

    public BigDecimal getCirculatingSupply() { return circulatingSupply; }
    public void setCirculatingSupply(BigDecimal circulatingSupply) { this.circulatingSupply = circulatingSupply; }

    public Integer getMarketCapRank() { return marketCapRank; }
    public void setMarketCapRank(Integer marketCapRank) { this.marketCapRank = marketCapRank; }

    public Long getFullyDilutedValuation() { return fullyDilutedValuation; }
    public void setFullyDilutedValuation(Long fullyDilutedValuation) { this.fullyDilutedValuation = fullyDilutedValuation; }

    public BigDecimal getVolume24h() { return volume24h; }
    public void setVolume24h(BigDecimal volume24h) { this.volume24h = volume24h; }

    public BigDecimal getVolumeChange24h() { return volumeChange24h; }
    public void setVolumeChange24h(BigDecimal volumeChange24h) { this.volumeChange24h = volumeChange24h; }

    public BigDecimal getChange1h() { return change1h; }
    public void setChange1h(BigDecimal change1h) { this.change1h = change1h; }

    public BigDecimal getChange24h() { return change24h; }
    public void setChange24h(BigDecimal change24h) { this.change24h = change24h; }

    public BigDecimal getChange7d() { return change7d; }
    public void setChange7d(BigDecimal change7d) { this.change7d = change7d; }

    public BigDecimal getChange30d() { return change30d; }
    public void setChange30d(BigDecimal change30d) { this.change30d = change30d; }

    public BigDecimal getChange1y() { return change1y; }
    public void setChange1y(BigDecimal change1y) { this.change1y = change1y; }

    public BigDecimal getAllTimeHigh() { return allTimeHigh; }
    public void setAllTimeHigh(BigDecimal allTimeHigh) { this.allTimeHigh = allTimeHigh; }

    public LocalDateTime getAthDate() { return athDate; }
    public void setAthDate(LocalDateTime athDate) { this.athDate = athDate; }

    public BigDecimal getAthChangePercent() { return athChangePercent; }
    public void setAthChangePercent(BigDecimal athChangePercent) { this.athChangePercent = athChangePercent; }

    public BigDecimal getAllTimeLow() { return allTimeLow; }
    public void setAllTimeLow(BigDecimal allTimeLow) { this.allTimeLow = allTimeLow; }

    public LocalDateTime getAtlDate() { return atlDate; }
    public void setAtlDate(LocalDateTime atlDate) { this.atlDate = atlDate; }

    public BigDecimal getStakingApy() { return stakingApy; }
    public void setStakingApy(BigDecimal stakingApy) { this.stakingApy = stakingApy; }

    public Boolean getIsStakeable() { return isStakeable; }
    public void setIsStakeable(Boolean isStakeable) { this.isStakeable = isStakeable; }

    public BigDecimal getMinStakeAmount() { return minStakeAmount; }
    public void setMinStakeAmount(BigDecimal minStakeAmount) { this.minStakeAmount = minStakeAmount; }

    public Integer getLockPeriodDays() { return lockPeriodDays; }
    public void setLockPeriodDays(Integer lockPeriodDays) { this.lockPeriodDays = lockPeriodDays; }

    public BigDecimal getTotalValueLocked() { return totalValueLocked; }
    public void setTotalValueLocked(BigDecimal totalValueLocked) { this.totalValueLocked = totalValueLocked; }

    public Long getTwitterFollowers() { return twitterFollowers; }
    public void setTwitterFollowers(Long twitterFollowers) { this.twitterFollowers = twitterFollowers; }

    public Long getRedditSubscribers() { return redditSubscribers; }
    public void setRedditSubscribers(Long redditSubscribers) { this.redditSubscribers = redditSubscribers; }

    public Integer getGithubStars() { return githubStars; }
    public void setGithubStars(Integer githubStars) { this.githubStars = githubStars; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Helper methods
    public BigDecimal getCirculatingSupplyPercent() {
        if (maxSupply == null || maxSupply.compareTo(BigDecimal.ZERO) == 0) return null;
        if (circulatingSupply == null) return null;
        return circulatingSupply.divide(maxSupply, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getDistanceFromAth() {
        if (allTimeHigh == null || instrument == null || instrument.getCurrentPrice() == null) return null;
        return allTimeHigh.subtract(instrument.getCurrentPrice())
                          .divide(allTimeHigh, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));
    }

    public boolean isToken() {
        return contractAddress != null && !contractAddress.isEmpty();
    }

    @Override
    public String toString() {
        return "CryptoMetrics{instrumentId=" + id + ", marketCapRank=" + marketCapRank + ", stakingApy=" + stakingApy + "}";
    }
}
