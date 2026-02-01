package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.repository.MarketDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MarketDataService {
    
    @Autowired
    private MarketDataRepository marketDataRepository;
    
    public List<MarketData> getAllMarketData() {
        return marketDataRepository.findAll();
    }
    
    public Optional<MarketData> getMarketDataById(Long id) {
        return marketDataRepository.findById(id);
    }
    
    public Optional<MarketData> getMarketDataBySymbol(String symbol) {
        return marketDataRepository.findBySymbol(symbol);
    }
    
    public List<MarketData> getStaleMarketData(int maxAgeMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(maxAgeMinutes);
        return marketDataRepository.findStaleData(cutoffTime);
    }
    
    public List<MarketData> getMarketDataByAssetType(String assetTypeCode) {
        return marketDataRepository.findByAssetTypeCode(assetTypeCode);
    }
    
    public MarketData saveMarketData(MarketData marketData) {
        return marketDataRepository.save(marketData);
    }
}