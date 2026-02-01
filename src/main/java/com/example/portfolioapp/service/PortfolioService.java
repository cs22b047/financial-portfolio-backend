package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {
    
    @Autowired
    private AssetService assetService;
    
    public Map<String, Object> getPortfolioSummary() {
        List<Asset> ownedAssets = assetService.getOwnedAssets();
        List<Asset> watchlistAssets = assetService.getWatchlistAssets();
        
        Map<String, Object> summary = new HashMap<>();
        
        // Portfolio values
        BigDecimal totalValue = ownedAssets.stream()
                .map(Asset::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalGainLoss = ownedAssets.stream()
                .map(Asset::getGainLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Asset counts
        long ownedCount = ownedAssets.size();
        long watchlistCount = watchlistAssets.size();
        
        // Asset type breakdown
        Map<String, Long> assetTypeBreakdown = new HashMap<>();
        ownedAssets.forEach(asset -> {
            String assetType = asset.getAssetType().getName();
            assetTypeBreakdown.put(assetType, assetTypeBreakdown.getOrDefault(assetType, 0L) + 1);
        });
        
        summary.put("totalValue", totalValue);
        summary.put("totalGainLoss", totalGainLoss);
        summary.put("ownedAssetsCount", ownedCount);
        summary.put("watchlistCount", watchlistCount);
        summary.put("assetTypeBreakdown", assetTypeBreakdown);
        
        return summary;
    }
    
    public List<Asset> getTopPerformers(int limit) {
        return assetService.getOwnedAssets().stream()
                .filter(asset -> asset.getGainLossPercentage().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getGainLossPercentage().compareTo(a.getGainLossPercentage()))
                .limit(limit)
                .toList();
    }
    
    public List<Asset> getTargetPriceAlerts() {
        return assetService.getWatchlistAssets().stream()
                .filter(Asset::isTargetPriceMet)
                .toList();
    }
}