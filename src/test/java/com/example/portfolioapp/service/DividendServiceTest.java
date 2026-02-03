package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.exception.DuplicateResourceException;
import com.example.portfolioapp.exception.InvalidOperationException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AssetRepository;
import com.example.portfolioapp.repository.DividendRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DividendServiceTest {

    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DividendService dividendService;

    private Asset ownedAsset;
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

        ownedAsset = new Asset();
        ownedAsset.setId(1L);
        ownedAsset.setMarketData(appleStock);
        ownedAsset.setQuantity(new BigDecimal("100"));
        ownedAsset.setAverageCost(new BigDecimal("170.00"));
        ownedAsset.setStatus(AssetStatus.OWNED);
    }

    @Test
    void recordDividend_Success() {
        // Arrange
        LocalDate paymentDate = LocalDate.of(2026, 1, 15);
        BigDecimal amountPerShare = new BigDecimal("0.95");
        
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));
        when(dividendRepository.existsByAssetIdAndPaymentDate(1L, paymentDate)).thenReturn(false);
        when(dividendRepository.save(any(Dividend.class))).thenAnswer(i -> {
            Dividend dividend = i.getArgument(0);
            dividend.setId(1L);
            return dividend;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Dividend result = dividendService.recordDividend("AAPL", paymentDate, amountPerShare);

        // Assert
        assertNotNull(result);
        assertEquals(ownedAsset, result.getAsset());
        assertEquals(paymentDate, result.getPaymentDate());
        assertEquals(amountPerShare, result.getAmountPerShare());
        assertEquals(new BigDecimal("100"), result.getSharesHeld());
        // Total amount = 0.95 * 100 = 95.00
        assertEquals(0, new BigDecimal("95.00").compareTo(result.getTotalAmount()));
        
        verify(dividendRepository).save(any(Dividend.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void recordDividend_AssetNotFound_ThrowsException() {
        // Arrange
        when(assetRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            dividendService.recordDividend("INVALID", LocalDate.now(), new BigDecimal("0.95"))
        );
        
        verify(dividendRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void recordDividend_WatchlistAsset_ThrowsException() {
        // Arrange
        ownedAsset.setStatus(AssetStatus.WATCHLIST);
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            dividendService.recordDividend("AAPL", LocalDate.now(), new BigDecimal("0.95"))
        );
        
        verify(dividendRepository, never()).save(any());
    }

    @Test
    void recordDividend_DuplicateDividend_ThrowsException() {
        // Arrange
        LocalDate paymentDate = LocalDate.of(2026, 1, 15);
        
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));
        when(dividendRepository.existsByAssetIdAndPaymentDate(1L, paymentDate)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> 
            dividendService.recordDividend("AAPL", paymentDate, new BigDecimal("0.95"))
        );
        
        verify(dividendRepository, never()).save(any());
    }

    @Test
    void recordDividend_CalculatesCorrectTotalAmount() {
        // Arrange
        LocalDate paymentDate = LocalDate.now();
        BigDecimal amountPerShare = new BigDecimal("2.50");
        ownedAsset.setQuantity(new BigDecimal("50.5")); // Fractional shares
        
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));
        when(dividendRepository.existsByAssetIdAndPaymentDate(1L, paymentDate)).thenReturn(false);
        when(dividendRepository.save(any(Dividend.class))).thenAnswer(i -> {
            Dividend dividend = i.getArgument(0);
            dividend.setId(1L);
            return dividend;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Dividend result = dividendService.recordDividend("AAPL", paymentDate, amountPerShare);

        // Assert
        // Total amount = 2.50 * 50.5 = 126.25
        assertEquals(0, new BigDecimal("126.2500").compareTo(result.getTotalAmount()));
    }

    @Test
    void getDividendsBySymbol_Success() {
        // Arrange
        Dividend div1 = new Dividend();
        div1.setAsset(ownedAsset);
        div1.setPaymentDate(LocalDate.of(2026, 1, 15));
        
        Dividend div2 = new Dividend();
        div2.setAsset(ownedAsset);
        div2.setPaymentDate(LocalDate.of(2025, 10, 15));

        when(dividendRepository.findBySymbol("AAPL"))
            .thenReturn(Arrays.asList(div1, div2));

        // Act
        List<Dividend> result = dividendService.getDividendsBySymbol("AAPL");

        // Assert
        assertEquals(2, result.size());
        assertEquals(div1, result.get(0));
        assertEquals(div2, result.get(1));
    }

    @Test
    void getDividendsByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        
        Dividend div1 = new Dividend();
        div1.setPaymentDate(LocalDate.of(2025, 6, 15));
        
        when(dividendRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(startDate, endDate))
            .thenReturn(Arrays.asList(div1));

        // Act
        List<Dividend> result = dividendService.getDividendsByDateRange(startDate, endDate);

        // Assert
        assertEquals(1, result.size());
        assertEquals(div1, result.get(0));
    }

    @Test
    void getTotalDividendIncome_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        BigDecimal expectedTotal = new BigDecimal("1250.75");
        
        when(dividendRepository.calculateTotalDividendIncome(startDate, endDate))
            .thenReturn(expectedTotal);

        // Act
        BigDecimal result = dividendService.getTotalDividendIncome(startDate, endDate);

        // Assert
        assertEquals(expectedTotal, result);
    }

    @Test
    void getTotalDividendIncome_NoData_ReturnsZero() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        
        when(dividendRepository.calculateTotalDividendIncome(startDate, endDate))
            .thenReturn(null);

        // Act
        BigDecimal result = dividendService.getTotalDividendIncome(startDate, endDate);

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void recordDividend_ZeroShares_ThrowsException() {
        // Arrange
        ownedAsset.setQuantity(BigDecimal.ZERO);
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            dividendService.recordDividend("AAPL", LocalDate.now(), new BigDecimal("0.95"))
        );
    }

    @Test
    void getAllDividends_Success() {
        // Arrange
        Dividend div1 = new Dividend();
        Dividend div2 = new Dividend();
        
        when(dividendRepository.findAllByOrderByPaymentDateDesc())
            .thenReturn(Arrays.asList(div1, div2));

        // Act
        List<Dividend> result = dividendService.getAllDividends();

        // Assert
        assertEquals(2, result.size());
        verify(dividendRepository).findAllByOrderByPaymentDateDesc();
    }
}
