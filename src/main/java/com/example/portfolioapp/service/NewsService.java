package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.entity.News;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.MarketDataRepository;
import com.example.portfolioapp.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for News operations.
 */
@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private MarketDataRepository marketDataRepository;

    /**
     * Get all news
     */
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    /**
     * Get news by ID
     */
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", id));
    }

    /**
     * Get news by symbol
     */
    public List<News> getNewsBySymbol(String symbol) {
        return newsRepository.findBySymbolOrderByPublishedDateDesc(symbol);
    }

    /**
     * Get news by symbol with pagination
     */
    public Page<News> getNewsBySymbol(String symbol, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findBySymbol(symbol, pageable);
    }

    /**
     * Get news by market data ID
     */
    public List<News> getNewsByMarketDataId(Long marketDataId) {
        return newsRepository.findByMarketDataIdOrderByPublishedDateDesc(marketDataId);
    }

    /**
     * Get news by sentiment
     */
    public List<News> getNewsBySentiment(String sentiment) {
        return newsRepository.findBySentimentOrderByPublishedDateDesc(sentiment);
    }

    /**
     * Get news by source
     */
    public List<News> getNewsBySource(String source) {
        return newsRepository.findBySourceOrderByPublishedDateDesc(source);
    }

    /**
     * Get unread news
     */
    public List<News> getUnreadNews() {
        return newsRepository.findByIsReadFalseOrderByPublishedDateDesc();
    }

    /**
     * Get unread news for a symbol
     */
    public List<News> getUnreadNewsBySymbol(String symbol) {
        return newsRepository.findBySymbolAndIsReadFalseOrderByPublishedDateDesc(symbol);
    }

    /**
     * Get recent news (last N days)
     */
    public List<News> getRecentNews(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsRepository.findRecentNews(since);
    }

    /**
     * Get recent news for a symbol
     */
    public List<News> getRecentNewsBySymbol(String symbol, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsRepository.findRecentNewsBySymbol(symbol, since);
    }

    /**
     * Get news by date range
     */
    public List<News> getNewsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return newsRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Search news by keyword
     */
    public List<News> searchNews(String keyword) {
        return newsRepository.searchByKeyword(keyword);
    }

    /**
     * Get top news (most recent, limited)
     */
    public List<News> getTopNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findTopNews(pageable);
    }

    /**
     * Create news
     */
    @Transactional
    public News createNews(News news) {
        logger.info("Creating news: {}", news.getTitle());

        // Link to market data if symbol provided
        if (news.getSymbol() != null && news.getMarketData() == null) {
            Optional<MarketData> marketData = marketDataRepository.findBySymbol(news.getSymbol());
            marketData.ifPresent(news::setMarketData);
        }

        return newsRepository.save(news);
    }

    /**
     * Update news
     */
    @Transactional
    public News updateNews(Long id, News updates) {
        logger.info("Updating news: {}", id);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", id));

        if (updates.getTitle() != null) {
            news.setTitle(updates.getTitle());
        }
        if (updates.getSummary() != null) {
            news.setSummary(updates.getSummary());
        }
        if (updates.getLink() != null) {
            news.setLink(updates.getLink());
        }
        if (updates.getImageUrl() != null) {
            news.setImageUrl(updates.getImageUrl());
        }
        if (updates.getSource() != null) {
            news.setSource(updates.getSource());
        }
        if (updates.getPublisher() != null) {
            news.setPublisher(updates.getPublisher());
        }
        if (updates.getSentiment() != null) {
            news.setSentiment(updates.getSentiment());
        }
        if (updates.getIsRead() != null) {
            news.setIsRead(updates.getIsRead());
        }

        return newsRepository.save(news);
    }

    /**
     * Delete news
     */
    @Transactional
    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new ResourceNotFoundException("News", id);
        }
        newsRepository.deleteById(id);
        logger.info("Deleted news: {}", id);
    }

    /**
     * Mark news as read
     */
    @Transactional
    public News markAsRead(Long id) {
        News news = getNewsById(id);
        news.setIsRead(true);
        return newsRepository.save(news);
    }

    /**
     * Mark all news as read for a symbol
     */
    @Transactional
    public void markAllAsReadBySymbol(String symbol) {
        newsRepository.markAllAsReadBySymbol(symbol);
        logger.info("Marked all news as read for symbol: {}", symbol);
    }

    /**
     * Delete old news
     */
    @Transactional
    public int deleteOldNews(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = newsRepository.deleteOldNews(cutoffDate);
        logger.info("Deleted {} old news articles older than {} days", deleted, daysOld);
        return deleted;
    }

    /**
     * Get news count by symbol
     */
    public long getNewsCountBySymbol(String symbol) {
        return newsRepository.countBySymbol(symbol);
    }

    /**
     * Get unread news count
     */
    public long getUnreadNewsCount() {
        return newsRepository.countByIsReadFalse();
    }

    /**
     * Get news statistics
     */
    public Map<String, Object> getNewsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalNews", newsRepository.count());
        stats.put("unreadCount", newsRepository.countByIsReadFalse());
        stats.put("sources", newsRepository.findDistinctSources());
        stats.put("symbolsWithNews", newsRepository.findDistinctSymbols());
        
        // Sentiment breakdown
        List<Object[]> sentimentCounts = newsRepository.countBySentiment();
        Map<String, Long> sentimentBreakdown = new HashMap<>();
        for (Object[] row : sentimentCounts) {
            String sentiment = row[0] != null ? row[0].toString() : "unknown";
            Long count = (Long) row[1];
            sentimentBreakdown.put(sentiment, count);
        }
        stats.put("sentimentBreakdown", sentimentBreakdown);
        
        return stats;
    }

    /**
     * Get distinct sources
     */
    public List<String> getDistinctSources() {
        return newsRepository.findDistinctSources();
    }

    /**
     * Get distinct symbols with news
     */
    public List<String> getDistinctSymbols() {
        return newsRepository.findDistinctSymbols();
    }
}
