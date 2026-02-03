package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.UserSettings;
import com.example.portfolioapp.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for UserSettings operations.
 */
@RestController
@RequestMapping("/api/user-settings")
@CrossOrigin(origins = "*")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    /**
     * Get all user settings
     * GET /api/user-settings
     */
    @GetMapping
    public ResponseEntity<List<UserSettings>> getAllSettings() {
        return ResponseEntity.ok(userSettingsService.getAllSettings());
    }

    /**
     * Get settings by ID
     * GET /api/user-settings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserSettings> getSettingsById(@PathVariable Long id) {
        return ResponseEntity.ok(userSettingsService.getSettingsById(id));
    }

    /**
     * Get settings by user name
     * GET /api/user-settings/user/{userName}
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<UserSettings> getSettingsByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(userSettingsService.getSettingsByUserName(userName));
    }

    /**
     * Get or create default settings
     * GET /api/user-settings/default
     */
    @GetMapping("/default")
    public ResponseEntity<UserSettings> getDefaultSettings() {
        return ResponseEntity.ok(userSettingsService.getOrCreateDefaultSettings());
    }

    /**
     * Create new user settings
     * POST /api/user-settings
     */
    @PostMapping
    public ResponseEntity<UserSettings> createSettings(@RequestBody UserSettings settings) {
        UserSettings created = userSettingsService.createSettings(settings);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update settings by ID
     * PUT /api/user-settings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserSettings> updateSettings(
            @PathVariable Long id,
            @RequestBody UserSettings updates) {
        return ResponseEntity.ok(userSettingsService.updateSettings(id, updates));
    }

    /**
     * Update settings by user name
     * PUT /api/user-settings/user/{userName}
     */
    @PutMapping("/user/{userName}")
    public ResponseEntity<UserSettings> updateSettingsByUserName(
            @PathVariable String userName,
            @RequestBody UserSettings updates) {
        return ResponseEntity.ok(userSettingsService.updateSettingsByUserName(userName, updates));
    }

    /**
     * Delete settings by ID
     * DELETE /api/user-settings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSettings(@PathVariable Long id) {
        userSettingsService.deleteSettings(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete settings by user name
     * DELETE /api/user-settings/user/{userName}
     */
    @DeleteMapping("/user/{userName}")
    public ResponseEntity<Void> deleteSettingsByUserName(@PathVariable String userName) {
        userSettingsService.deleteSettingsByUserName(userName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle notifications
     * PATCH /api/user-settings/{id}/toggle-notifications
     */
    @PatchMapping("/{id}/toggle-notifications")
    public ResponseEntity<UserSettings> toggleNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(userSettingsService.toggleNotifications(id));
    }

    /**
     * Toggle price alerts
     * PATCH /api/user-settings/{id}/toggle-price-alerts
     */
    @PatchMapping("/{id}/toggle-price-alerts")
    public ResponseEntity<UserSettings> togglePriceAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(userSettingsService.togglePriceAlerts(id));
    }

    /**
     * Change theme
     * PATCH /api/user-settings/{id}/theme
     */
    @PatchMapping("/{id}/theme")
    public ResponseEntity<UserSettings> changeTheme(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String theme = request.get("theme");
        return ResponseEntity.ok(userSettingsService.changeTheme(id, theme));
    }

    /**
     * Check if settings exist for user
     * GET /api/user-settings/exists/{userName}
     */
    @GetMapping("/exists/{userName}")
    public ResponseEntity<Map<String, Boolean>> existsByUserName(@PathVariable String userName) {
        boolean exists = userSettingsService.existsByUserName(userName);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Get users with notifications enabled
     * GET /api/user-settings/notifications-enabled
     */
    @GetMapping("/notifications-enabled")
    public ResponseEntity<List<UserSettings>> getUsersWithNotificationsEnabled() {
        return ResponseEntity.ok(userSettingsService.getUsersWithNotificationsEnabled());
    }

    /**
     * Get users with price alerts enabled
     * GET /api/user-settings/price-alerts-enabled
     */
    @GetMapping("/price-alerts-enabled")
    public ResponseEntity<List<UserSettings>> getUsersWithPriceAlertsEnabled() {
        return ResponseEntity.ok(userSettingsService.getUsersWithPriceAlertsEnabled());
    }
}
