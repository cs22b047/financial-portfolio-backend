package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    List<Asset> findByStatus(AssetStatus status);
    
    List<Asset> findByStatusIn(List<AssetStatus> statuses);
    
    @Query("SELECT a FROM Asset a WHERE a.status = 'OWNED'")
    List<Asset> findOwnedAssets();
    
    @Query("SELECT a FROM Asset a WHERE a.status IN ('WATCHLIST', 'RESEARCH', 'PENDING_PURCHASE')")
    List<Asset> findWatchlistAssets();
    
    @Query("SELECT a FROM Asset a WHERE a.assetType.code = :assetTypeCode")
    List<Asset> findByAssetTypeCode(String assetTypeCode);
    
    @Query("SELECT a FROM Asset a WHERE a.symbol = :symbol AND a.status = :status")
    Asset findBySymbolAndStatus(String symbol, AssetStatus status);
}