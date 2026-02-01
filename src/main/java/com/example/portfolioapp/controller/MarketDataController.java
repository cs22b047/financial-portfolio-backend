package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@CrossOrigin(origins = "*")
public class MarketDataController {
    
    @Autowired
    private MarketDataService marketDataService;
    
    @GetMapping
    public List<MarketData> getAllMarketData() {
        return marketDataService.getAllMarketData();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MarketData> getMarketDataById(@PathVariable Long id) {
        return marketDataService.getMarketDataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<MarketData> getMarketDataBySymbol(@PathVariable String symbol) {
        return marketDataService.getMarketDataBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/stale")
    public List<MarketData> getStaleMarketData(@RequestParam(defaultValue = "15") int maxAgeMinutes) {
        return marketDataService.getStaleMarketData(maxAgeMinutes);
    }
    
    @GetMapping("/type/{assetTypeCode}")
    public List<MarketData> getMarketDataByAssetType(@PathVariable String assetTypeCode) {
        return marketDataService.getMarketDataByAssetType(assetTypeCode);
    }
    
    @PostMapping
    public MarketData createOrUpdateMarketData(@RequestBody MarketData marketData) {
        return marketDataService.saveMarketData(marketData);
    }
}