package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    
    Optional<MarketData> findBySymbol(String symbol);
    
    @Query("SELECT m FROM MarketData m WHERE m.lastUpdated < :cutoffTime")
    List<MarketData> findStaleData(LocalDateTime cutoffTime);
    
    @Query("SELECT m FROM MarketData m WHERE m.assetType.code = :assetTypeCode")
    List<MarketData> findByAssetTypeCode(String assetTypeCode);
}