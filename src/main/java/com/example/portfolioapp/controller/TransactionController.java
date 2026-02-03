package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import com.example.portfolioapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Transaction operations
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Get transactions by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Transaction>> getTransactionsBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(transactionService.getTransactionsBySymbol(symbol));
    }

    /**
     * Get transactions by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Transaction>> getTransactionsByType(@PathVariable TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(type));
    }

    /**
     * Get transactions by date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Validate required parameters
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    /**
     * Get all transactions
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * Get total invested
     */
    @GetMapping("/total-invested")
    public ResponseEntity<BigDecimal> getTotalInvested() {
        return ResponseEntity.ok(transactionService.calculateTotalInvested());
    }

    /**
     * Get realized gains
     */
    @GetMapping("/realized-gains")
    public ResponseEntity<BigDecimal> getRealizedGains() {
        return ResponseEntity.ok(transactionService.calculateRealizedGains());
    }
}
