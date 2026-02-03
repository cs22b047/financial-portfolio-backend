package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reference table for blockchain networks.
 * Examples: Ethereum, Bitcoin, Solana, Polygon
 */
@Entity
@Table(name = "blockchains")
public class Blockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // ETH, BTC, SOL

    @Column(nullable = false, length = 50)
    private String name; // Ethereum, Bitcoin, Solana

    @Enumerated(EnumType.STRING)
    @Column(name = "consensus_type", length = 20)
    private ConsensusType consensusType;

    @Column(name = "native_token", length = 20)
    private String nativeToken; // ETH, BTC, SOL

    @Column(name = "chain_id")
    private Long chainId; // For EVM chains

    @Column(name = "explorer_url", length = 200)
    private String explorerUrl;

    @Column(name = "supports_staking")
    private Boolean supportsStaking = false;

    @Column(name = "supports_smart_contracts")
    private Boolean supportsSmartContracts = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "blockchain", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<CryptoMetrics> cryptoMetrics;

    public Blockchain() {
        this.createdDate = LocalDateTime.now();
    }

    public Blockchain(String code, String name, ConsensusType consensusType) {
        this();
        this.code = code;
        this.name = name;
        this.consensusType = consensusType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ConsensusType getConsensusType() { return consensusType; }
    public void setConsensusType(ConsensusType consensusType) { this.consensusType = consensusType; }

    public String getNativeToken() { return nativeToken; }
    public void setNativeToken(String nativeToken) { this.nativeToken = nativeToken; }

    public Long getChainId() { return chainId; }
    public void setChainId(Long chainId) { this.chainId = chainId; }

    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }

    public Boolean getSupportsStaking() { return supportsStaking; }
    public void setSupportsStaking(Boolean supportsStaking) { this.supportsStaking = supportsStaking; }

    public Boolean getSupportsSmartContracts() { return supportsSmartContracts; }
    public void setSupportsSmartContracts(Boolean supportsSmartContracts) { this.supportsSmartContracts = supportsSmartContracts; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<CryptoMetrics> getCryptoMetrics() { return cryptoMetrics; }
    public void setCryptoMetrics(List<CryptoMetrics> cryptoMetrics) { this.cryptoMetrics = cryptoMetrics; }

    public String getExplorerTransactionUrl(String txHash) {
        if (explorerUrl == null || txHash == null) return null;
        return explorerUrl + "/tx/" + txHash;
    }

    @Override
    public String toString() {
        return "Blockchain{code='" + code + "', name='" + name + "'}";
    }
}
