package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.UserSettings;
import com.example.portfolioapp.exception.DuplicateResourceException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for UserSettings operations.
 */
@Service
public class UserSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    /**
     * Get all user settings
     */
    public List<UserSettings> getAllSettings() {
        return userSettingsRepository.findAll();
    }

    /**
     * Get settings by ID
     */
    public UserSettings getSettingsById(Long id) {
        return userSettingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", id));
    }

    /**
     * Get settings by user name
     */
    public UserSettings getSettingsByUserName(String userName) {
        return userSettingsRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", "userName", userName));
    }

    /**
     * Get or create default settings
     */
    @Transactional
    public UserSettings getOrCreateDefaultSettings() {
        Optional<UserSettings> existing = userSettingsRepository.findDefaultSettings();
        if (existing.isPresent()) {
            return existing.get();
        }
        
        UserSettings defaultSettings = new UserSettings("default");
        return userSettingsRepository.save(defaultSettings);
    }

    /**
     * Create new user settings
     */
    @Transactional
    public UserSettings createSettings(UserSettings settings) {
        logger.info("Creating user settings for: {}", settings.getUserName());

        if (settings.getUserName() != null && userSettingsRepository.existsByUserName(settings.getUserName())) {
            throw new DuplicateResourceException("UserSettings already exists for user: " + settings.getUserName());
        }

        return userSettingsRepository.save(settings);
    }

    /**
     * Update existing settings
     */
    @Transactional
    public UserSettings updateSettings(Long id, UserSettings updates) {
        logger.info("Updating user settings: {}", id);

        UserSettings settings = userSettingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", id));

        // Update fields if provided
        if (updates.getUserName() != null) {
            settings.setUserName(updates.getUserName());
        }
        if (updates.getDefaultCurrency() != null) {
            settings.setDefaultCurrency(updates.getDefaultCurrency());
        }
        if (updates.getTimezone() != null) {
            settings.setTimezone(updates.getTimezone());
        }
        if (updates.getDateFormat() != null) {
            settings.setDateFormat(updates.getDateFormat());
        }
        if (updates.getDecimalPlaces() != null) {
            settings.setDecimalPlaces(updates.getDecimalPlaces());
        }
        if (updates.getTheme() != null) {
            settings.setTheme(updates.getTheme());
        }
        if (updates.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(updates.getNotificationsEnabled());
        }
        if (updates.getPriceAlertsEnabled() != null) {
            settings.setPriceAlertsEnabled(updates.getPriceAlertsEnabled());
        }

        return userSettingsRepository.save(settings);
    }

    /**
     * Update settings by user name
     */
    @Transactional
    public UserSettings updateSettingsByUserName(String userName, UserSettings updates) {
        logger.info("Updating settings for user: {}", userName);

        UserSettings settings = userSettingsRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", "userName", userName));

        return updateSettings(settings.getId(), updates);
    }

    /**
     * Delete settings by ID
     */
    @Transactional
    public void deleteSettings(Long id) {
        if (!userSettingsRepository.existsById(id)) {
            throw new ResourceNotFoundException("UserSettings", id);
        }
        userSettingsRepository.deleteById(id);
        logger.info("Deleted user settings: {}", id);
    }

    /**
     * Delete settings by user name
     */
    @Transactional
    public void deleteSettingsByUserName(String userName) {
        UserSettings settings = userSettingsRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", "userName", userName));
        userSettingsRepository.delete(settings);
        logger.info("Deleted settings for user: {}", userName);
    }

    /**
     * Toggle notifications
     */
    @Transactional
    public UserSettings toggleNotifications(Long id) {
        UserSettings settings = getSettingsById(id);
        settings.setNotificationsEnabled(!settings.getNotificationsEnabled());
        return userSettingsRepository.save(settings);
    }

    /**
     * Toggle price alerts
     */
    @Transactional
    public UserSettings togglePriceAlerts(Long id) {
        UserSettings settings = getSettingsById(id);
        settings.setPriceAlertsEnabled(!settings.getPriceAlertsEnabled());
        return userSettingsRepository.save(settings);
    }

    /**
     * Change theme
     */
    @Transactional
    public UserSettings changeTheme(Long id, String theme) {
        UserSettings settings = getSettingsById(id);
        settings.setTheme(theme);
        return userSettingsRepository.save(settings);
    }

    /**
     * Get users with notifications enabled
     */
    public List<UserSettings> getUsersWithNotificationsEnabled() {
        return userSettingsRepository.findByNotificationsEnabledTrue();
    }

    /**
     * Get users with price alerts enabled
     */
    public List<UserSettings> getUsersWithPriceAlertsEnabled() {
        return userSettingsRepository.findByPriceAlertsEnabledTrue();
    }

    /**
     * Check if settings exist for user
     */
    public boolean existsByUserName(String userName) {
        return userSettingsRepository.existsByUserName(userName);
    }
}
