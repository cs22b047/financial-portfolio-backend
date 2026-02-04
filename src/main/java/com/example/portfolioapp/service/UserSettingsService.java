package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.UserSettings;
import com.example.portfolioapp.repository.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for UserSettings operations.
 * Single-row design - manages the one and only settings record.
 */
@Service
public class UserSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    /**
     * Get the singleton settings record
     * Creates default settings if none exist
     */
    @Transactional
    public UserSettings getSettings() {
        return userSettingsRepository.getSettings()
                .orElseGet(this::createDefaultSettings);
    }

    /**
     * Create default settings (called once during initialization)
     */
    @Transactional
    private UserSettings createDefaultSettings() {
        logger.info("Creating default user settings");
        UserSettings defaultSettings = new UserSettings(
            "user",
            "USD",
            "UTC",
            "USD",
            "light",
            BigDecimal.ZERO
        );
        return userSettingsRepository.save(defaultSettings);
    }

    /**
     * Update settings
     * Only one settings record exists, so this updates that record
     */
    @Transactional
    public UserSettings updateSettings(UserSettings updates) {
        logger.info("Updating user settings");
        
        UserSettings current = getSettings();

        // Update only non-null fields
        if (updates.getUserName() != null) {
            current.setUserName(updates.getUserName());
        }
        if (updates.getDefaultCurrency() != null) {
            current.setDefaultCurrency(updates.getDefaultCurrency());
        }
        if (updates.getTimeZone() != null) {
            current.setTimeZone(updates.getTimeZone());
        }
        if (updates.getCurrency() != null) {
            current.setCurrency(updates.getCurrency());
        }
        if (updates.getTheme() != null) {
            current.setTheme(updates.getTheme());
        }
        if (updates.getWallet() != null) {
            current.setWallet(updates.getWallet());
        }
        if (updates.getTarget() != null) {
            current.setTarget(updates.getTarget());
        }

        return userSettingsRepository.save(current);
    }

    /**
     * Update wallet balance
     */
    @Transactional
    public UserSettings updateWallet(BigDecimal amount) {
        logger.info("Updating wallet: {}", amount);
        UserSettings settings = getSettings();
        settings.setWallet(amount);
        return userSettingsRepository.save(settings);
    }

    /**
     * Add to wallet
     */
    @Transactional
    public UserSettings addToWallet(BigDecimal amount) {
        logger.info("Adding to wallet: {}", amount);
        UserSettings settings = getSettings();
        settings.setWallet(settings.getWallet().add(amount));
        return userSettingsRepository.save(settings);
    }

    /**
     * Subtract from wallet
     */
    @Transactional
    public UserSettings subtractFromWallet(BigDecimal amount) {
        logger.info("Subtracting from wallet: {}", amount);
        UserSettings settings = getSettings();
        BigDecimal newBalance = settings.getWallet().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }
        settings.setWallet(newBalance);
        return userSettingsRepository.save(settings);
    }

    /**
     * Change theme
     */
    @Transactional
    public UserSettings changeTheme(String theme) {
        logger.info("Changing theme to: {}", theme);
        UserSettings settings = getSettings();
        settings.setTheme(theme);
        return userSettingsRepository.save(settings);
    }

    /**
     * Get current wallet balance
     */
    public BigDecimal getWalletBalance() {
        return getSettings().getWallet();
    }

    /**
     * Update target goal
     */
    @Transactional
    public UserSettings updateTarget(BigDecimal target) {
        logger.info("Updating target: {}", target);
        UserSettings settings = getSettings();
        settings.setTarget(target);
        return userSettingsRepository.save(settings);
    }

    /**
     * Get current target goal
     */
    public BigDecimal getTarget() {
        return getSettings().getTarget();
    }
}
