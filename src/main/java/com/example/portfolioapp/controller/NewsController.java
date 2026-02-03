package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.News;
import com.example.portfolioapp.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for News operations.
 */
@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * Get all news
     * GET /api/news
     */
    @GetMapping
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    /**
     * Get news by ID
     * GET /api/news/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    /**
     * Get news by symbol
     * GET /api/news/symbol/{symbol}
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<News>> getNewsBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(newsService.getNewsBySymbol(symbol));
    }

    /**
     * Get news by symbol with pagination
     * GET /api/news/symbol/{symbol}/paged?page=0&size=10
     */
    @GetMapping("/symbol/{symbol}/paged")
    public ResponseEntity<Page<News>> getNewsBySymbolPaged(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getNewsBySymbol(symbol, page, size));
    }

    /**
     * Get news by market data ID
     * GET /api/news/market-data/{marketDataId}
     */
    @GetMapping("/market-data/{marketDataId}")
    public ResponseEntity<List<News>> getNewsByMarketDataId(@PathVariable Long marketDataId) {
        return ResponseEntity.ok(newsService.getNewsByMarketDataId(marketDataId));
    }

    /**
     * Get news by sentiment
     * GET /api/news/sentiment/{sentiment}
     */
    @GetMapping("/sentiment/{sentiment}")
    public ResponseEntity<List<News>> getNewsBySentiment(@PathVariable String sentiment) {
        return ResponseEntity.ok(newsService.getNewsBySentiment(sentiment));
    }

    /**
     * Get news by source
     * GET /api/news/source/{source}
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<List<News>> getNewsBySource(@PathVariable String source) {
        return ResponseEntity.ok(newsService.getNewsBySource(source));
    }

    /**
     * Get unread news
     * GET /api/news/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<News>> getUnreadNews() {
        return ResponseEntity.ok(newsService.getUnreadNews());
    }

    /**
     * Get unread news for a symbol
     * GET /api/news/unread/{symbol}
     */
    @GetMapping("/unread/{symbol}")
    public ResponseEntity<List<News>> getUnreadNewsBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(newsService.getUnreadNewsBySymbol(symbol));
    }

    /**
     * Get recent news (last N days, default 7)
     * GET /api/news/recent?days=7
     */
    @GetMapping("/recent")
    public ResponseEntity<List<News>> getRecentNews(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(newsService.getRecentNews(days));
    }

    /**
     * Get recent news for a symbol
     * GET /api/news/recent/{symbol}?days=7
     */
    @GetMapping("/recent/{symbol}")
    public ResponseEntity<List<News>> getRecentNewsBySymbol(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(newsService.getRecentNewsBySymbol(symbol, days));
    }

    /**
     * Get news by date range
     * GET /api/news/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<News>> getNewsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(newsService.getNewsByDateRange(startDate, endDate));
    }

    /**
     * Search news by keyword
     * GET /api/news/search?keyword=earnings
     */
    @GetMapping("/search")
    public ResponseEntity<List<News>> searchNews(@RequestParam String keyword) {
        return ResponseEntity.ok(newsService.searchNews(keyword));
    }

    /**
     * Get top news (most recent, limited)
     * GET /api/news/top?limit=10
     */
    @GetMapping("/top")
    public ResponseEntity<List<News>> getTopNews(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(newsService.getTopNews(limit));
    }

    /**
     * Create news
     * POST /api/news
     */
    @PostMapping
    public ResponseEntity<News> createNews(@RequestBody News news) {
        News created = newsService.createNews(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update news
     * PUT /api/news/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(@PathVariable Long id, @RequestBody News updates) {
        return ResponseEntity.ok(newsService.updateNews(id, updates));
    }

    /**
     * Delete news
     * DELETE /api/news/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark news as read
     * PATCH /api/news/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<News> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.markAsRead(id));
    }

    /**
     * Mark all news as read for a symbol
     * PATCH /api/news/read-all/{symbol}
     */
    @PatchMapping("/read-all/{symbol}")
    public ResponseEntity<Map<String, String>> markAllAsReadBySymbol(@PathVariable String symbol) {
        newsService.markAllAsReadBySymbol(symbol);
        return ResponseEntity.ok(Map.of("message", "All news marked as read for symbol: " + symbol));
    }

    /**
     * Delete old news
     * DELETE /api/news/old?daysOld=30
     */
    @DeleteMapping("/old")
    public ResponseEntity<Map<String, Object>> deleteOldNews(@RequestParam(defaultValue = "30") int daysOld) {
        int deleted = newsService.deleteOldNews(daysOld);
        return ResponseEntity.ok(Map.of("deleted", deleted, "daysOld", daysOld));
    }

    /**
     * Get news count by symbol
     * GET /api/news/count/{symbol}
     */
    @GetMapping("/count/{symbol}")
    public ResponseEntity<Map<String, Object>> getNewsCountBySymbol(@PathVariable String symbol) {
        long count = newsService.getNewsCountBySymbol(symbol);
        return ResponseEntity.ok(Map.of("symbol", symbol, "count", count));
    }

    /**
     * Get unread news count
     * GET /api/news/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNewsCount() {
        long count = newsService.getUnreadNewsCount();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get news statistics
     * GET /api/news/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNewsStatistics() {
        return ResponseEntity.ok(newsService.getNewsStatistics());
    }

    /**
     * Get distinct news sources
     * GET /api/news/sources
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getDistinctSources() {
        return ResponseEntity.ok(newsService.getDistinctSources());
    }

    /**
     * Get distinct symbols with news
     * GET /api/news/symbols
     */
    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getDistinctSymbols() {
        return ResponseEntity.ok(newsService.getDistinctSymbols());
    }
}
