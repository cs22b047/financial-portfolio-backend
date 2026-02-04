package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserSettings entity.
 * Single-row design - only one settings record exists.
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    /**
     * Get the singleton settings record
     * Since there's only one row, this always returns the same record
     */
    @Query("SELECT u FROM UserSettings u")
    Optional<UserSettings> getSettings();

    /**
     * Check if settings exist
     */
    @Query("SELECT COUNT(u) > 0 FROM UserSettings u")
    boolean settingsExist();
}
