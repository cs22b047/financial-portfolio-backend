package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.AssetType;
import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AssetTypeRepository;
import com.example.portfolioapp.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    @Mock
    private MarketDataRepository marketDataRepository;

    @Mock
    private AssetTypeRepository assetTypeRepository;

    @InjectMocks
    private MarketDataService marketDataService;

    private MarketData testMarketData;
    private AssetType stockType;

    @BeforeEach
    void setUp() {
        stockType = new AssetType();
        stockType.setId(1L);
        stockType.setCode("STOCK");
        stockType.setName("Stock");

        testMarketData = new MarketData();
        testMarketData.setId(1L);
        testMarketData.setSymbol("AAPL");
        testMarketData.setName("Apple Inc.");
        testMarketData.setAssetType(stockType);
        testMarketData.setCurrentPrice(new BigDecimal("175.50"));
        testMarketData.setCurrency("USD");
        testMarketData.setSector("Technology");
    }

    @Test
    void createOrUpdateMarketData_NewMarketData_CreatesSuccessfully() {
        // Arrange
        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());
        when(marketDataRepository.save(any(MarketData.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        MarketData result = marketDataService.createOrUpdateMarketData(testMarketData);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        verify(marketDataRepository).save(any(MarketData.class));
    }

    @Test
    void createOrUpdateMarketData_ExistingMarketData_UpdatesSuccessfully() {
        // Arrange
        MarketData existing = new MarketData();
        existing.setId(1L);
        existing.setSymbol("AAPL");
        existing.setCurrentPrice(new BigDecimal("170.00"));

        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(existing));
        when(marketDataRepository.save(any(MarketData.class))).thenAnswer(i -> i.getArguments()[0]);

        testMarketData.setCurrentPrice(new BigDecimal("175.50"));

        // Act
        MarketData result = marketDataService.createOrUpdateMarketData(testMarketData);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("175.50"), result.getCurrentPrice());
        verify(marketDataRepository).save(existing);
    }

    @Test
    void getBySymbol_ValidSymbol_ReturnsMarketData() {
        // Arrange
        when(marketDataRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testMarketData));

        // Act
        MarketData result = marketDataService.getBySymbol("AAPL");

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals("Apple Inc.", result.getName());
    }

    @Test
    void getBySymbol_InvalidSymbol_ThrowsResourceNotFoundException() {
        // Arrange
        when(marketDataRepository.findBySymbol("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            marketDataService.getBySymbol("INVALID")
        );
    }

    @Test
    void getAllMarketData_Success_ReturnsAllData() {
        // Arrange
        MarketData data2 = new MarketData();
        data2.setSymbol("MSFT");
        when(marketDataRepository.findAll()).thenReturn(Arrays.asList(testMarketData, data2));

        // Act
        List<MarketData> result = marketDataService.getAllMarketData();

        // Assert
        assertEquals(2, result.size());
        verify(marketDataRepository).findAll();
    }
}
