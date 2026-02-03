package com.example.portfolioapp.exception;

/**
 * Exception thrown when ESG rating is not found
 */
public class ESGRatingNotFoundException extends RuntimeException {

    public ESGRatingNotFoundException(String message) {
        super(message);
    }

    public ESGRatingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESGRatingNotFoundException(Long id) {
        super("ESG rating not found with id: " + id);
    }

    public ESGRatingNotFoundException(String field, String value) {
        super(String.format("ESG rating not found with %s: %s", field, value));
    }
}
