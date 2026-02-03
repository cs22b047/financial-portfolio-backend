package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Dividend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Dividend entity
 */
@Repository
public interface DividendRepository extends JpaRepository<Dividend, Long> {

    /**
     * Find all dividends for a specific asset
     */
    @Query("SELECT d FROM Dividend d WHERE d.asset.id = :assetId ORDER BY d.paymentDate DESC")
    List<Dividend> findByAssetId(@Param("assetId") Long assetId);

    /**
     * Find dividend by asset and payment date (enforces unique constraint)
     */
    @Query("SELECT d FROM Dividend d WHERE d.asset.id = :assetId AND d.paymentDate = :paymentDate")
    Optional<Dividend> findByAssetIdAndPaymentDate(@Param("assetId") Long assetId, 
                                                   @Param("paymentDate") LocalDate paymentDate);

    /**
     * Find dividends within a date range
     */
    @Query("SELECT d FROM Dividend d WHERE d.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY d.paymentDate DESC")
    List<Dividend> findByDateRange(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);

    /**
     * Calculate total dividend income within a date range
     */
    @Query("SELECT SUM(d.totalAmount) FROM Dividend d WHERE d.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountInDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Calculate total dividends received for a specific asset
     */
    @Query("SELECT SUM(d.totalAmount) FROM Dividend d WHERE d.asset.id = :assetId")
    BigDecimal sumTotalAmountByAssetId(@Param("assetId") Long assetId);

    /**
     * Find dividends for a symbol
     */
    @Query("SELECT d FROM Dividend d WHERE d.asset.symbol = :symbol ORDER BY d.paymentDate DESC")
    List<Dividend> findBySymbol(@Param("symbol") String symbol);

    /**
     * Find recent dividends (last N months)
     */
    @Query("SELECT d FROM Dividend d WHERE d.paymentDate >= :sinceDate ORDER BY d.paymentDate DESC")
    List<Dividend> findRecentDividends(@Param("sinceDate") LocalDate sinceDate);

    /**
     * Calculate average dividend per share for an asset
     */
    @Query("SELECT AVG(d.amountPerShare) FROM Dividend d WHERE d.asset.id = :assetId")
    BigDecimal calculateAverageDividendPerShare(@Param("assetId") Long assetId);

    /**
     * Count dividends received for an asset
     */
    @Query("SELECT COUNT(d) FROM Dividend d WHERE d.asset.id = :assetId")
    long countDividendsByAssetId(@Param("assetId") Long assetId);

    /**
     * Check if dividend already exists (duplicate prevention)
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dividend d " +
           "WHERE d.asset.id = :assetId AND d.paymentDate = :paymentDate")
    boolean existsByAssetIdAndPaymentDate(@Param("assetId") Long assetId, 
                                         @Param("paymentDate") LocalDate paymentDate);

    /**
     * Get all dividends grouped by year
     */
    @Query("SELECT d FROM Dividend d ORDER BY d.paymentDate DESC")
    List<Dividend> findAllOrderedByDate();

    /**
     * Find dividends for all assets owned by user
     */
    @Query("SELECT d FROM Dividend d WHERE d.asset.status = 'OWNED' ORDER BY d.paymentDate DESC")
    List<Dividend> findAllForOwnedAssets();

    /**
     * Find dividends by symbol ordered by payment date desc
     */
    @Query("SELECT d FROM Dividend d WHERE d.asset.marketData.symbol = :symbol ORDER BY d.paymentDate DESC")
    List<Dividend> findByAsset_MarketData_SymbolOrderByPaymentDateDesc(@Param("symbol") String symbol);

    /**
     * Find dividends by date range ordered by payment date desc
     */
    @Query("SELECT d FROM Dividend d WHERE d.paymentDate BETWEEN :startDate AND :endDate ORDER BY d.paymentDate DESC")
    List<Dividend> findByPaymentDateBetweenOrderByPaymentDateDesc(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);

    /**
     * Calculate total dividend income for date range
     */
    @Query("SELECT SUM(d.totalAmount) FROM Dividend d WHERE d.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalDividendIncome(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);

    /**
     * Find all dividends ordered by payment date desc
     */
    @Query("SELECT d FROM Dividend d ORDER BY d.paymentDate DESC")
    List<Dividend> findAllByOrderByPaymentDateDesc();
}
