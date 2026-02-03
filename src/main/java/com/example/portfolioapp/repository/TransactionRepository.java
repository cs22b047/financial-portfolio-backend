package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific asset
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAssetId(@Param("assetId") Long assetId);

    /**
     * Find transactions by type
     */
    List<Transaction> findByTransactionType(TransactionType transactionType);

    /**
     * Find transactions within a date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find buy transactions for an asset
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId AND t.transactionType = 'BUY' " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findBuyTransactionsByAssetId(@Param("assetId") Long assetId);

    /**
     * Find sell transactions for an asset
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId AND t.transactionType = 'SELL' " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findSellTransactionsByAssetId(@Param("assetId") Long assetId);

    /**
     * Find dividend transactions for an asset
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId AND t.transactionType = 'DIVIDEND' " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findDividendTransactionsByAssetId(@Param("assetId") Long assetId);

    /**
     * Calculate total dividends received within a date range
     */
    @Query("SELECT SUM(t.price) FROM Transaction t WHERE t.transactionType = 'DIVIDEND' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumDividendsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total fees paid
     */
    @Query("SELECT SUM(t.fees) FROM Transaction t WHERE t.fees IS NOT NULL")
    BigDecimal calculateTotalFees();

    /**
     * Get recent transactions (last N days)
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :sinceDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Count transactions by type
     */
    long countByTransactionType(TransactionType transactionType);

    /**
     * Find all transactions for a symbol
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.symbol = :symbol ORDER BY t.transactionDate DESC")
    List<Transaction> findBySymbol(@Param("symbol") String symbol);

    /**
     * Find transactions by symbol ordered by date desc
     */
    @Query("SELECT t FROM Transaction t WHERE t.asset.marketData.symbol = :symbol ORDER BY t.transactionDate DESC")
    List<Transaction> findByAsset_MarketData_SymbolOrderByTransactionDateDesc(@Param("symbol") String symbol);

    /**
     * Find transactions by type ordered by date desc
     */
    List<Transaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType type);

    /**
     * Find transactions by date range ordered by date desc
     */
    @Query("SELECT t FROM Transaction t WHERE FUNCTION('DATE', t.transactionDate) BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);

    /**
     * Find all transactions ordered by date desc
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByOrderByTransactionDateDesc();

    /**
     * Calculate total invested (BUY transactions)
     */
    @Query("SELECT SUM(t.quantity * t.price) FROM Transaction t WHERE t.transactionType = 'BUY'")
    BigDecimal calculateTotalInvested();

    /**
     * Calculate realized gains (difference between SELL and BUY)
     */
    @Query("SELECT SUM(CASE WHEN t.transactionType = 'SELL' THEN t.quantity * t.price ELSE -(t.quantity * t.price) END) FROM Transaction t WHERE t.transactionType IN ('BUY', 'SELL')")
    BigDecimal calculateRealizedGains();
}
