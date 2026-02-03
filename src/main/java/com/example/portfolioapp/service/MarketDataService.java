package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.MarketDataRepository;
import com.example.portfolioapp.repository.AssetTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for MarketData operations (master stock data)
 */
@Service
public class MarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);

    @Autowired
    private MarketDataRepository marketDataRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    /**
     * Create or update market data for a symbol
     */
    @Transactional
    public MarketData createOrUpdateMarketData(MarketData marketData) {
        logger.info("Creating/updating market data for symbol: {}", marketData.getSymbol());

        MarketData existing = marketDataRepository.findBySymbol(marketData.getSymbol()).orElse(null);
        
        if (existing != null) {
            // Update existing
            updateMarketDataFields(existing, marketData);
            return marketDataRepository.save(existing);
        } else {
            // Create new
            return marketDataRepository.save(marketData);
        }
    }

    private void updateMarketDataFields(MarketData existing, MarketData updated) {
        existing.setCurrentPrice(updated.getCurrentPrice());
        existing.setPreviousClose(updated.getPreviousClose());
        existing.setDayChange(updated.getDayChange());
        existing.setDayChangePercent(updated.getDayChangePercent());
        existing.setVolume(updated.getVolume());
        existing.setDayHigh(updated.getDayHigh());
        existing.setDayLow(updated.getDayLow());
        existing.setWeek52High(updated.getWeek52High());
        existing.setWeek52Low(updated.getWeek52Low());
        existing.setMarketCap(updated.getMarketCap());
        existing.setBidPrice(updated.getBidPrice());
        existing.setAskPrice(updated.getAskPrice());
        existing.setSector(updated.getSector());
        existing.setIndustry(updated.getIndustry());
        existing.setExchange(updated.getExchange());
        existing.setDividendYield(updated.getDividendYield());
        existing.setPeRatio(updated.getPeRatio());
        existing.setBeta(updated.getBeta());
        existing.setEps(updated.getEps());
        existing.setDataSource(updated.getDataSource());
        existing.setMarketStatus(updated.getMarketStatus());
    }

    /**
     * Get market data by symbol
     */
    public MarketData getBySymbol(String symbol) {
        return marketDataRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("MarketData", "symbol", symbol));
    }

    /**
     * Search stocks by name or symbol
     */
    public List<MarketData> searchStocks(String query) {
        return marketDataRepository.searchBySymbolOrName(query);
    }

    /**
     * Get stocks by sector
     */
    public List<MarketData> getStocksBySector(String sector) {
        return marketDataRepository.findBySector(sector);
    }

    /**
     * Get top gainers
     */
    public List<MarketData> getTopGainers(int limit) {
        List<MarketData> gainers = marketDataRepository.findTopGainers();
        return gainers.stream().limit(limit).toList();
    }

    /**
     * Get top losers
     */
    public List<MarketData> getTopLosers(int limit) {
        List<MarketData> losers = marketDataRepository.findTopLosers();
        return losers.stream().limit(limit).toList();
    }

    /**
     * Get all market data
     */
    public List<MarketData> getAllMarketData() {
        return marketDataRepository.findAll();
    }

    /**
     * Get market data by asset type
     */
    public List<MarketData> getByAssetType(Long assetTypeId) {
        // Validate that asset type exists
        if (!assetTypeRepository.existsById(assetTypeId)) {
            throw new ResourceNotFoundException("AssetType", "id", assetTypeId);
        }
        
        logger.info("Fetching market data for asset type ID: {}", assetTypeId);
        return marketDataRepository.findByAssetTypeId(assetTypeId);
    }

    /**
     * Delete market data
     */
    @Transactional
    public void deleteMarketData(Long id) {
        MarketData marketData = marketDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarketData", id));
        marketDataRepository.delete(marketData);
        logger.info("Deleted market data for symbol: {}", marketData.getSymbol());
    }
}
