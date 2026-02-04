-- Migration: Create Alerts Table
-- Description: Creates the alerts table for price alert functionality
-- One-to-many relationship: Asset (1) -> Alerts (Many)

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    target_price DECIMAL(15, 4) NOT NULL,
    above_or_below ENUM('ABOVE', 'BELOW') NOT NULL,
    triggered BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_alerts_asset FOREIGN KEY (asset_id) 
        REFERENCES assets (id) ON DELETE CASCADE,
    INDEX idx_alerts_asset_id (asset_id),
    INDEX idx_alerts_triggered (triggered),
    INDEX idx_alerts_asset_triggered (asset_id, triggered)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
