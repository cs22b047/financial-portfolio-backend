package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Alert;
import com.example.portfolioapp.entity.AlertDirection;
import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.exception.InvalidOperationException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AlertRepository;
import com.example.portfolioapp.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for Alert operations
 * Manages price alerts for assets and scheduled alert checking
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Create a new price alert for an asset
     */
    @Transactional
    public Alert createAlert(Long assetId, BigDecimal targetPrice, AlertDirection aboveOrBelow) {
        logger.info("Creating alert for asset ID: {}, target price: {}, direction: {}", 
                    assetId, targetPrice, aboveOrBelow);

        // Validate input
        if (targetPrice == null || targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Target price must be greater than zero");
        }

        if (aboveOrBelow == null) {
            throw new InvalidOperationException("Alert direction (ABOVE/BELOW) must be specified");
        }

        // Find the asset
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        // Create and save the alert
        Alert alert = new Alert(asset, targetPrice, aboveOrBelow);
        Alert savedAlert = alertRepository.save(alert);

        logger.info("Alert created successfully with ID: {}", savedAlert.getId());
        return savedAlert;
    }

    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    /**
     * Get alert by ID
     */
    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    /**
     * Get all alerts for a specific asset
     */
    public List<Alert> getAlertsByAssetId(Long assetId) {
        return alertRepository.findByAssetId(assetId);
    }

    /**
     * Get all active (non-triggered) alerts
     */
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByTriggeredFalse();
    }

    /**
     * Get all triggered alerts
     */
    public List<Alert> getTriggeredAlerts() {
        return alertRepository.findByTriggeredTrue();
    }

    /**
     * Get active alerts for a specific asset
     */
    public List<Alert> getActiveAlertsByAssetId(Long assetId) {
        return alertRepository.findByAssetIdAndTriggeredFalse(assetId);
    }

    /**
     * Update an existing alert
     */
    @Transactional
    public Alert updateAlert(Long id, BigDecimal targetPrice, AlertDirection aboveOrBelow, Boolean triggered) {
        logger.info("Updating alert ID: {}", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        // Update fields if provided
        if (targetPrice != null) {
            if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("Target price must be greater than zero");
            }
            alert.setTargetPrice(targetPrice);
        }

        if (aboveOrBelow != null) {
            alert.setAboveOrBelow(aboveOrBelow);
        }

        if (triggered != null) {
            alert.setTriggered(triggered);
        }

        Alert updatedAlert = alertRepository.save(alert);
        logger.info("Alert updated successfully: {}", id);
        return updatedAlert;
    }

    /**
     * Reset a triggered alert (set triggered to false)
     */
    @Transactional
    public Alert resetAlert(Long id) {
        logger.info("Resetting alert ID: {}", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));

        alert.setTriggered(false);
        Alert resetAlert = alertRepository.save(alert);

        logger.info("Alert reset successfully: {}", id);
        return resetAlert;
    }

    /**
     * Delete an alert
     */
    @Transactional
    public void deleteAlert(Long id) {
        logger.info("Deleting alert ID: {}", id);

        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alert", id);
        }

        alertRepository.deleteById(id);
        logger.info("Alert deleted successfully: {}", id);
    }

    /**
     * Delete all alerts for an asset
     */
    @Transactional
    public void deleteAlertsByAssetId(Long assetId) {
        logger.info("Deleting all alerts for asset ID: {}", assetId);
        alertRepository.deleteByAssetId(assetId);
        logger.info("Alerts deleted for asset ID: {}", assetId);
    }

    /**
     * Check and trigger alerts based on current asset prices
     * This method is scheduled to run periodically (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5 minutes
    @Transactional
    public void checkAndTriggerAlerts() {
        logger.info("Starting scheduled alert check...");

        try {
            // Find all alerts that meet trigger conditions
            List<Alert> alertsToTrigger = alertRepository.findAlertsThatShouldTrigger();

            if (alertsToTrigger.isEmpty()) {
                logger.info("No alerts to trigger at this time");
                return;
            }

            logger.info("Found {} alert(s) to trigger", alertsToTrigger.size());

            // Trigger each alert
            for (Alert alert : alertsToTrigger) {
                triggerAlert(alert);
            }

            logger.info("Alert check completed. Triggered {} alert(s)", alertsToTrigger.size());

        } catch (Exception e) {
            logger.error("Error during alert check: {}", e.getMessage(), e);
        }
    }

    /**
     * Trigger a single alert
     */
    private void triggerAlert(Alert alert) {
        alert.setTriggered(true);
        alertRepository.save(alert);

        Asset asset = alert.getAsset();
        logger.info("ALERT TRIGGERED: {} alert for {} - Target: {}, Current: {}", 
                    alert.getAboveOrBelow(),
                    asset.getSymbol(),
                    alert.getTargetPrice(),
                    asset.getCurrentPrice());

        // TODO: Send notification (email, in-app, push, etc.)
        // This is where you would integrate with a notification service
    }

    /**
     * Manually check and trigger alerts (useful for testing)
     */
    @Transactional
    public int checkAndTriggerAlertsManually() {
        logger.info("Manual alert check triggered");
        
        List<Alert> alertsToTrigger = alertRepository.findAlertsThatShouldTrigger();
        
        for (Alert alert : alertsToTrigger) {
            triggerAlert(alert);
        }
        
        logger.info("Manual alert check completed. Triggered {} alert(s)", alertsToTrigger.size());
        return alertsToTrigger.size();
    }

    /**
     * Get count of active alerts for an asset
     */
    public long countActiveAlertsByAssetId(Long assetId) {
        return alertRepository.countActiveAlertsByAssetId(assetId);
    }
}
