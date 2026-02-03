package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Asset asset;
    private MarketData appleStock;

    @BeforeEach
    void setUp() {
        AssetType stockType = new AssetType();
        stockType.setId(1L);
        stockType.setCode("STOCK");
        stockType.setName("STOCK");

        appleStock = new MarketData();
        appleStock.setId(1L);
        appleStock.setSymbol("AAPL");
        appleStock.setName("Apple Inc.");
        appleStock.setAssetType(stockType);
        appleStock.setCurrency("USD");

        asset = new Asset();
        asset.setId(1L);
        asset.setMarketData(appleStock);
        asset.setQuantity(new BigDecimal("100"));
        asset.setAverageCost(new BigDecimal("170.00"));
        asset.setStatus(AssetStatus.OWNED);
    }

    @Test
    void getTransactionsBySymbol_Success() {
        // Arrange
        Transaction txn1 = new Transaction();
        txn1.setAsset(asset);
        txn1.setTransactionType(TransactionType.BUY);
        txn1.setQuantity(new BigDecimal("50"));
        
        Transaction txn2 = new Transaction();
        txn2.setAsset(asset);
        txn2.setTransactionType(TransactionType.SELL);
        txn2.setQuantity(new BigDecimal("20"));

        when(transactionRepository.findByAsset_MarketData_SymbolOrderByTransactionDateDesc("AAPL"))
            .thenReturn(Arrays.asList(txn1, txn2));

        // Act
        List<Transaction> result = transactionService.getTransactionsBySymbol("AAPL");

        // Assert
        assertEquals(2, result.size());
        assertEquals(TransactionType.BUY, result.get(0).getTransactionType());
        assertEquals(TransactionType.SELL, result.get(1).getTransactionType());
    }

    @Test
    void getTransactionsByType_Success() {
        // Arrange
        Transaction buyTxn = new Transaction();
        buyTxn.setTransactionType(TransactionType.BUY);
        
        when(transactionRepository.findByTransactionTypeOrderByTransactionDateDesc(TransactionType.BUY))
            .thenReturn(Arrays.asList(buyTxn));

        // Act
        List<Transaction> result = transactionService.getTransactionsByType(TransactionType.BUY);

        // Assert
        assertEquals(1, result.size());
        assertEquals(TransactionType.BUY, result.get(0).getTransactionType());
    }

    @Test
    void getTransactionsByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        
        Transaction txn = new Transaction();
        txn.setTransactionDate(LocalDate.of(2025, 6, 15).atStartOfDay());
        
        when(transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate))
            .thenReturn(Arrays.asList(txn));

        // Act
        List<Transaction> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        // Assert
        assertEquals(1, result.size());
        // Transaction date is LocalDateTime, so we need to extract the date part
        assertEquals(LocalDate.of(2025, 6, 15), result.get(0).getTransactionDate().toLocalDate());
    }

    @Test
    void getAllTransactions_Success() {
        // Arrange
        Transaction txn1 = new Transaction();
        Transaction txn2 = new Transaction();
        
        when(transactionRepository.findAllByOrderByTransactionDateDesc())
            .thenReturn(Arrays.asList(txn1, txn2));

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertEquals(2, result.size());
        verify(transactionRepository).findAllByOrderByTransactionDateDesc();
    }

    @Test
    void calculateTotalInvested_Success() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("17000.00");
        when(transactionRepository.calculateTotalInvested()).thenReturn(expectedTotal);

        // Act
        BigDecimal result = transactionService.calculateTotalInvested();

        // Assert
        assertEquals(expectedTotal, result);
    }

    @Test
    void calculateTotalInvested_NoData_ReturnsZero() {
        // Arrange
        when(transactionRepository.calculateTotalInvested()).thenReturn(null);

        // Act
        BigDecimal result = transactionService.calculateTotalInvested();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateRealizedGains_Success() {
        // Arrange
        BigDecimal expectedGains = new BigDecimal("1500.00");
        when(transactionRepository.calculateRealizedGains()).thenReturn(expectedGains);

        // Act
        BigDecimal result = transactionService.calculateRealizedGains();

        // Assert
        assertEquals(expectedGains, result);
    }

    @Test
    void calculateRealizedGains_NoData_ReturnsZero() {
        // Arrange
        when(transactionRepository.calculateRealizedGains()).thenReturn(null);

        // Act
        BigDecimal result = transactionService.calculateRealizedGains();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }
}
