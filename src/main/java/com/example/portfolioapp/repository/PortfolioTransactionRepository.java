package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.PortfolioTransaction;
import com.example.portfolioapp.entity.SimpleTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, Long> {

    List<PortfolioTransaction> findByPositionId(Long positionId);

    List<PortfolioTransaction> findByTransactionType(SimpleTransactionType type);

    @Query("SELECT t FROM PortfolioTransaction t WHERE t.position.instrument.symbol = :symbol ORDER BY t.transactionDate DESC")
    List<PortfolioTransaction> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT t FROM PortfolioTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<PortfolioTransaction> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM PortfolioTransaction t ORDER BY t.transactionDate DESC")
    Page<PortfolioTransaction> findAllOrderByDateDesc(Pageable pageable);

    @Query("SELECT t FROM PortfolioTransaction t WHERE t.transactionType = 'BUY' ORDER BY t.transactionDate DESC")
    List<PortfolioTransaction> findAllBuys();

    @Query("SELECT t FROM PortfolioTransaction t WHERE t.transactionType = 'SELL' ORDER BY t.transactionDate DESC")
    List<PortfolioTransaction> findAllSells();

    @Query("SELECT SUM(t.realizedGainLoss) FROM PortfolioTransaction t WHERE t.transactionType = 'SELL' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumRealizedGainLoss(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.realizedGainLoss) FROM PortfolioTransaction t WHERE t.transactionType = 'SELL' AND t.isLongTerm = true AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumLongTermGainLoss(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.realizedGainLoss) FROM PortfolioTransaction t WHERE t.transactionType = 'SELL' AND t.isLongTerm = false AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumShortTermGainLoss(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.fees) FROM PortfolioTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumFees(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
