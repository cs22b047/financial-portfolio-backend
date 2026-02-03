-- ============================================================================
-- SIMPLIFY DIVIDENDS TABLE
-- ============================================================================
-- Based on actual Yahoo Finance API data:
--   - Date (payment_date)
--   - Amount per share
-- 
-- Calculated fields:
--   - shares_held (from assets table)
--   - total_amount (calculated: amount_per_share * shares_held)
-- ============================================================================

USE portfolio_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop old complex table
DROP TABLE IF EXISTS dividends;

-- Create simplified dividends table
CREATE TABLE dividends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to asset
    asset_id BIGINT NOT NULL,
    
    -- Payment Details (from Yahoo Finance)
    payment_date DATE NOT NULL,
    amount_per_share DECIMAL(15,8) NOT NULL,
    
    -- Calculated Fields
    shares_held DECIMAL(15,8),        -- Snapshot of shares at payment time
    total_amount DECIMAL(15,4),       -- amount_per_share * shares_held
    
    -- Metadata
    currency VARCHAR(10) DEFAULT 'USD',
    
    -- Timestamps
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_asset (asset_id),
    INDEX idx_payment_date (payment_date),
    UNIQUE KEY uk_asset_payment (asset_id, payment_date),  -- Prevent duplicates
    
    -- Foreign Key
    CONSTRAINT fk_dividends_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- Verify
SELECT 'Dividends table simplified!' AS status;
DESCRIBE dividends;
