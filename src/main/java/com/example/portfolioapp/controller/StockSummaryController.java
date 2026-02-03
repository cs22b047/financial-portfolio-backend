package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.StockSummary;
import com.example.portfolioapp.service.StockSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for StockSummary operations
 */
@RestController
@RequestMapping("/api/stock-summary")
@CrossOrigin(origins = "*")
public class StockSummaryController {

    @Autowired
    private StockSummaryService stockSummaryService;

    /**
     * Create or update stock summary
     */
    @PostMapping
    public ResponseEntity<StockSummary> createOrUpdateStockSummary(@RequestBody StockSummary summary) {
        StockSummary saved = stockSummaryService.createOrUpdateStockSummary(summary);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Batch create stock summaries
     */
    @PostMapping("/batch")
    public ResponseEntity<List<StockSummary>> batchCreateStockSummaries(
            @RequestBody List<StockSummary> summaries) {
        List<StockSummary> saved = stockSummaryService.batchCreateStockSummaries(summaries);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get stock summaries by market data ID
     */
    @GetMapping("/market-data/{marketDataId}")
    public ResponseEntity<List<StockSummary>> getByMarketDataId(@PathVariable Long marketDataId) {
        List<StockSummary> summaries = stockSummaryService.getByMarketDataId(marketDataId);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get stock summaries by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<StockSummary>> getBySymbol(@PathVariable String symbol) {
        List<StockSummary> summaries = stockSummaryService.getBySymbol(symbol);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get stock summary by market data ID and period
     */
    @GetMapping("/market-data/{marketDataId}/period/{period}")
    public ResponseEntity<StockSummary> getByMarketDataIdAndPeriod(
            @PathVariable Long marketDataId,
            @PathVariable String period) {
        StockSummary summary = stockSummaryService.getByMarketDataIdAndPeriod(marketDataId, period);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get stock summary by symbol and period
     */
    @GetMapping("/symbol/{symbol}/period/{period}")
    public ResponseEntity<StockSummary> getBySymbolAndPeriod(
            @PathVariable String symbol,
            @PathVariable String period) {
        StockSummary summary = stockSummaryService.getBySymbolAndPeriod(symbol, period);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get top performers by period
     */
    @GetMapping("/top-performers/{period}")
    public ResponseEntity<List<StockSummary>> getTopPerformersByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "10") int limit) {
        List<StockSummary> performers = stockSummaryService.getTopPerformersByPeriod(period, limit);
        return ResponseEntity.ok(performers);
    }

    /**
     * Get best Sharpe ratios by period
     */
    @GetMapping("/best-sharpe/{period}")
    public ResponseEntity<List<StockSummary>> getBestSharpeRatioByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "10") int limit) {
        List<StockSummary> summaries = stockSummaryService.getBestSharpeRatioByPeriod(period, limit);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get lowest volatility stocks by period
     */
    @GetMapping("/lowest-volatility/{period}")
    public ResponseEntity<List<StockSummary>> getLowestVolatilityByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "10") int limit) {
        List<StockSummary> summaries = stockSummaryService.getLowestVolatilityByPeriod(period, limit);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get highest volatility stocks by period
     */
    @GetMapping("/highest-volatility/{period}")
    public ResponseEntity<List<StockSummary>> getHighestVolatilityByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "10") int limit) {
        List<StockSummary> summaries = stockSummaryService.getHighestVolatilityByPeriod(period, limit);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Delete stock summary
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStockSummary(@PathVariable Long id) {
        stockSummaryService.deleteStockSummary(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Stock summary deleted successfully");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }
}
