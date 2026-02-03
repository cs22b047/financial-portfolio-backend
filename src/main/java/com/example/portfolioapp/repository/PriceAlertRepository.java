package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.PriceAlert;
import com.example.portfolioapp.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByPositionId(Long positionId);

    List<PriceAlert> findByIsActiveTrue();

    List<PriceAlert> findByIsTriggeredTrue();

    List<PriceAlert> findByAlertType(AlertType alertType);

    @Query("SELECT a FROM PriceAlert a WHERE a.position.instrument.symbol = :symbol")
    List<PriceAlert> findBySymbol(@Param("symbol") String symbol);

    @Query("SELECT a FROM PriceAlert a WHERE a.isActive = true AND a.isTriggered = false")
    List<PriceAlert> findActiveUntriggeredAlerts();

    @Query("SELECT a FROM PriceAlert a WHERE a.isActive = true AND a.isTriggered = false AND a.position.id = :positionId")
    List<PriceAlert> findActiveAlertsForPosition(@Param("positionId") Long positionId);
}
