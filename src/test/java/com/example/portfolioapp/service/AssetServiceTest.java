package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.exception.DuplicateResourceException;
import com.example.portfolioapp.exception.InvalidOperationException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AssetRepository;
import com.example.portfolioapp.repository.MarketDataRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private MarketDataRepository marketDataRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AssetService assetService;

    private MarketData appleStock;
    private AssetType stockType;

    @BeforeEach
    void setUp() {
        stockType = new AssetType();
        stockType.setId(1L);
        stockType.setCode("STOCK");
        stockType.setName("STOCK");

        appleStock = new MarketData();
        appleStock.setId(1L);
        appleStock.setSymbol("AAPL");
        appleStock.setName("Apple Inc.");
        appleStock.setAssetType(stockType);
        appleStock.setCurrentPrice(new BigDecimal("175.50"));
        appleStock.setCurrency("USD");
    }

    @Test
    void buyStock_Success_CreatesNewAsset() {
        // Arrange
        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(appleStock));
        when(assetRepository.findByMarketDataIdAndStatus(1L, AssetStatus.OWNED)).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Asset result = assetService.buyStock("AAPL", new BigDecimal("10"), new BigDecimal("175.50"), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10"), result.getQuantity());
        assertEquals(AssetStatus.OWNED, result.getStatus());
        assertEquals(appleStock, result.getMarketData());
        
        verify(assetRepository).save(any(Asset.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void buyStock_Success_UpdatesExistingAsset() {
        // Arrange
        Asset existingAsset = new Asset();
        existingAsset.setId(1L);
        existingAsset.setMarketData(appleStock);
        existingAsset.setQuantity(new BigDecimal("5"));
        existingAsset.setAverageCost(new BigDecimal("170.00"));
        existingAsset.setStatus(AssetStatus.OWNED);

        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(appleStock));
        when(assetRepository.findByMarketDataIdAndStatus(1L, AssetStatus.OWNED)).thenReturn(Optional.of(existingAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Asset result = assetService.buyStock("AAPL", new BigDecimal("10"), new BigDecimal("175.50"), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("15"), result.getQuantity()); // 5 + 10
        assertTrue(result.getAverageCost().compareTo(new BigDecimal("170.00")) > 0); // New weighted average
        
        verify(assetRepository).save(existingAsset);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void buyStock_StockNotFound_ThrowsException() {
        // Arrange
        when(marketDataRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            assetService.buyStock("INVALID", new BigDecimal("10"), new BigDecimal("100"), LocalDate.now())
        );
        
        verify(assetRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buyStock_InvalidQuantity_ThrowsException() {
        // Arrange - no mocking needed, service should validate before repository calls

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            assetService.buyStock("AAPL", new BigDecimal("0"), new BigDecimal("175.50"), LocalDate.now())
        );
        
        assertThrows(InvalidOperationException.class, () -> 
            assetService.buyStock("AAPL", new BigDecimal("-5"), new BigDecimal("175.50"), LocalDate.now())
        );
    }

    @Test
    void sellStock_Success_PartialSale() {
        // Arrange
        Asset ownedAsset = new Asset();
        ownedAsset.setId(1L);
        ownedAsset.setMarketData(appleStock);
        ownedAsset.setQuantity(new BigDecimal("20"));
        ownedAsset.setAverageCost(new BigDecimal("170.00"));
        ownedAsset.setStatus(AssetStatus.OWNED);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Asset result = assetService.sellStock("AAPL", new BigDecimal("10"), new BigDecimal("180.00"), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10"), result.getQuantity()); // 20 - 10
        assertEquals(AssetStatus.OWNED, result.getStatus());
        
        verify(assetRepository).save(ownedAsset);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void sellStock_Success_CompleteSale() {
        // Arrange
        Asset ownedAsset = new Asset();
        ownedAsset.setId(1L);
        ownedAsset.setMarketData(appleStock);
        ownedAsset.setQuantity(new BigDecimal("10"));
        ownedAsset.setAverageCost(new BigDecimal("170.00"));
        ownedAsset.setStatus(AssetStatus.OWNED);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assetService.sellStock("AAPL", new BigDecimal("10"), new BigDecimal("180.00"), LocalDate.now());

        // Assert
        verify(assetRepository).delete(ownedAsset);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void sellStock_AssetNotFound_ThrowsException() {
        // Arrange
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            assetService.sellStock("AAPL", new BigDecimal("10"), new BigDecimal("180.00"), LocalDate.now())
        );
    }

    @Test
    void sellStock_InsufficientShares_ThrowsException() {
        // Arrange
        Asset ownedAsset = new Asset();
        ownedAsset.setQuantity(new BigDecimal("5"));
        ownedAsset.setStatus(AssetStatus.OWNED);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            assetService.sellStock("AAPL", new BigDecimal("10"), new BigDecimal("180.00"), LocalDate.now())
        );
    }

    @Test
    void sellStock_WatchlistAsset_ThrowsException() {
        // Arrange
        Asset watchlistAsset = new Asset();
        watchlistAsset.setMarketData(appleStock);
        watchlistAsset.setQuantity(BigDecimal.ZERO);
        watchlistAsset.setStatus(AssetStatus.WATCHLIST);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(watchlistAsset));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            assetService.sellStock("AAPL", new BigDecimal("10"), new BigDecimal("180.00"), LocalDate.now())
        );
    }

    @Test
    void addToWatchlist_Success() {
        // Arrange
        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(appleStock));
        when(assetRepository.findByMarketDataIdAndStatus(1L, AssetStatus.WATCHLIST)).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Asset result = assetService.addToWatchlist("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(AssetStatus.WATCHLIST, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getQuantity());
        assertEquals(BigDecimal.ZERO, result.getAverageCost());
        assertEquals(appleStock, result.getMarketData());
        
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void addToWatchlist_AlreadyExists_ThrowsException() {
        // Arrange
        Asset existingWatchlist = new Asset();
        existingWatchlist.setStatus(AssetStatus.WATCHLIST);

        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(appleStock));
        when(assetRepository.findByMarketDataIdAndStatus(1L, AssetStatus.WATCHLIST)).thenReturn(Optional.of(existingWatchlist));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> 
            assetService.addToWatchlist("AAPL")
        );
        
        verify(assetRepository, never()).save(any());
    }

    @Test
    void removeFromWatchlist_Success() {
        // Arrange
        Asset watchlistAsset = new Asset();
        watchlistAsset.setId(1L);
        watchlistAsset.setMarketData(appleStock);
        watchlistAsset.setStatus(AssetStatus.WATCHLIST);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(watchlistAsset));

        // Act
        assetService.removeFromWatchlist("AAPL");

        // Assert
        verify(assetRepository).delete(watchlistAsset);
    }

    @Test
    void removeFromWatchlist_NotFound_ThrowsException() {
        // Arrange
        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            assetService.removeFromWatchlist("AAPL")
        );
    }

    @Test
    void removeFromWatchlist_OwnedAsset_ThrowsException() {
        // Arrange
        Asset ownedAsset = new Asset();
        ownedAsset.setStatus(AssetStatus.OWNED);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(ownedAsset));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> 
            assetService.removeFromWatchlist("AAPL")
        );
    }

    @Test
    void getOwnedAssets_Success() {
        // Arrange
        Asset asset1 = new Asset();
        asset1.setMarketData(appleStock);
        asset1.setStatus(AssetStatus.OWNED);
        
        Asset asset2 = new Asset();
        asset2.setStatus(AssetStatus.OWNED);

        when(assetRepository.findAllOwned()).thenReturn(Arrays.asList(asset1, asset2));

        // Act
        List<Asset> result = assetService.getOwnedAssets();

        // Assert
        assertEquals(2, result.size());
        verify(assetRepository).findAllOwned();
    }

    @Test
    void getWatchlistAssets_Success() {
        // Arrange
        Asset asset1 = new Asset();
        asset1.setMarketData(appleStock);
        asset1.setStatus(AssetStatus.WATCHLIST);

        when(assetRepository.findAllWatchlist()).thenReturn(Arrays.asList(asset1));

        // Act
        List<Asset> result = assetService.getWatchlistAssets();

        // Assert
        assertEquals(1, result.size());
        assertEquals(AssetStatus.WATCHLIST, result.get(0).getStatus());
        verify(assetRepository).findAllWatchlist();
    }

    @Test
    void getAssetBySymbol_Success() {
        // Arrange
        Asset asset = new Asset();
        asset.setMarketData(appleStock);

        when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));

        // Act
        Asset result = assetService.getAssetBySymbol("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals(appleStock, result.getMarketData());
    }

    @Test
    void getAssetBySymbol_NotFound_ThrowsException() {
        // Arrange
        when(assetRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            assetService.getAssetBySymbol("INVALID")
        );
    }

    @Test
    void getTotalPortfolioValue_WithAssets_ReturnsCorrectTotal() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("50000.00");
        when(assetRepository.calculateTotalPortfolioValue()).thenReturn(expectedTotal);

        // Act
        BigDecimal result = assetService.getTotalPortfolioValue();

        // Assert
        assertEquals(expectedTotal, result);
        verify(assetRepository).calculateTotalPortfolioValue();
    }

    @Test
    void getTotalPortfolioValue_NoAssets_ReturnsZero() {
        // Arrange
        when(assetRepository.calculateTotalPortfolioValue()).thenReturn(null);

        // Act
        BigDecimal result = assetService.getTotalPortfolioValue();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getTotalInvestmentCost_ReturnsCorrectTotal() {
        // Arrange
        BigDecimal expectedCost = new BigDecimal("45000.00");
        when(assetRepository.calculateTotalInvestmentCost()).thenReturn(expectedCost);

        // Act
        BigDecimal result = assetService.getTotalInvestmentCost();

        // Assert
        assertEquals(expectedCost, result);
    }

    @Test
    void calculateUnrealizedGainLoss_ReturnsCorrectValue() {
        // Arrange
        when(assetRepository.calculateTotalPortfolioValue()).thenReturn(new BigDecimal("50000.00"));
        when(assetRepository.calculateTotalInvestmentCost()).thenReturn(new BigDecimal("45000.00"));

        // Act
        BigDecimal result = assetService.calculateUnrealizedGainLoss();

        // Assert
        assertEquals(new BigDecimal("5000.00"), result);
    }

    @Test
    void getById_ValidId_ReturnsAsset() {
        // Arrange
        Asset asset = new Asset();
        asset.setId(1L);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        // Act
        Asset result = assetService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_InvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            assetService.getById(999L)
        );
    }
}
