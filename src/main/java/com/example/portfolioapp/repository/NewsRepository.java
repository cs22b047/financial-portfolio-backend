package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for News entity.
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    /**
     * Find news by symbol
     */
    List<News> findBySymbolOrderByPublishedDateDesc(String symbol);

    /**
     * Find news by symbol with pagination
     */
    Page<News> findBySymbol(String symbol, Pageable pageable);

    /**
     * Find news by market data ID
     */
    List<News> findByMarketDataIdOrderByPublishedDateDesc(Long marketDataId);

    /**
     * Find news by sentiment
     */
    List<News> findBySentimentOrderByPublishedDateDesc(String sentiment);

    /**
     * Find news by source
     */
    List<News> findBySourceOrderByPublishedDateDesc(String source);

    /**
     * Find unread news
     */
    List<News> findByIsReadFalseOrderByPublishedDateDesc();

    /**
     * Find unread news for a symbol
     */
    List<News> findBySymbolAndIsReadFalseOrderByPublishedDateDesc(String symbol);

    /**
     * Find recent news (last N days)
     */
    @Query("SELECT n FROM News n WHERE n.publishedDate >= :since ORDER BY n.publishedDate DESC")
    List<News> findRecentNews(@Param("since") LocalDateTime since);

    /**
     * Find recent news for a symbol
     */
    @Query("SELECT n FROM News n WHERE n.symbol = :symbol AND n.publishedDate >= :since ORDER BY n.publishedDate DESC")
    List<News> findRecentNewsBySymbol(@Param("symbol") String symbol, @Param("since") LocalDateTime since);

    /**
     * Find news by date range
     */
    @Query("SELECT n FROM News n WHERE n.publishedDate BETWEEN :startDate AND :endDate ORDER BY n.publishedDate DESC")
    List<News> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Search news by title or summary containing keyword
     */
    @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(n.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY n.publishedDate DESC")
    List<News> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find top news (most recent, limited)
     */
    @Query("SELECT n FROM News n ORDER BY n.publishedDate DESC")
    List<News> findTopNews(Pageable pageable);

    /**
     * Count news by symbol
     */
    long countBySymbol(String symbol);

    /**
     * Count unread news
     */
    long countByIsReadFalse();

    /**
     * Count news by sentiment
     */
    @Query("SELECT n.sentiment, COUNT(n) FROM News n GROUP BY n.sentiment")
    List<Object[]> countBySentiment();

    /**
     * Mark news as read
     */
    @Modifying
    @Query("UPDATE News n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    /**
     * Mark all news as read for a symbol
     */
    @Modifying
    @Query("UPDATE News n SET n.isRead = true WHERE n.symbol = :symbol")
    void markAllAsReadBySymbol(@Param("symbol") String symbol);

    /**
     * Delete old news (older than specified date)
     */
    @Modifying
    @Query("DELETE FROM News n WHERE n.publishedDate < :cutoffDate")
    int deleteOldNews(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find distinct sources
     */
    @Query("SELECT DISTINCT n.source FROM News n WHERE n.source IS NOT NULL ORDER BY n.source")
    List<String> findDistinctSources();

    /**
     * Find distinct symbols with news
     */
    @Query("SELECT DISTINCT n.symbol FROM News n WHERE n.symbol IS NOT NULL ORDER BY n.symbol")
    List<String> findDistinctSymbols();
}
