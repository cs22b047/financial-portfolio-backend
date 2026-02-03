package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import com.example.portfolioapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for Transaction operations
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get transactions by symbol
     */
    public List<Transaction> getTransactionsBySymbol(String symbol) {
        return transactionRepository.findByAsset_MarketData_SymbolOrderByTransactionDateDesc(symbol);
    }

    /**
     * Get transactions by type
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByTransactionTypeOrderByTransactionDateDesc(type);
    }

    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate);
    }

    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }

    /**
     * Calculate total invested (sum of BUY transactions)
     */
    public BigDecimal calculateTotalInvested() {
        BigDecimal total = transactionRepository.calculateTotalInvested();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate realized gains (from SELL transactions)
     */
    public BigDecimal calculateRealizedGains() {
        BigDecimal total = transactionRepository.calculateRealizedGains();
        return total != null ? total : BigDecimal.ZERO;
    }
}
