package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.ESGRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ESGRating entity
 */
@Repository
public interface ESGRatingRepository extends JpaRepository<ESGRating, Long> {

    /**
     * Find ESG rating by symbol
     */
    Optional<ESGRating> findBySymbol(String symbol);

    /**
     * Find all ESG ratings with score above threshold
     */
    @Query("SELECT e FROM ESGRating e WHERE e.totalScore >= :minScore ORDER BY e.totalScore DESC")
    List<ESGRating> findByTotalScoreGreaterThanEqual(@Param("minScore") BigDecimal minScore);

    /**
     * Find ESG ratings by grade
     */
    List<ESGRating> findByTotalGrade(String grade);

    /**
     * Find ESG ratings by controversy level
     */
    List<ESGRating> findByControversyLevel(Integer controversyLevel);

    /**
     * Find ESG ratings by risk level
     */
    List<ESGRating> findByRiskLevel(String riskLevel);

    /**
     * Get top ESG performers
     */
    @Query("SELECT e FROM ESGRating e WHERE e.totalScore IS NOT NULL ORDER BY e.totalScore DESC")
    List<ESGRating> findTopPerformers();

    /**
     * Get ESG ratings with high environment scores
     */
    @Query("SELECT e FROM ESGRating e WHERE e.environmentScore >= :minScore ORDER BY e.environmentScore DESC")
    List<ESGRating> findByHighEnvironmentScore(@Param("minScore") BigDecimal minScore);

    /**
     * Get ESG ratings with high social scores
     */
    @Query("SELECT e FROM ESGRating e WHERE e.socialScore >= :minScore ORDER BY e.socialScore DESC")
    List<ESGRating> findByHighSocialScore(@Param("minScore") BigDecimal minScore);

    /**
     * Get ESG ratings with high governance scores
     */
    @Query("SELECT e FROM ESGRating e WHERE e.governanceScore >= :minScore ORDER BY e.governanceScore DESC")
    List<ESGRating> findByHighGovernanceScore(@Param("minScore") BigDecimal minScore);

    /**
     * Find ESG ratings with low controversy (0-2)
     */
    @Query("SELECT e FROM ESGRating e WHERE e.controversyLevel IS NOT NULL AND e.controversyLevel <= 2 ORDER BY e.totalScore DESC")
    List<ESGRating> findLowControversyRatings();

    /**
     * Check if ESG rating exists for symbol
     */
    boolean existsBySymbol(String symbol);

    /**
     * Count ESG ratings by grade
     */
    @Query("SELECT e.totalGrade, COUNT(e) FROM ESGRating e WHERE e.totalGrade IS NOT NULL GROUP BY e.totalGrade")
    List<Object[]> countByGrade();

    /**
     * Get average ESG scores
     */
    @Query("SELECT AVG(e.totalScore), AVG(e.environmentScore), AVG(e.socialScore), AVG(e.governanceScore) FROM ESGRating e WHERE e.totalScore IS NOT NULL")
    Object[] getAverageScores();
}
