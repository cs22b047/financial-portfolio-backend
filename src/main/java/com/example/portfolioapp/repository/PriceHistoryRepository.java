package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.id = :marketDataId ORDER BY p.priceDate DESC")
    List<PriceHistory> findByMarketDataId(@Param("marketDataId") Long marketDataId);

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.symbol = :symbol ORDER BY p.priceDate DESC")
    List<PriceHistory> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.id = :marketDataId AND p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate ASC")
    List<PriceHistory> findByMarketDataIdAndDateRange(@Param("marketDataId") Long marketDataId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.symbol = :symbol AND p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate ASC")
    List<PriceHistory> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.id = :marketDataId AND p.priceDate = :date")
    Optional<PriceHistory> findByMarketDataIdAndDate(@Param("marketDataId") Long marketDataId, @Param("date") LocalDate date);

    @Query("SELECT p FROM PriceHistory p WHERE p.marketData.id = :marketDataId ORDER BY p.priceDate DESC LIMIT 1")
    Optional<PriceHistory> findLatestByMarketDataId(@Param("marketDataId") Long marketDataId);

    @Query("SELECT MAX(p.priceDate) FROM PriceHistory p WHERE p.marketData.id = :marketDataId")
    Optional<LocalDate> findLatestDateByMarketDataId(@Param("marketDataId") Long marketDataId);

    boolean existsByMarketDataIdAndPriceDate(Long marketDataId, LocalDate priceDate);
}
