package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.TaxLot;
import com.example.portfolioapp.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaxLotRepository extends JpaRepository<TaxLot, Long> {

    List<TaxLot> findByPosition(Position position);

    List<TaxLot> findByPositionId(Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false ORDER BY t.purchaseDate ASC")
    List<TaxLot> findOpenLotsFIFO(@Param("positionId") Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false ORDER BY t.purchaseDate DESC")
    List<TaxLot> findOpenLotsLIFO(@Param("positionId") Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false ORDER BY t.costBasisPerUnit DESC")
    List<TaxLot> findOpenLotsHIFO(@Param("positionId") Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false")
    List<TaxLot> findOpenLots(@Param("positionId") Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = true")
    List<TaxLot> findClosedLots(@Param("positionId") Long positionId);

    @Query("SELECT SUM(t.remainingQuantity) FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false")
    BigDecimal sumRemainingQuantity(@Param("positionId") Long positionId);

    @Query("SELECT SUM(t.remainingQuantity * t.costBasisPerUnit) FROM TaxLot t WHERE t.position.id = :positionId AND t.isClosed = false")
    BigDecimal sumRemainingCostBasis(@Param("positionId") Long positionId);

    @Query("SELECT t FROM TaxLot t WHERE t.purchaseDate BETWEEN :startDate AND :endDate")
    List<TaxLot> findByPurchaseDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TaxLot t WHERE t.isWashSale = true")
    List<TaxLot> findWashSaleLots();
}
