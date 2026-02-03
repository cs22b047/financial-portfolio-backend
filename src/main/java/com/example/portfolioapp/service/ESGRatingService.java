package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.ESGRating;
import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.exception.ESGRatingNotFoundException;
import com.example.portfolioapp.repository.ESGRatingRepository;
import com.example.portfolioapp.repository.MarketDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for ESG rating operations
 */
@Service
@Transactional
public class ESGRatingService {

    @Autowired
    private ESGRatingRepository esgRatingRepository;

    @Autowired
    private MarketDataRepository marketDataRepository;

    /**
     * Get all ESG ratings
     */
    public List<ESGRating> getAllESGRatings() {
        return esgRatingRepository.findAll();
    }

    /**
     * Get ESG rating by ID
     */
    public Optional<ESGRating> getESGRatingById(Long id) {
        return esgRatingRepository.findById(id);
    }

    /**
     * Get ESG rating by symbol
     */
    public ESGRating getESGRatingBySymbol(String symbol) {
        return esgRatingRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ESGRatingNotFoundException("symbol", symbol));
    }

    /**
     * Create or update ESG rating
     */
    public ESGRating createOrUpdateESGRating(ESGRating esgRating) {
        // If symbol provided, check if rating exists
        if (esgRating.getSymbol() != null) {
            Optional<ESGRating> existing = esgRatingRepository.findBySymbol(esgRating.getSymbol());
            if (existing.isPresent()) {
                ESGRating existingRating = existing.get();
                updateESGRatingFields(existingRating, esgRating);
                return esgRatingRepository.save(existingRating);
            }
        }
        
        return esgRatingRepository.save(esgRating);
    }

    /**
     * Update ESG rating for a symbol
     */
    public ESGRating updateESGRatingForSymbol(String symbol, ESGRating updates) {
        ESGRating existing = esgRatingRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ESGRatingNotFoundException("symbol", symbol));
        
        updateESGRatingFields(existing, updates);
        return esgRatingRepository.save(existing);
    }

    /**
     * Get top ESG performers
     */
    public List<ESGRating> getTopESGPerformers(int limit) {
        List<ESGRating> allPerformers = esgRatingRepository.findTopPerformers();
        return allPerformers.stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get ESG ratings by grade
     */
    public List<ESGRating> getESGRatingsByGrade(String grade) {
        return esgRatingRepository.findByTotalGrade(grade);
    }

    /**
     * Get ESG ratings with minimum score
     */
    public List<ESGRating> getESGRatingsAboveScore(BigDecimal minScore) {
        return esgRatingRepository.findByTotalScoreGreaterThanEqual(minScore);
    }

    /**
     * Get ESG ratings by controversy level
     */
    public List<ESGRating> getESGRatingsByControversy(Integer controversyLevel) {
        return esgRatingRepository.findByControversyLevel(controversyLevel);
    }

    /**
     * Get low controversy ESG ratings
     */
    public List<ESGRating> getLowControversyRatings() {
        return esgRatingRepository.findLowControversyRatings();
    }

    /**
     * Get ESG ratings by risk level
     */
    public List<ESGRating> getESGRatingsByRiskLevel(String riskLevel) {
        return esgRatingRepository.findByRiskLevel(riskLevel);
    }

    /**
     * Get high environment score ratings
     */
    public List<ESGRating> getHighEnvironmentScores(BigDecimal minScore) {
        return esgRatingRepository.findByHighEnvironmentScore(minScore);
    }

    /**
     * Get high social score ratings
     */
    public List<ESGRating> getHighSocialScores(BigDecimal minScore) {
        return esgRatingRepository.findByHighSocialScore(minScore);
    }

    /**
     * Get high governance score ratings
     */
    public List<ESGRating> getHighGovernanceScores(BigDecimal minScore) {
        return esgRatingRepository.findByHighGovernanceScore(minScore);
    }

    /**
     * Get ESG statistics
     */
    public Map<String, Object> getESGStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total count
        long totalCount = esgRatingRepository.count();
        stats.put("totalRatings", totalCount);
        
        // Average scores
        Object[] avgScores = esgRatingRepository.getAverageScores();
        if (avgScores != null && avgScores.length == 4) {
            stats.put("averageTotalScore", avgScores[0]);
            stats.put("averageEnvironmentScore", avgScores[1]);
            stats.put("averageSocialScore", avgScores[2]);
            stats.put("averageGovernanceScore", avgScores[3]);
        }
        
        // Grade distribution
        List<Object[]> gradeDistribution = esgRatingRepository.countByGrade();
        Map<String, Long> gradeMap = new HashMap<>();
        for (Object[] row : gradeDistribution) {
            gradeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("gradeDistribution", gradeMap);
        
        // Low controversy count
        long lowControversyCount = esgRatingRepository.findLowControversyRatings().size();
        stats.put("lowControversyCount", lowControversyCount);
        
        return stats;
    }

    /**
     * Delete ESG rating
     */
    public void deleteESGRating(Long id) {
        if (!esgRatingRepository.existsById(id)) {
            throw new ESGRatingNotFoundException(id);
        }
        esgRatingRepository.deleteById(id);
    }

    /**
     * Delete ESG rating by symbol
     */
    public void deleteESGRatingBySymbol(String symbol) {
        ESGRating rating = esgRatingRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ESGRatingNotFoundException("symbol", symbol));
        esgRatingRepository.delete(rating);
    }

    /**
     * Check if ESG rating exists for symbol
     */
    public boolean existsBySymbol(String symbol) {
        return esgRatingRepository.existsBySymbol(symbol);
    }

    // Helper method to update fields
    private void updateESGRatingFields(ESGRating existing, ESGRating updates) {
        if (updates.getTotalScore() != null) {
            existing.setTotalScore(updates.getTotalScore());
        }
        if (updates.getTotalGrade() != null) {
            existing.setTotalGrade(updates.getTotalGrade());
        }
        if (updates.getEnvironmentScore() != null) {
            existing.setEnvironmentScore(updates.getEnvironmentScore());
        }
        if (updates.getEnvironmentGrade() != null) {
            existing.setEnvironmentGrade(updates.getEnvironmentGrade());
        }
        if (updates.getSocialScore() != null) {
            existing.setSocialScore(updates.getSocialScore());
        }
        if (updates.getSocialGrade() != null) {
            existing.setSocialGrade(updates.getSocialGrade());
        }
        if (updates.getGovernanceScore() != null) {
            existing.setGovernanceScore(updates.getGovernanceScore());
        }
        if (updates.getGovernanceGrade() != null) {
            existing.setGovernanceGrade(updates.getGovernanceGrade());
        }
        if (updates.getControversyLevel() != null) {
            existing.setControversyLevel(updates.getControversyLevel());
        }
        if (updates.getRiskLevel() != null) {
            existing.setRiskLevel(updates.getRiskLevel());
        }
        if (updates.getDataSource() != null) {
            existing.setDataSource(updates.getDataSource());
        }
    }
}
