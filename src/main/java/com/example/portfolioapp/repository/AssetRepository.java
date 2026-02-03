package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Asset entity (user's watchlist and owned stocks)
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Find asset by symbol
     */
    Optional<Asset> findBySymbol(String symbol);

    /**
     * Find all assets by status
     */
    List<Asset> findByStatus(AssetStatus status);

    /**
     * Find all owned assets
     */
    @Query("SELECT a FROM Asset a WHERE a.status = 'OWNED' ORDER BY a.currentPrice * a.quantity DESC")
    List<Asset> findAllOwned();

    /**
     * Find all watchlist assets
     */
    @Query("SELECT a FROM Asset a WHERE a.status = 'WATCHLIST' ORDER BY a.addedToWatchlistDate DESC")
    List<Asset> findAllWatchlist();

    /**
     * Find asset by market data ID
     */
    Optional<Asset> findByMarketDataId(Long marketDataId);

    /**
     * Find asset by market data ID and status
     */
    Optional<Asset> findByMarketDataIdAndStatus(Long marketDataId, AssetStatus status);

    /**
     * Find assets by sector
     */
    List<Asset> findBySectorAndStatus(String sector, AssetStatus status);

    /**
     * Find assets with price alerts enabled
     */
    @Query("SELECT a FROM Asset a WHERE a.priceAlertsEnabled = true AND a.status = 'OWNED'")
    List<Asset> findAssetsWithPriceAlerts();

    /**
     * Calculate total portfolio value (sum of all owned assets)
     */
    @Query("SELECT SUM(a.quantity * a.currentPrice) FROM Asset a WHERE a.status = 'OWNED'")
    BigDecimal calculateTotalPortfolioValue();

    /**
     * Calculate total investment cost
     */
    @Query("SELECT SUM(a.quantity * a.purchasePrice) FROM Asset a WHERE a.status = 'OWNED'")
    BigDecimal calculateTotalInvestmentCost();

    /**
     * Find top performing assets by gain percentage
     */
    @Query("SELECT a FROM Asset a WHERE a.status = 'OWNED' AND a.quantity IS NOT NULL " +
           "AND a.purchasePrice IS NOT NULL AND a.currentPrice IS NOT NULL " +
           "ORDER BY ((a.currentPrice - a.purchasePrice) / a.purchasePrice) DESC")
    List<Asset> findTopPerformers();

    /**
     * Find assets meeting target price
     */
    @Query("SELECT a FROM Asset a WHERE a.targetPrice IS NOT NULL AND a.currentPrice >= a.targetPrice")
    List<Asset> findAssetsReachedTargetPrice();

    /**
     * Check if user owns a specific symbol
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Asset a WHERE a.symbol = :symbol AND a.status = 'OWNED'")
    boolean ownsSymbol(@Param("symbol") String symbol);
}
