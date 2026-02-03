package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.PriceHistory;
import com.example.portfolioapp.service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for PriceHistory operations
 */
@RestController
@RequestMapping("/api/price-history")
@CrossOrigin(origins = "*")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    /**
     * Get price history by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<PriceHistory>> getBySymbol(@PathVariable String symbol) {
        List<PriceHistory> priceHistory = priceHistoryService.getBySymbol(symbol);
        return ResponseEntity.ok(priceHistory);
    }

    /**
     * Get price history by symbol and date range
     */
    @GetMapping("/symbol/{symbol}/range")
    public ResponseEntity<List<PriceHistory>> getBySymbolAndDateRange(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PriceHistory> priceHistory = priceHistoryService.getBySymbolAndDateRange(symbol, startDate, endDate);
        return ResponseEntity.ok(priceHistory);
    }

    /**
     * Get price history by market data ID
     */
    @GetMapping("/market-data/{marketDataId}")
    public ResponseEntity<List<PriceHistory>> getByMarketDataId(@PathVariable Long marketDataId) {
        List<PriceHistory> priceHistory = priceHistoryService.getByMarketDataId(marketDataId);
        return ResponseEntity.ok(priceHistory);
    }

    /**
     * Get price history by market data ID and date range
     */
    @GetMapping("/market-data/{marketDataId}/range")
    public ResponseEntity<List<PriceHistory>> getByMarketDataIdAndDateRange(
            @PathVariable Long marketDataId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PriceHistory> priceHistory = priceHistoryService.getByMarketDataIdAndDateRange(marketDataId, startDate, endDate);
        return ResponseEntity.ok(priceHistory);
    }

    /**
     * Get latest price history entry by market data ID
     */
    @GetMapping("/market-data/{marketDataId}/latest")
    public ResponseEntity<PriceHistory> getLatestByMarketDataId(@PathVariable Long marketDataId) {
        PriceHistory priceHistory = priceHistoryService.getLatestByMarketDataId(marketDataId);
        if (priceHistory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(priceHistory);
    }
}
