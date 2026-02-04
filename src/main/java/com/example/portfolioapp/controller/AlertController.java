package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Alert;
import com.example.portfolioapp.entity.AlertDirection;
import com.example.portfolioapp.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Alerts
 * Manages price alerts for assets
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * Create a new alert
     * POST /api/alerts
     * Body: {
     *   "assetId": 1,
     *   "targetPrice": 150.00,
     *   "aboveOrBelow": "ABOVE"
     * }
     */
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Map<String, Object> request) {
        try {
            Long assetId = Long.parseLong(request.get("assetId").toString());
            BigDecimal targetPrice = new BigDecimal(request.get("targetPrice").toString());
            AlertDirection aboveOrBelow = AlertDirection.valueOf(request.get("aboveOrBelow").toString().toUpperCase());

            Alert alert = alertService.createAlert(assetId, targetPrice, aboveOrBelow);
            return new ResponseEntity<>(alert, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all alerts
     * GET /api/alerts
     */
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        List<Alert> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alert by ID
     * GET /api/alerts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        return alertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all alerts for a specific asset
     * GET /api/alerts/asset/{assetId}
     */
    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<Alert>> getAlertsByAssetId(@PathVariable Long assetId) {
        List<Alert> alerts = alertService.getAlertsByAssetId(assetId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get all active (non-triggered) alerts
     * GET /api/alerts/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        List<Alert> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get all triggered alerts
     * GET /api/alerts/triggered
     */
    @GetMapping("/triggered")
    public ResponseEntity<List<Alert>> getTriggeredAlerts() {
        List<Alert> alerts = alertService.getTriggeredAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get active alerts for a specific asset
     * GET /api/alerts/asset/{assetId}/active
     */
    @GetMapping("/asset/{assetId}/active")
    public ResponseEntity<List<Alert>> getActiveAlertsByAssetId(@PathVariable Long assetId) {
        List<Alert> alerts = alertService.getActiveAlertsByAssetId(assetId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Update an alert
     * PUT /api/alerts/{id}
     * Body: {
     *   "targetPrice": 160.00,
     *   "aboveOrBelow": "BELOW",
     *   "triggered": false
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Alert> updateAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            BigDecimal targetPrice = request.containsKey("targetPrice") 
                    ? new BigDecimal(request.get("targetPrice").toString()) 
                    : null;
            
            AlertDirection aboveOrBelow = request.containsKey("aboveOrBelow")
                    ? AlertDirection.valueOf(request.get("aboveOrBelow").toString().toUpperCase())
                    : null;
            
            Boolean triggered = request.containsKey("triggered")
                    ? Boolean.parseBoolean(request.get("triggered").toString())
                    : null;

            Alert updated = alertService.updateAlert(id, targetPrice, aboveOrBelow, triggered);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset a triggered alert (set triggered to false)
     * PATCH /api/alerts/{id}/reset
     */
    @PatchMapping("/{id}/reset")
    public ResponseEntity<Alert> resetAlert(@PathVariable Long id) {
        try {
            Alert reset = alertService.resetAlert(id);
            return ResponseEntity.ok(reset);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an alert
     * DELETE /api/alerts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAlert(@PathVariable Long id) {
        try {
            alertService.deleteAlert(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete all alerts for an asset
     * DELETE /api/alerts/asset/{assetId}
     */
    @DeleteMapping("/asset/{assetId}")
    public ResponseEntity<Map<String, String>> deleteAlertsByAssetId(@PathVariable Long assetId) {
        try {
            alertService.deleteAlertsByAssetId(assetId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All alerts deleted for asset");
            response.put("assetId", assetId.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Manually trigger alert check (for testing)
     * POST /api/alerts/check
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAlerts() {
        try {
            int triggered = alertService.checkAndTriggerAlertsManually();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert check completed");
            response.put("alertsTriggered", triggered);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get count of active alerts for an asset
     * GET /api/alerts/asset/{assetId}/count
     */
    @GetMapping("/asset/{assetId}/count")
    public ResponseEntity<Map<String, Long>> getActiveAlertCount(@PathVariable Long assetId) {
        long count = alertService.countActiveAlertsByAssetId(assetId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("assetId", assetId);
        response.put("activeAlertCount", count);
        
        return ResponseEntity.ok(response);
    }
}
