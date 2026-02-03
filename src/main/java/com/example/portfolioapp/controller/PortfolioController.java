package com.example.portfolioapp.controller;

import com.example.portfolioapp.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for portfolio analytics and summaries.
 */
@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    // ==================== Portfolio Summary ====================

    @GetMapping("/summary")
    public Map<String, Object> getPortfolioSummary() {
        return portfolioService.getPortfolioSummary();
    }

    // ==================== Allocation Analysis ====================

    @GetMapping("/allocation/type")
    public Map<String, Object> getAllocationByType() {
        return portfolioService.getAllocationByType();
    }

    @GetMapping("/allocation/sector")
    public Map<String, Object> getAllocationBySector() {
        return portfolioService.getAllocationBySector();
    }

    // ==================== Performance ====================

    @GetMapping("/top-performers")
    public List<Map<String, Object>> getTopPerformers(@RequestParam(defaultValue = "5") int limit) {
        return portfolioService.getTopPerformers(limit);
    }

    @GetMapping("/worst-performers")
    public List<Map<String, Object>> getWorstPerformers(@RequestParam(defaultValue = "5") int limit) {
        return portfolioService.getWorstPerformers(limit);
    }

    @GetMapping("/largest-positions")
    public List<Map<String, Object>> getLargestPositions(@RequestParam(defaultValue = "10") int limit) {
        return portfolioService.getLargestPositions(limit);
    }

    // ==================== Realized Gains ====================

    @GetMapping("/realized-gains")
    public Map<String, Object> getRealizedGains(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return portfolioService.getRealizedGainsSummary(startDate, endDate);
    }

    @GetMapping("/realized-gains/ytd")
    public Map<String, Object> getYTDRealizedGains() {
        LocalDate startDate = LocalDate.now().withDayOfYear(1);
        LocalDate endDate = LocalDate.now();
        return portfolioService.getRealizedGainsSummary(startDate, endDate);
    }

    // ==================== Alerts ====================

    @GetMapping("/alerts/watchlist")
    public List<Map<String, Object>> getWatchlistAlerts() {
        return portfolioService.getWatchlistAlerts();
    }
}
