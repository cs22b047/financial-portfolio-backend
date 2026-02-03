package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.TechnicalIndicators;
import com.example.portfolioapp.service.TechnicalIndicatorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for TechnicalIndicators operations
 */
@RestController
@RequestMapping("/api/technical-indicators")
@CrossOrigin(origins = "*")
public class TechnicalIndicatorsController {

    @Autowired
    private TechnicalIndicatorsService technicalIndicatorsService;

    /**
     * Create or update technical indicators
     */
    @PostMapping
    public ResponseEntity<TechnicalIndicators> createOrUpdateTechnicalIndicators(
            @RequestBody TechnicalIndicators indicators) {
        TechnicalIndicators saved = technicalIndicatorsService.createOrUpdateTechnicalIndicators(indicators);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Batch create technical indicators
     */
    @PostMapping("/batch")
    public ResponseEntity<List<TechnicalIndicators>> batchCreateTechnicalIndicators(
            @RequestBody List<TechnicalIndicators> indicatorsList) {
        List<TechnicalIndicators> saved = technicalIndicatorsService.batchCreateTechnicalIndicators(indicatorsList);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get technical indicators by price history ID
     */
    @GetMapping("/price-history/{priceHistoryId}")
    public ResponseEntity<TechnicalIndicators> getByPriceHistoryId(@PathVariable Long priceHistoryId) {
        TechnicalIndicators indicators = technicalIndicatorsService.getByPriceHistoryId(priceHistoryId);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get all technical indicators for a market data ID
     */
    @GetMapping("/market-data/{marketDataId}")
    public ResponseEntity<List<TechnicalIndicators>> getByMarketDataId(@PathVariable Long marketDataId) {
        List<TechnicalIndicators> indicators = technicalIndicatorsService.getByMarketDataId(marketDataId);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get all technical indicators for a symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<TechnicalIndicators>> getBySymbol(@PathVariable String symbol) {
        List<TechnicalIndicators> indicators = technicalIndicatorsService.getBySymbol(symbol);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get technical indicators by symbol and date range
     */
    @GetMapping("/symbol/{symbol}/range")
    public ResponseEntity<List<TechnicalIndicators>> getBySymbolAndDateRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TechnicalIndicators> indicators =
            technicalIndicatorsService.getBySymbolAndDateRange(symbol, startDate, endDate);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get technical indicators by market data ID and date range
     */
    @GetMapping("/market-data/{marketDataId}/range")
    public ResponseEntity<List<TechnicalIndicators>> getByMarketDataIdAndDateRange(
            @PathVariable Long marketDataId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TechnicalIndicators> indicators =
            technicalIndicatorsService.getByMarketDataIdAndDateRange(marketDataId, startDate, endDate);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get latest technical indicators for a symbol
     */
    @GetMapping("/symbol/{symbol}/latest")
    public ResponseEntity<TechnicalIndicators> getLatestBySymbol(@PathVariable String symbol) {
        TechnicalIndicators indicators = technicalIndicatorsService.getLatestBySymbol(symbol);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Get latest technical indicators for a market data ID
     */
    @GetMapping("/market-data/{marketDataId}/latest")
    public ResponseEntity<TechnicalIndicators> getLatestByMarketDataId(@PathVariable Long marketDataId) {
        TechnicalIndicators indicators = technicalIndicatorsService.getLatestByMarketDataId(marketDataId);
        return ResponseEntity.ok(indicators);
    }

    /**
     * Delete technical indicators
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTechnicalIndicators(@PathVariable Long id) {
        technicalIndicatorsService.deleteTechnicalIndicators(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Technical indicators deleted successfully");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }
}
