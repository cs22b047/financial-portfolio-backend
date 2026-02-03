package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Dividend;
import com.example.portfolioapp.service.DividendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Dividend operations.
 * 
 * Endpoints implement the calculation logic for:
 * ✅ shares_held - Retrieved from assets.quantity
 * ✅ total_amount - Calculated as amount_per_share × shares_held
 */
@RestController
@RequestMapping("/api/dividends")
@CrossOrigin(origins = "*")
public class DividendController {

    @Autowired
    private DividendService dividendService;

    /**
     * Record a new dividend payment.
     * 
     * Example request body:
     * {
     *   "symbol": "AAPL",
     *   "paymentDate": "2025-02-01",
     *   "amountPerShare": 0.25
     * }
     * 
     * The service will:
     * 1. Fetch shares_held from assets.quantity
     * 2. Calculate total_amount = amount_per_share × shares_held
     * 3. Save dividend with calculated values
     */
    @PostMapping
    public ResponseEntity<Dividend> recordDividend(@RequestBody DividendRequest request) {
        // Validate required fields
        if (request.getSymbol() == null || request.getPaymentDate() == null || request.getAmountPerShare() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Dividend dividend = dividendService.recordDividend(
            request.getSymbol(),
            request.getPaymentDate(),
            request.getAmountPerShare()
        );
        return new ResponseEntity<>(dividend, HttpStatus.CREATED);
    }

    /**
     * Record dividend with explicit shares (for historical data)
     */
    @PostMapping("/with-shares")
    public ResponseEntity<Dividend> recordDividendWithShares(@RequestBody DividendWithSharesRequest request) {
        Dividend dividend = dividendService.recordDividendWithShares(
            request.getSymbol(),
            request.getPaymentDate(),
            request.getAmountPerShare(),
            request.getSharesAtPayment()
        );
        return new ResponseEntity<>(dividend, HttpStatus.CREATED);
    }

    /**
     * Get all dividends for a specific symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Dividend>> getDividendsBySymbol(@PathVariable String symbol) {
        List<Dividend> dividends = dividendService.getDividendsBySymbol(symbol);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get all dividends for an asset
     */
    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<Dividend>> getDividendsByAssetId(@PathVariable Long assetId) {
        List<Dividend> dividends = dividendService.getDividendsByAssetId(assetId);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividends within a date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Dividend>> getDividendsInDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Validate required parameters
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Dividend> dividends = dividendService.getDividendsInDateRange(startDate, endDate);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get total dividend income for a date range
     */
    @GetMapping("/total-income")
    public ResponseEntity<BigDecimal> getTotalDividendIncome(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = dividendService.calculateTotalDividendIncome(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    /**
     * Get recent dividends (last N months)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Dividend>> getRecentDividends(
            @RequestParam(defaultValue = "12") int months) {
        List<Dividend> dividends = dividendService.getRecentDividends(months);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get all dividends
     */
    @GetMapping
    public ResponseEntity<List<Dividend>> getAllDividends() {
        List<Dividend> dividends = dividendService.getAllDividends();
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get all dividends for owned assets
     */
    @GetMapping("/owned")
    public ResponseEntity<List<Dividend>> getAllDividendsForOwnedAssets() {
        List<Dividend> dividends = dividendService.getAllDividendsForOwnedAssets();
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividend by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dividend> getDividendById(@PathVariable Long id) {
        Dividend dividend = dividendService.getDividendById(id);
        return ResponseEntity.ok(dividend);
    }

    /**
     * Delete a dividend
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDividend(@PathVariable Long id) {
        dividendService.deleteDividend(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Dividend deleted successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    // DTOs for request bodies

    public static class DividendRequest {
        private String symbol;
        private LocalDate paymentDate;
        private BigDecimal amountPerShare;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
        }

        public BigDecimal getAmountPerShare() {
            return amountPerShare;
        }

        public void setAmountPerShare(BigDecimal amountPerShare) {
            this.amountPerShare = amountPerShare;
        }
    }

    public static class DividendWithSharesRequest extends DividendRequest {
        private BigDecimal sharesAtPayment;

        public BigDecimal getSharesAtPayment() {
            return sharesAtPayment;
        }

        public void setSharesAtPayment(BigDecimal sharesAtPayment) {
            this.sharesAtPayment = sharesAtPayment;
        }
    }
}
