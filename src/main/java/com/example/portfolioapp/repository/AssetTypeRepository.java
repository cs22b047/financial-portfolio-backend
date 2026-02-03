package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AssetType entity
 */
@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {

    /**
     * Find asset type by code (e.g., "STOCK", "BOND", "CRYPTO")
     */
    Optional<AssetType> findByCode(String code);

    /**
     * Find asset type by name
     */
    Optional<AssetType> findByName(String name);

    /**
     * Find all active asset types
     */
    List<AssetType> findByIsActiveTrue();

    /**
     * Find asset types by risk level
     */
    @Query("SELECT at FROM AssetType at WHERE at.riskLevel = :riskLevel AND at.isActive = true")
    List<AssetType> findByRiskLevel(com.example.portfolioapp.entity.RiskLevel riskLevel);

    /**
     * Check if asset type code exists
     */
    boolean existsByCode(String code);
}
