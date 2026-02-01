package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Dividend;
import com.example.portfolioapp.entity.DividendType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<Dividend, Long> {
    
    List<Dividend> findByAssetId(Long assetId);
    
    List<Dividend> findByDividendType(DividendType dividendType);
    
    List<Dividend> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT d FROM Dividend d WHERE d.asset.id = :assetId ORDER BY d.paymentDate DESC")
    List<Dividend> findByAssetIdOrderByDateDesc(Long assetId);
}