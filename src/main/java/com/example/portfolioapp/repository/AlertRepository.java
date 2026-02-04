package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Alert;
import com.example.portfolioapp.entity.AlertDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Alert entity
 * Manages price alerts for assets
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find all alerts for a specific asset
     */
    List<Alert> findByAssetId(Long assetId);

    /**
     * Find all non-triggered alerts
     */
    List<Alert> findByTriggeredFalse();

    /**
     * Find all triggered alerts
     */
    List<Alert> findByTriggeredTrue();

    /**
     * Find non-triggered alerts for a specific asset
     */
    List<Alert> findByAssetIdAndTriggeredFalse(Long assetId);

    /**
     * Find triggered alerts for a specific asset
     */
    List<Alert> findByAssetIdAndTriggeredTrue(Long assetId);

    /**
     * Find all active alerts that need to be checked against current prices
     * Joins with Asset to get current_price for comparison
     */
    @Query("SELECT al FROM Alert al " +
           "JOIN FETCH al.asset a " +
           "WHERE al.triggered = false " +
           "AND a.currentPrice IS NOT NULL")
    List<Alert> findActiveAlertsWithAssetPrices();

    /**
     * Find alerts that should trigger based on price conditions
     * ABOVE alerts: current_price >= target_price
     * BELOW alerts: current_price <= target_price
     */
    @Query("SELECT al FROM Alert al " +
           "JOIN al.asset a " +
           "WHERE al.triggered = false " +
           "AND ((al.aboveOrBelow = 'ABOVE' AND a.currentPrice >= al.targetPrice) " +
           "OR (al.aboveOrBelow = 'BELOW' AND a.currentPrice <= al.targetPrice))")
    List<Alert> findAlertsThatShouldTrigger();

    /**
     * Count non-triggered alerts for an asset
     */
    @Query("SELECT COUNT(al) FROM Alert al WHERE al.asset.id = :assetId AND al.triggered = false")
    long countActiveAlertsByAssetId(@Param("assetId") Long assetId);

    /**
     * Find alerts by direction
     */
    @Query("SELECT al FROM Alert al WHERE al.aboveOrBelow = :direction")
    List<Alert> findByAboveOrBelow(@Param("direction") AlertDirection direction);

    /**
     * Delete all alerts for an asset
     */
    void deleteByAssetId(Long assetId);
}
