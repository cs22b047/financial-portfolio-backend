package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.TechnicalIndicators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicalIndicatorsRepository extends JpaRepository<TechnicalIndicators, Long> {

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.id = :priceHistoryId")
    Optional<TechnicalIndicators> findByPriceHistoryId(@Param("priceHistoryId") Long priceHistoryId);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.id = :marketDataId ORDER BY t.indicatorDate DESC")
    List<TechnicalIndicators> findByMarketDataId(@Param("marketDataId") Long marketDataId);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.symbol = :symbol ORDER BY t.indicatorDate DESC")
    List<TechnicalIndicators> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.id = :marketDataId AND t.indicatorDate BETWEEN :startDate AND :endDate ORDER BY t.indicatorDate ASC")
    List<TechnicalIndicators> findByMarketDataIdAndDateRange(@Param("marketDataId") Long marketDataId,
                                                               @Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.symbol = :symbol AND t.indicatorDate BETWEEN :startDate AND :endDate ORDER BY t.indicatorDate ASC")
    List<TechnicalIndicators> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.id = :marketDataId ORDER BY t.indicatorDate DESC LIMIT 1")
    Optional<TechnicalIndicators> findLatestByMarketDataId(@Param("marketDataId") Long marketDataId);

    @Query("SELECT t FROM TechnicalIndicators t WHERE t.priceHistory.marketData.symbol = :symbol ORDER BY t.indicatorDate DESC LIMIT 1")
    Optional<TechnicalIndicators> findLatestBySymbol(@Param("symbol") String symbol);

    boolean existsByPriceHistoryId(Long priceHistoryId);
}
