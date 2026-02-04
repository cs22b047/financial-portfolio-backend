package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.UserSettings;
import com.example.portfolioapp.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST Controller for UserSettings operations.
 * Single-row design - manages the one settings record.
 */
@RestController
@RequestMapping("/api/user-settings")
@CrossOrigin(origins = "*")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    /**
     * Get user settings
     * GET /api/user-settings
     */
    @GetMapping
    public ResponseEntity<UserSettings> getSettings() {
        return ResponseEntity.ok(userSettingsService.getSettings());
    }

    /**
     * Update user settings
     * PUT /api/user-settings
     */
    @PutMapping
    public ResponseEntity<UserSettings> updateSettings(@RequestBody UserSettings updates) {
        return ResponseEntity.ok(userSettingsService.updateSettings(updates));
    }

    /**
     * Change theme
     * PATCH /api/user-settings/theme
     */
    @PatchMapping("/theme")
    public ResponseEntity<UserSettings> changeTheme(@RequestBody Map<String, String> request) {
        String theme = request.get("theme");
        if (theme == null || theme.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userSettingsService.changeTheme(theme));
    }

    /**
     * Get wallet balance
     * GET /api/user-settings/wallet
     */
    @GetMapping("/wallet")
    public ResponseEntity<Map<String, BigDecimal>> getWallet() {
        BigDecimal balance = userSettingsService.getWalletBalance();
        return ResponseEntity.ok(Map.of("wallet", balance));
    }

    /**
     * Update wallet balance
     * PATCH /api/user-settings/wallet
     */
    @PatchMapping("/wallet")
    public ResponseEntity<UserSettings> updateWallet(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        if (amount == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userSettingsService.updateWallet(amount));
    }

    /**
     * Add to wallet
     * POST /api/user-settings/wallet/add
     */
    @PostMapping("/wallet/add")
    public ResponseEntity<UserSettings> addToWallet(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userSettingsService.addToWallet(amount));
    }

    /**
     * Subtract from wallet
     * POST /api/user-settings/wallet/subtract
     */
    @PostMapping("/wallet/subtract")
    public ResponseEntity<?> subtractFromWallet(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(userSettingsService.subtractFromWallet(amount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get target goal
     * GET /api/user-settings/target
     */
    @GetMapping("/target")
    public ResponseEntity<Map<String, BigDecimal>> getTarget() {
        BigDecimal target = userSettingsService.getTarget();
        return ResponseEntity.ok(Map.of("target", target));
    }

    /**
     * Update target goal
     * PATCH /api/user-settings/target
     */
    @PatchMapping("/target")
    public ResponseEntity<UserSettings> updateTarget(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal target = request.get("target");
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userSettingsService.updateTarget(target));
    }
}
