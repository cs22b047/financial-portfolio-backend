package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.StockSummary;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.StockSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for StockSummary operations
 */
@Service
public class StockSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(StockSummaryService.class);

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    /**
     * Create or update stock summary
     */
    @Transactional
    public StockSummary createOrUpdateStockSummary(StockSummary summary) {
        logger.info("Creating/updating stock summary for market data ID: {}, period: {}",
                    summary.getMarketData().getId(), summary.getPeriod());

        Optional<StockSummary> existing =
            stockSummaryRepository.findByMarketDataIdAndDateRangeAndPeriod(
                summary.getMarketData().getId(),
                summary.getStartDate(),
                summary.getEndDate(),
                summary.getPeriod()
            );

        if (existing.isPresent()) {
            // Update existing
            StockSummary existingSummary = existing.get();
            updateStockSummaryFields(existingSummary, summary);
            return stockSummaryRepository.save(existingSummary);
        } else {
            // Create new
            return stockSummaryRepository.save(summary);
        }
    }

    private void updateStockSummaryFields(StockSummary existing, StockSummary updated) {
        existing.setTradingDays(updated.getTradingDays());
        existing.setStartPrice(updated.getStartPrice());
        existing.setEndPrice(updated.getEndPrice());
        existing.setMinPrice(updated.getMinPrice());
        existing.setMaxPrice(updated.getMaxPrice());
        existing.setAvgPrice(updated.getAvgPrice());
        existing.setTotalReturn(updated.getTotalReturn());
        existing.setAvgDailyReturn(updated.getAvgDailyReturn());
        existing.setAnnualizedReturn(updated.getAnnualizedReturn());
        existing.setDailyVolatility(updated.getDailyVolatility());
        existing.setAnnualizedVolatility(updated.getAnnualizedVolatility());
        existing.setSharpeRatio(updated.getSharpeRatio());
        existing.setMaxDailyGain(updated.getMaxDailyGain());
        existing.setMaxDailyLoss(updated.getMaxDailyLoss());
        existing.setAvgVolume(updated.getAvgVolume());
        existing.setMaxVolume(updated.getMaxVolume());
        existing.setMinVolume(updated.getMinVolume());
        existing.setVar95(updated.getVar95());
        existing.setVar99(updated.getVar99());
        existing.setMaxDrawdown(updated.getMaxDrawdown());
    }

    /**
     * Get stock summaries by market data ID
     */
    public List<StockSummary> getByMarketDataId(Long marketDataId) {
        return stockSummaryRepository.findByMarketDataId(marketDataId);
    }

    /**
     * Get stock summaries by symbol
     */
    public List<StockSummary> getBySymbol(String symbol) {
        return stockSummaryRepository.findBySymbol(symbol);
    }

    /**
     * Get stock summary by market data ID and period
     */
    public StockSummary getByMarketDataIdAndPeriod(Long marketDataId, String period) {
        return stockSummaryRepository.findByMarketDataIdAndPeriod(marketDataId, period)
                .orElseThrow(() -> new ResourceNotFoundException("StockSummary not found for marketDataId: " + marketDataId + " and period: " + period));
    }

    /**
     * Get stock summary by symbol and period
     */
    public StockSummary getBySymbolAndPeriod(String symbol, String period) {
        return stockSummaryRepository.findBySymbolAndPeriod(symbol, period)
                .orElseThrow(() -> new ResourceNotFoundException("StockSummary not found for symbol: " + symbol + " and period: " + period));
    }

    /**
     * Get top performers by period
     */
    public List<StockSummary> getTopPerformersByPeriod(String period, int limit) {
        List<StockSummary> performers = stockSummaryRepository.findTopPerformersByPeriod(period);
        return performers.stream().limit(limit).toList();
    }

    /**
     * Get best Sharpe ratios by period
     */
    public List<StockSummary> getBestSharpeRatioByPeriod(String period, int limit) {
        List<StockSummary> summaries = stockSummaryRepository.findBestSharpeRatioByPeriod(period);
        return summaries.stream().limit(limit).toList();
    }

    /**
     * Get lowest volatility stocks by period
     */
    public List<StockSummary> getLowestVolatilityByPeriod(String period, int limit) {
        List<StockSummary> summaries = stockSummaryRepository.findLowestVolatilityByPeriod(period);
        return summaries.stream().limit(limit).toList();
    }

    /**
     * Get highest volatility stocks by period
     */
    public List<StockSummary> getHighestVolatilityByPeriod(String period, int limit) {
        List<StockSummary> summaries = stockSummaryRepository.findHighestVolatilityByPeriod(period);
        return summaries.stream().limit(limit).toList();
    }

    /**
     * Delete stock summary
     */
    @Transactional
    public void deleteStockSummary(Long id) {
        StockSummary summary = stockSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockSummary", id));
        stockSummaryRepository.delete(summary);
        logger.info("Deleted stock summary with ID: {}", id);
    }

    /**
     * Batch create stock summaries
     */
    @Transactional
    public List<StockSummary> batchCreateStockSummaries(List<StockSummary> summaries) {
        logger.info("Batch creating {} stock summaries", summaries.size());
        return stockSummaryRepository.saveAll(summaries);
    }
}
