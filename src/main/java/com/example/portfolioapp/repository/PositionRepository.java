package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Position;
import com.example.portfolioapp.entity.PositionStatus;
import com.example.portfolioapp.entity.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByStatus(PositionStatus status);

    List<Position> findByStatusIn(List<PositionStatus> statuses);

    @Query("SELECT p FROM Position p WHERE p.status = 'OPEN'")
    List<Position> findOpenPositions();

    @Query("SELECT p FROM Position p WHERE p.status IN ('WATCHLIST', 'RESEARCH')")
    List<Position> findWatchlistPositions();

    @Query("SELECT p FROM Position p WHERE p.instrument.symbol = :symbol")
    Optional<Position> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT p FROM Position p WHERE p.instrument.symbol = :symbol AND p.status = :status")
    Optional<Position> findBySymbolAndStatus(@Param("symbol") String symbol, @Param("status") PositionStatus status);

    @Query("SELECT p FROM Position p WHERE p.instrument.instrumentType = :type AND p.status = 'OPEN'")
    List<Position> findOpenPositionsByType(@Param("type") InstrumentType type);

    @Query("SELECT p FROM Position p WHERE p.alertsEnabled = true AND p.status IN ('OPEN', 'WATCHLIST')")
    List<Position> findPositionsWithAlertsEnabled();

    @Query("SELECT p FROM Position p WHERE p.targetBuyPrice IS NOT NULL AND p.status = 'WATCHLIST'")
    List<Position> findWatchlistWithTargetPrice();

    @Query("SELECT p FROM Position p JOIN p.instrument i WHERE p.status = 'OPEN' ORDER BY (p.totalQuantity * i.currentPrice) DESC")
    List<Position> findOpenPositionsOrderByValue();

    @Query("SELECT COUNT(p) FROM Position p WHERE p.status = 'OPEN'")
    long countOpenPositions();

    @Query("SELECT COUNT(p) FROM Position p WHERE p.status IN ('WATCHLIST', 'RESEARCH')")
    long countWatchlistPositions();
}
