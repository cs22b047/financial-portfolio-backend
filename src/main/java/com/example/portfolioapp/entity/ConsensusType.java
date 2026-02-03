package com.example.portfolioapp.entity;

/**
 * Blockchain consensus mechanisms.
 */
public enum ConsensusType {
    PROOF_OF_WORK("Proof of Work", "Mining-based consensus (Bitcoin)"),
    PROOF_OF_STAKE("Proof of Stake", "Staking-based consensus (Ethereum 2.0)"),
    DELEGATED_POS("Delegated Proof of Stake", "Delegated staking (EOS, Tron)"),
    PROOF_OF_AUTHORITY("Proof of Authority", "Authority-based consensus"),
    PROOF_OF_HISTORY("Proof of History", "Time-based consensus (Solana)"),
    BYZANTINE_FAULT_TOLERANT("BFT", "Byzantine Fault Tolerant variants");

    private final String displayName;
    private final String description;

    ConsensusType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean supportsStaking() {
        return this == PROOF_OF_STAKE || this == DELEGATED_POS;
    }
}
