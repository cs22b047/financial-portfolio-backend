package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for MarketData entity (master stock data)
 */
@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    /**
     * Find market data by stock symbol
     */
    Optional<MarketData> findBySymbol(String symbol);

    /**
     * Find all stocks in a sector
     */
    List<MarketData> findBySector(String sector);

    /**
     * Find all stocks in an industry
     */
    List<MarketData> findByIndustry(String industry);

    /**
     * Find stocks by exchange
     */
    List<MarketData> findByExchange(String exchange);

    /**
     * Find stocks by asset type
     */
    @Query("SELECT md FROM MarketData md WHERE md.assetType.id = :assetTypeId")
    List<MarketData> findByAssetTypeId(@Param("assetTypeId") Long assetTypeId);

    /**
     * Find stocks updated after a certain time
     */
    @Query("SELECT md FROM MarketData md WHERE md.lastUpdated > :afterDate ORDER BY md.lastUpdated DESC")
    List<MarketData> findByLastUpdatedAfter(@Param("afterDate") LocalDateTime afterDate);

    /**
     * Find stale data (not updated in last N hours)
     */
    @Query("SELECT md FROM MarketData md WHERE md.lastUpdated < :beforeDate ORDER BY md.lastUpdated ASC")
    List<MarketData> findStaleData(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Search stocks by name or symbol
     */
    @Query("SELECT md FROM MarketData md WHERE " +
           "LOWER(md.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(md.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<MarketData> searchBySymbolOrName(@Param("query") String query);

    /**
     * Check if symbol exists
     */
    boolean existsBySymbol(String symbol);

    /**
     * Get top gainers for the day
     */
    @Query("SELECT md FROM MarketData md WHERE md.dayChangePercent IS NOT NULL " +
           "ORDER BY md.dayChangePercent DESC")
    List<MarketData> findTopGainers();

    /**
     * Get top losers for the day
     */
    @Query("SELECT md FROM MarketData md WHERE md.dayChangePercent IS NOT NULL " +
           "ORDER BY md.dayChangePercent ASC")
    List<MarketData> findTopLosers();
}
