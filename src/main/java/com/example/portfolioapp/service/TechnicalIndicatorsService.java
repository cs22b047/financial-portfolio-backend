package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.TechnicalIndicators;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.TechnicalIndicatorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for TechnicalIndicators operations
 */
@Service
public class TechnicalIndicatorsService {

    private static final Logger logger = LoggerFactory.getLogger(TechnicalIndicatorsService.class);

    @Autowired
    private TechnicalIndicatorsRepository technicalIndicatorsRepository;

    /**
     * Create or update technical indicators
     */
    @Transactional
    public TechnicalIndicators createOrUpdateTechnicalIndicators(TechnicalIndicators indicators) {
        logger.info("Creating/updating technical indicators for price history ID: {}",
                    indicators.getPriceHistory().getId());

        Optional<TechnicalIndicators> existing =
            technicalIndicatorsRepository.findByPriceHistoryId(indicators.getPriceHistory().getId());

        if (existing.isPresent()) {
            // Update existing
            TechnicalIndicators existingIndicators = existing.get();
            updateTechnicalIndicatorsFields(existingIndicators, indicators);
            return technicalIndicatorsRepository.save(existingIndicators);
        } else {
            // Create new
            return technicalIndicatorsRepository.save(indicators);
        }
    }

    private void updateTechnicalIndicatorsFields(TechnicalIndicators existing, TechnicalIndicators updated) {
        existing.setIndicatorDate(updated.getIndicatorDate());
        existing.setDailyReturn(updated.getDailyReturn());
        existing.setCumulativeReturn(updated.getCumulativeReturn());
        existing.setLogReturn(updated.getLogReturn());
        existing.setSma20(updated.getSma20());
        existing.setSma50(updated.getSma50());
        existing.setSma200(updated.getSma200());
        existing.setEma12(updated.getEma12());
        existing.setEma26(updated.getEma26());
        existing.setMacd(updated.getMacd());
        existing.setMacdSignal(updated.getMacdSignal());
        existing.setMacdHistogram(updated.getMacdHistogram());
        existing.setBbMiddle(updated.getBbMiddle());
        existing.setBbUpper(updated.getBbUpper());
        existing.setBbLower(updated.getBbLower());
        existing.setBbWidth(updated.getBbWidth());
        existing.setRsi(updated.getRsi());
        existing.setAtr(updated.getAtr());
        existing.setVolumeSma20(updated.getVolumeSma20());
        existing.setVolumeRatio(updated.getVolumeRatio());
        existing.setMomentum10(updated.getMomentum10());
        existing.setRoc10(updated.getRoc10());
        existing.setStochasticK(updated.getStochasticK());
        existing.setStochasticD(updated.getStochasticD());
    }

    /**
     * Get technical indicators by price history ID
     */
    public TechnicalIndicators getByPriceHistoryId(Long priceHistoryId) {
        return technicalIndicatorsRepository.findByPriceHistoryId(priceHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("TechnicalIndicators", "priceHistoryId", priceHistoryId));
    }

    /**
     * Get all technical indicators for a market data ID
     */
    public List<TechnicalIndicators> getByMarketDataId(Long marketDataId) {
        return technicalIndicatorsRepository.findByMarketDataId(marketDataId);
    }

    /**
     * Get all technical indicators for a symbol
     */
    public List<TechnicalIndicators> getBySymbol(String symbol) {
        return technicalIndicatorsRepository.findBySymbol(symbol);
    }

    /**
     * Get technical indicators by symbol and date range
     */
    public List<TechnicalIndicators> getBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        return technicalIndicatorsRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    /**
     * Get technical indicators by market data ID and date range
     */
    public List<TechnicalIndicators> getByMarketDataIdAndDateRange(Long marketDataId, LocalDate startDate, LocalDate endDate) {
        return technicalIndicatorsRepository.findByMarketDataIdAndDateRange(marketDataId, startDate, endDate);
    }

    /**
     * Get latest technical indicators for a symbol
     */
    public TechnicalIndicators getLatestBySymbol(String symbol) {
        return technicalIndicatorsRepository.findLatestBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("TechnicalIndicators", "symbol", symbol));
    }

    /**
     * Get latest technical indicators for a market data ID
     */
    public TechnicalIndicators getLatestByMarketDataId(Long marketDataId) {
        return technicalIndicatorsRepository.findLatestByMarketDataId(marketDataId)
                .orElseThrow(() -> new ResourceNotFoundException("TechnicalIndicators", "marketDataId", marketDataId));
    }

    /**
     * Delete technical indicators
     */
    @Transactional
    public void deleteTechnicalIndicators(Long id) {
        TechnicalIndicators indicators = technicalIndicatorsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TechnicalIndicators", id));
        technicalIndicatorsRepository.delete(indicators);
        logger.info("Deleted technical indicators with ID: {}", id);
    }

    /**
     * Batch create technical indicators
     */
    @Transactional
    public List<TechnicalIndicators> batchCreateTechnicalIndicators(List<TechnicalIndicators> indicatorsList) {
        logger.info("Batch creating {} technical indicators", indicatorsList.size());
        return technicalIndicatorsRepository.saveAll(indicatorsList);
    }
}
