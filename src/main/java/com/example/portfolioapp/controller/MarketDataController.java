package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for MarketData operations (master stock data)
 */
@RestController
@RequestMapping("/api/market-data")
@CrossOrigin(origins = "*")
public class MarketDataController {

    @Autowired
    private MarketDataService marketDataService;

    /**
     * Create or update market data
     */
    @PostMapping
    public ResponseEntity<MarketData> createOrUpdateMarketData(@RequestBody MarketData marketData) {
        MarketData saved = marketDataService.createOrUpdateMarketData(marketData);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get market data by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<MarketData> getBySymbol(@PathVariable String symbol) {
        MarketData marketData = marketDataService.getBySymbol(symbol);
        return ResponseEntity.ok(marketData);
    }

    /**
     * Search stocks by name or symbol
     */
    @GetMapping("/search")
    public ResponseEntity<List<MarketData>> searchStocks(@RequestParam String query) {
        List<MarketData> results = marketDataService.searchStocks(query);
        return ResponseEntity.ok(results);
    }

    /**
     * Get stocks by sector
     */
    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<MarketData>> getStocksBySector(@PathVariable String sector) {
        List<MarketData> stocks = marketDataService.getStocksBySector(sector);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Get top gainers
     */
    @GetMapping("/top-gainers")
    public ResponseEntity<List<MarketData>> getTopGainers(
            @RequestParam(defaultValue = "10") int limit) {
        List<MarketData> gainers = marketDataService.getTopGainers(limit);
        return ResponseEntity.ok(gainers);
    }

    /**
     * Get top losers
     */
    @GetMapping("/top-losers")
    public ResponseEntity<List<MarketData>> getTopLosers(
            @RequestParam(defaultValue = "10") int limit) {
        List<MarketData> losers = marketDataService.getTopLosers(limit);
        return ResponseEntity.ok(losers);
    }

    /**
     * Get all market data
     */
    @GetMapping
    public ResponseEntity<List<MarketData>> getAllMarketData() {
        List<MarketData> data = marketDataService.getAllMarketData();
        return ResponseEntity.ok(data);
    }

    /**
     * Get market data by asset type
     */
    @GetMapping("/asset-type/{assetTypeId}")
    public ResponseEntity<List<MarketData>> getByAssetType(@PathVariable Long assetTypeId) {
        List<MarketData> assets = marketDataService.getByAssetType(assetTypeId);
        return ResponseEntity.ok(assets);
    }

    /**
     * Delete market data
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMarketData(@PathVariable Long id) {
        marketDataService.deleteMarketData(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Market data deleted successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }
}
