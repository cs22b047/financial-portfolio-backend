package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.StockSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockSummaryRepository extends JpaRepository<StockSummary, Long> {

    @Query("SELECT s FROM StockSummary s WHERE s.marketData.id = :marketDataId ORDER BY s.endDate DESC")
    List<StockSummary> findByMarketDataId(@Param("marketDataId") Long marketDataId);

    @Query("SELECT s FROM StockSummary s WHERE s.marketData.symbol = :symbol ORDER BY s.endDate DESC")
    List<StockSummary> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT s FROM StockSummary s WHERE s.marketData.id = :marketDataId AND s.period = :period")
    Optional<StockSummary> findByMarketDataIdAndPeriod(@Param("marketDataId") Long marketDataId,
                                                        @Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.marketData.symbol = :symbol AND s.period = :period")
    Optional<StockSummary> findBySymbolAndPeriod(@Param("symbol") String symbol,
                                                  @Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.period = :period ORDER BY s.totalReturn DESC")
    List<StockSummary> findTopPerformersByPeriod(@Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.period = :period ORDER BY s.sharpeRatio DESC")
    List<StockSummary> findBestSharpeRatioByPeriod(@Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.period = :period ORDER BY s.annualizedVolatility ASC")
    List<StockSummary> findLowestVolatilityByPeriod(@Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.period = :period ORDER BY s.annualizedVolatility DESC")
    List<StockSummary> findHighestVolatilityByPeriod(@Param("period") String period);

    @Query("SELECT s FROM StockSummary s WHERE s.marketData.id = :marketDataId AND s.startDate = :startDate AND s.endDate = :endDate AND s.period = :period")
    Optional<StockSummary> findByMarketDataIdAndDateRangeAndPeriod(@Param("marketDataId") Long marketDataId,
                                                                     @Param("startDate") LocalDate startDate,
                                                                     @Param("endDate") LocalDate endDate,
                                                                     @Param("period") String period);

    boolean existsByMarketDataIdAndPeriod(Long marketDataId, String period);
}
