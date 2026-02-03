package com.example.portfolioapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reference table for bond credit ratings.
 * Supports multiple rating agencies (S&P, Moody's, Fitch).
 */
@Entity
@Table(name = "credit_ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"agency", "rating"}))
public class CreditRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RatingAgency agency;

    @Column(nullable = false, length = 10)
    private String rating; // AAA, AA+, BBB-, etc.

    @Column(name = "rating_rank")
    private Integer ratingRank; // Numeric rank for comparison (1=highest quality)

    @Enumerated(EnumType.STRING)
    @Column(name = "rating_category", length = 20)
    private RatingCategory ratingCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public CreditRating() {
        this.createdDate = LocalDateTime.now();
    }

    public CreditRating(RatingAgency agency, String rating, Integer ratingRank, RatingCategory category) {
        this();
        this.agency = agency;
        this.rating = rating;
        this.ratingRank = ratingRank;
        this.ratingCategory = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RatingAgency getAgency() { return agency; }
    public void setAgency(RatingAgency agency) { this.agency = agency; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public Integer getRatingRank() { return ratingRank; }
    public void setRatingRank(Integer ratingRank) { this.ratingRank = ratingRank; }

    public RatingCategory getRatingCategory() { return ratingCategory; }
    public void setRatingCategory(RatingCategory ratingCategory) { this.ratingCategory = ratingCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public boolean isInvestmentGrade() {
        return ratingCategory == RatingCategory.INVESTMENT_GRADE;
    }

    @Override
    public String toString() {
        return "CreditRating{agency=" + agency + ", rating='" + rating + "'}";
    }
}
