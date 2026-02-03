package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSettings entity.
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    /**
     * Find settings by user name
     */
    Optional<UserSettings> findByUserName(String userName);

    /**
     * Check if user settings exist by user name
     */
    boolean existsByUserName(String userName);

    /**
     * Find all users with notifications enabled
     */
    List<UserSettings> findByNotificationsEnabledTrue();

    /**
     * Find all users with price alerts enabled
     */
    List<UserSettings> findByPriceAlertsEnabledTrue();

    /**
     * Find users by theme
     */
    List<UserSettings> findByTheme(String theme);

    /**
     * Find users by default currency
     */
    List<UserSettings> findByDefaultCurrency(String currency);

    /**
     * Count users by theme
     */
    @Query("SELECT u.theme, COUNT(u) FROM UserSettings u GROUP BY u.theme")
    List<Object[]> countByTheme();

    /**
     * Get or create default settings
     */
    @Query("SELECT u FROM UserSettings u WHERE u.userName = 'default' OR u.id = 1 ORDER BY u.id ASC")
    Optional<UserSettings> findDefaultSettings();
}
