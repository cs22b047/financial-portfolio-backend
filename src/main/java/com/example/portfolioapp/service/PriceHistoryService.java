package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.PriceHistory;
import com.example.portfolioapp.repository.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for PriceHistory operations
 */
@Service
public class PriceHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PriceHistoryService.class);

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    /**
     * Get all price history for a symbol
     */
    public List<PriceHistory> getBySymbol(String symbol) {
        logger.info("Fetching price history for symbol: {}", symbol);
        return priceHistoryRepository.findBySymbol(symbol);
    }

    /**
     * Get price history for a symbol within a date range
     */
    public List<PriceHistory> getBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching price history for symbol: {} between {} and {}", symbol, startDate, endDate);
        return priceHistoryRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    /**
     * Get all price history for a market data ID
     */
    public List<PriceHistory> getByMarketDataId(Long marketDataId) {
        logger.info("Fetching price history for market data ID: {}", marketDataId);
        return priceHistoryRepository.findByMarketDataId(marketDataId);
    }

    /**
     * Get price history for a market data ID within a date range
     */
    public List<PriceHistory> getByMarketDataIdAndDateRange(Long marketDataId, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching price history for market data ID: {} between {} and {}", marketDataId, startDate, endDate);
        return priceHistoryRepository.findByMarketDataIdAndDateRange(marketDataId, startDate, endDate);
    }

    /**
     * Get latest price history entry for a market data ID
     */
    public PriceHistory getLatestByMarketDataId(Long marketDataId) {
        logger.info("Fetching latest price history for market data ID: {}", marketDataId);
        return priceHistoryRepository.findLatestByMarketDataId(marketDataId)
                .orElse(null);
    }
}
