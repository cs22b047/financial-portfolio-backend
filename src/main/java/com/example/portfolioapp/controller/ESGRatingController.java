package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.ESGRating;
import com.example.portfolioapp.service.ESGRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for ESG (Environmental, Social, Governance) ratings
 */
@RestController
@RequestMapping("/api/esg-ratings")
@CrossOrigin(origins = "*")
public class ESGRatingController {

    @Autowired
    private ESGRatingService esgRatingService;

    /**
     * Get all ESG ratings
     * GET /api/esg-ratings
     */
    @GetMapping
    public ResponseEntity<List<ESGRating>> getAllESGRatings() {
        List<ESGRating> ratings = esgRatingService.getAllESGRatings();
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ESG rating by ID
     * GET /api/esg-ratings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ESGRating> getESGRatingById(@PathVariable Long id) {
        return esgRatingService.getESGRatingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get ESG rating by symbol
     * GET /api/esg-ratings/symbol/{symbol}
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<ESGRating> getESGRatingBySymbol(@PathVariable String symbol) {
        ESGRating rating = esgRatingService.getESGRatingBySymbol(symbol);
        return ResponseEntity.ok(rating);
    }

    /**
     * Get top ESG performers
     * GET /api/esg-ratings/top-performers?limit=10
     */
    @GetMapping("/top-performers")
    public ResponseEntity<List<ESGRating>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        List<ESGRating> performers = esgRatingService.getTopESGPerformers(limit);
        return ResponseEntity.ok(performers);
    }

    /**
     * Get ESG ratings by grade
     * GET /api/esg-ratings/grade/{grade}
     */
    @GetMapping("/grade/{grade}")
    public ResponseEntity<List<ESGRating>> getByGrade(@PathVariable String grade) {
        List<ESGRating> ratings = esgRatingService.getESGRatingsByGrade(grade);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ESG ratings above minimum score
     * GET /api/esg-ratings/above-score?minScore=70
     */
    @GetMapping("/above-score")
    public ResponseEntity<List<ESGRating>> getAboveScore(
            @RequestParam BigDecimal minScore) {
        List<ESGRating> ratings = esgRatingService.getESGRatingsAboveScore(minScore);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ESG ratings by controversy level
     * GET /api/esg-ratings/controversy/{level}
     */
    @GetMapping("/controversy/{level}")
    public ResponseEntity<List<ESGRating>> getByControversy(@PathVariable Integer level) {
        List<ESGRating> ratings = esgRatingService.getESGRatingsByControversy(level);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get low controversy ESG ratings (0-2)
     * GET /api/esg-ratings/low-controversy
     */
    @GetMapping("/low-controversy")
    public ResponseEntity<List<ESGRating>> getLowControversy() {
        List<ESGRating> ratings = esgRatingService.getLowControversyRatings();
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ESG ratings by risk level
     * GET /api/esg-ratings/risk/{riskLevel}
     */
    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<List<ESGRating>> getByRiskLevel(@PathVariable String riskLevel) {
        List<ESGRating> ratings = esgRatingService.getESGRatingsByRiskLevel(riskLevel);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get high environment score ratings
     * GET /api/esg-ratings/high-environment?minScore=75
     */
    @GetMapping("/high-environment")
    public ResponseEntity<List<ESGRating>> getHighEnvironmentScores(
            @RequestParam(defaultValue = "75") BigDecimal minScore) {
        List<ESGRating> ratings = esgRatingService.getHighEnvironmentScores(minScore);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get high social score ratings
     * GET /api/esg-ratings/high-social?minScore=75
     */
    @GetMapping("/high-social")
    public ResponseEntity<List<ESGRating>> getHighSocialScores(
            @RequestParam(defaultValue = "75") BigDecimal minScore) {
        List<ESGRating> ratings = esgRatingService.getHighSocialScores(minScore);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get high governance score ratings
     * GET /api/esg-ratings/high-governance?minScore=75
     */
    @GetMapping("/high-governance")
    public ResponseEntity<List<ESGRating>> getHighGovernanceScores(
            @RequestParam(defaultValue = "75") BigDecimal minScore) {
        List<ESGRating> ratings = esgRatingService.getHighGovernanceScores(minScore);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get ESG statistics (averages, grade distribution, etc.)
     * GET /api/esg-ratings/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getESGStatistics() {
        Map<String, Object> stats = esgRatingService.getESGStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Create new ESG rating
     * POST /api/esg-ratings
     */
    @PostMapping
    public ResponseEntity<ESGRating> createESGRating(@RequestBody ESGRating esgRating) {
        ESGRating created = esgRatingService.createOrUpdateESGRating(esgRating);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update ESG rating by symbol
     * PUT /api/esg-ratings/symbol/{symbol}
     */
    @PutMapping("/symbol/{symbol}")
    public ResponseEntity<ESGRating> updateESGRating(
            @PathVariable String symbol,
            @RequestBody ESGRating updates) {
        ESGRating updated = esgRatingService.updateESGRatingForSymbol(symbol, updates);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete ESG rating by ID
     * DELETE /api/esg-ratings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteESGRating(@PathVariable Long id) {
        esgRatingService.deleteESGRating(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "ESG rating deleted successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete ESG rating by symbol
     * DELETE /api/esg-ratings/symbol/{symbol}
     */
    @DeleteMapping("/symbol/{symbol}")
    public ResponseEntity<Map<String, String>> deleteESGRatingBySymbol(@PathVariable String symbol) {
        esgRatingService.deleteESGRatingBySymbol(symbol);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "ESG rating deleted successfully");
        response.put("symbol", symbol);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Check if ESG rating exists for symbol
     * GET /api/esg-ratings/exists/{symbol}
     */
    @GetMapping("/exists/{symbol}")
    public ResponseEntity<Map<String, Boolean>> checkExists(@PathVariable String symbol) {
        boolean exists = esgRatingService.existsBySymbol(symbol);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
}
