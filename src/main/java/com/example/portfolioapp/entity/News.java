package com.example.portfolioapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * News entity for storing financial news articles related to market data.
 * 
 * Relationships:
 * - Many News -> One MarketData (optional, nullable)
 */
@Entity
@Table(name = "news",
       indexes = {
           @Index(name = "idx_news_symbol", columnList = "symbol"),
           @Index(name = "idx_news_market_data", columnList = "market_data_id"),
           @Index(name = "idx_news_published_date", columnList = "published_date"),
           @Index(name = "idx_news_sentiment", columnList = "sentiment")
       })
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_data_id")
    @JsonIgnore
    private MarketData marketData;

    @Column(name = "symbol", length = 20)
    private String symbol;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "link", length = 1000)
    private String link;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "publisher", length = 200)
    private String publisher;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "sentiment", length = 20)
    private String sentiment;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }

    // Constructors
    public News() {}

    public News(String symbol, String title, String summary) {
        this.symbol = symbol;
        this.title = title;
        this.summary = summary;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarketData getMarketData() {
        return marketData;
    }

    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
        if (marketData != null) {
            this.symbol = marketData.getSymbol();
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}
