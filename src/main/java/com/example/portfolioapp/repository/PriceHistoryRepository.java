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

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.id = :instrumentId ORDER BY p.priceDate DESC")
    List<PriceHistory> findByInstrumentId(@Param("instrumentId") Long instrumentId);

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.symbol = :symbol ORDER BY p.priceDate DESC")
    List<PriceHistory> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.id = :instrumentId AND p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate ASC")
    List<PriceHistory> findByInstrumentIdAndDateRange(@Param("instrumentId") Long instrumentId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.symbol = :symbol AND p.priceDate BETWEEN :startDate AND :endDate ORDER BY p.priceDate ASC")
    List<PriceHistory> findBySymbolAndDateRange(@Param("symbol") String symbol,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.id = :instrumentId AND p.priceDate = :date")
    Optional<PriceHistory> findByInstrumentIdAndDate(@Param("instrumentId") Long instrumentId, @Param("date") LocalDate date);

    @Query("SELECT p FROM PriceHistory p WHERE p.instrument.id = :instrumentId ORDER BY p.priceDate DESC LIMIT 1")
    Optional<PriceHistory> findLatestByInstrumentId(@Param("instrumentId") Long instrumentId);

    @Query("SELECT MAX(p.priceDate) FROM PriceHistory p WHERE p.instrument.id = :instrumentId")
    Optional<LocalDate> findLatestDateByInstrumentId(@Param("instrumentId") Long instrumentId);

    boolean existsByInstrumentIdAndPriceDate(Long instrumentId, LocalDate priceDate);
}
