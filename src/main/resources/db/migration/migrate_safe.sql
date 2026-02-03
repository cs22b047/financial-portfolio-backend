-- ============================================================================
-- PORTFOLIO DATABASE MIGRATION - SAFE VERSION
-- ============================================================================
-- Purpose: Simplify 19-table schema using original table names
-- Date: 2026-02-02
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- STEP 1: DROP UNNECESSARY TABLES (only if they exist)
-- ============================================================================

DROP TABLE IF EXISTS blockchains;
DROP TABLE IF EXISTS bond_metrics;
DROP TABLE IF EXISTS cash_accounts;
DROP TABLE IF EXISTS corporate_actions;
DROP TABLE IF EXISTS credit_ratings;
DROP TABLE IF EXISTS crypto_metrics;
DROP TABLE IF EXISTS exchanges;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS industries;
DROP TABLE IF EXISTS price_alerts;
DROP TABLE IF EXISTS sectors;
DROP TABLE IF EXISTS stock_metrics;
DROP TABLE IF EXISTS tax_lots;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS portfolio;
DROP TABLE IF EXISTS watchlist;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS user_stocks;
DROP TABLE IF EXISTS settings;

-- ============================================================================
-- STEP 2: ENSURE ASSET_TYPES TABLE EXISTS
-- ============================================================================

-- Asset types table already exists, just ensure default values
INSERT IGNORE INTO asset_types (code, name, description, risk_level, is_active) VALUES
('STOCK', 'Stocks', 'Equity securities representing ownership in public companies', 'MEDIUM', TRUE),
('BOND', 'Bonds', 'Fixed-income debt securities', 'LOW', TRUE),
('ETF', 'ETFs', 'Exchange-traded funds', 'MEDIUM', TRUE),
('MUTUAL_FUND', 'Mutual Funds', 'Pooled investment funds', 'MEDIUM', TRUE),
('CRYPTO', 'Cryptocurrency', 'Digital assets and cryptocurrencies', 'VERY_HIGH', TRUE),
('CASH', 'Cash & Equivalents', 'Liquid assets including savings accounts', 'VERY_LOW', TRUE);

-- ============================================================================
-- STEP 3: CREATE ASSETS TABLE (User's Relationship Table)
-- ============================================================================

CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Links
    asset_type_id BIGINT,
    market_data_id BIGINT NOT NULL,
    
    -- User's Relationship Status
    status ENUM('WATCHLIST', 'OWNED', 'RESEARCH', 'SOLD') NOT NULL DEFAULT 'WATCHLIST',
    
    -- Asset Identification (denormalized for convenience)
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(200),
    
    -- Ownership Data (for OWNED status)
    quantity DECIMAL(15,8),
    purchase_price DECIMAL(15,4),
    current_price DECIMAL(15,4),
    purchase_date DATE,
    
    -- Watchlist/Research Data
    target_price DECIMAL(15,4),
    added_to_watchlist_date DATE,
    price_alerts_enabled BOOLEAN DEFAULT FALSE,
    notes TEXT,
    priority_rank INT,
    
    -- Metadata
    sector VARCHAR(100),
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Constraints
    UNIQUE KEY uk_market_data (market_data_id),
    INDEX idx_status (status),
    INDEX idx_symbol (symbol),
    INDEX idx_asset_type (asset_type_id),
    
    -- Foreign Keys
    CONSTRAINT fk_assets_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types(id) ON DELETE SET NULL,
    CONSTRAINT fk_assets_market_data FOREIGN KEY (market_data_id) REFERENCES market_data(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- STEP 4: CREATE TRANSACTIONS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to assets
    asset_id BIGINT NOT NULL,
    
    -- Transaction Type
    transaction_type ENUM('BUY', 'SELL', 'DIVIDEND', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    
    -- Transaction Details
    quantity DECIMAL(15,8),
    price DECIMAL(15,4),
    transaction_date DATETIME(6) NOT NULL,
    fees DECIMAL(15,4) DEFAULT 0,
    notes TEXT,
    currency VARCHAR(10) DEFAULT 'USD',
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_asset (asset_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    
    -- Foreign Key
    CONSTRAINT fk_transactions_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- STEP 5: CREATE DIVIDENDS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS dividends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to assets
    asset_id BIGINT NOT NULL,
    
    -- Dividend Type
    dividend_type ENUM('CASH_DIVIDEND', 'STOCK_DIVIDEND', 'SPECIAL_DIVIDEND', 
                       'INTEREST_PAYMENT', 'COUPON_PAYMENT', 'SAVINGS_INTEREST', 
                       'MONEY_MARKET_INTEREST') NOT NULL,
    
    -- Payment Details
    payment_date DATE,
    ex_dividend_date DATE,
    record_date DATE,
    declaration_date DATE,
    
    -- Amount Details
    amount_per_share DECIMAL(15,8),
    shares_held DECIMAL(15,8),
    total_amount DECIMAL(15,4),
    tax_withheld DECIMAL(15,4) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'USD',
    
    -- Additional Info
    notes TEXT,
    dividend_yield DECIMAL(8,4),
    is_qualified BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_asset (asset_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_dividend_type (dividend_type),
    
    -- Foreign Key
    CONSTRAINT fk_dividends_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- STEP 6: CREATE NEWS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to Market Data
    market_data_id BIGINT,
    symbol VARCHAR(20) NOT NULL,
    
    -- News Content
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    link VARCHAR(1000),
    image_url VARCHAR(1000),
    
    -- Source Information
    source VARCHAR(50),
    publisher VARCHAR(200),
    published_date DATETIME(6),
    sentiment VARCHAR(20),
    
    -- User Flags
    is_read BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_market_data (market_data_id),
    INDEX idx_symbol (symbol),
    INDEX idx_published_date (published_date),
    INDEX idx_source (source),
    UNIQUE KEY uk_link (link(500)),
    
    -- Foreign Key
    CONSTRAINT fk_news_market_data FOREIGN KEY (market_data_id) REFERENCES market_data(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- STEP 7: UPDATE PRICE_HISTORY TABLE (ensure market_data_id column exists)
-- ============================================================================

-- Check if we need to rename the column
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'portfolio_db' 
    AND TABLE_NAME = 'price_history' 
    AND COLUMN_NAME = 'instrument_id'
);

-- Only rename if instrument_id still exists
SET @rename_col = IF(@column_exists > 0, 
    'ALTER TABLE price_history CHANGE COLUMN instrument_id market_data_id BIGINT NOT NULL',
    'SELECT "Column already renamed" AS message'
);

PREPARE stmt FROM @rename_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop old foreign key if it exists
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'portfolio_db' 
    AND TABLE_NAME = 'price_history' 
    AND CONSTRAINT_NAME = 'price_history_ibfk_1'
);

SET @drop_fk = IF(@fk_exists > 0, 
    'ALTER TABLE price_history DROP FOREIGN KEY price_history_ibfk_1',
    'SELECT "Old FK does not exist" AS message'
);

PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if new foreign key already exists
SET @new_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'portfolio_db' 
    AND TABLE_NAME = 'price_history' 
    AND CONSTRAINT_NAME = 'fk_price_history_market_data'
);

-- Only add new FK if it doesn't exist
SET @add_fk = IF(@new_fk_exists = 0, 
    'ALTER TABLE price_history ADD CONSTRAINT fk_price_history_market_data FOREIGN KEY (market_data_id) REFERENCES market_data(id) ON DELETE CASCADE',
    'SELECT "New FK already exists" AS message'
);

PREPARE stmt FROM @add_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- STEP 8: ENSURE USER_SETTINGS TABLE EXISTS
-- ============================================================================

-- Create user_settings table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- User Info
    user_name VARCHAR(100),
    
    -- Settings
    default_currency VARCHAR(3) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',
    date_format VARCHAR(20) DEFAULT 'yyyy-MM-dd',
    decimal_places INT DEFAULT 2,
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    price_alerts_enabled BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Migrate data from investor_settings if it exists
INSERT IGNORE INTO user_settings (
    id, user_name, default_currency, timezone, date_format, 
    decimal_places, theme, notifications_enabled,
    created_date, updated_date
)
SELECT 
    id, investor_name, default_currency, timezone, date_format,
    decimal_places, theme, notifications_enabled,
    created_date, updated_date
FROM investor_settings
WHERE EXISTS (SELECT 1 FROM information_schema.tables 
              WHERE table_schema = 'portfolio_db' 
              AND table_name = 'investor_settings');

-- Insert default row if table is empty
INSERT IGNORE INTO user_settings (id, user_name, default_currency, theme)
VALUES (1, 'Investor', 'USD', 'light');

-- ============================================================================
-- STEP 9: DROP OLD UNNECESSARY TABLES
-- ============================================================================

DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS instruments;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

SELECT '=== MIGRATION COMPLETE ===' AS status;

-- Check final table count
SELECT 'Table Count' AS check_type, COUNT(*) AS count 
FROM information_schema.tables 
WHERE table_schema = 'portfolio_db';

-- Check each table
SELECT 'Asset Types' AS table_name, COUNT(*) AS row_count FROM asset_types
UNION ALL
SELECT 'Market Data', COUNT(*) FROM market_data
UNION ALL
SELECT 'Assets', COUNT(*) FROM assets
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'Dividends', COUNT(*) FROM dividends
UNION ALL
SELECT 'News', COUNT(*) FROM news
UNION ALL
SELECT 'Price History', COUNT(*) FROM price_history
UNION ALL
SELECT 'User Settings', COUNT(*) FROM user_settings
UNION ALL
SELECT 'Currencies', COUNT(*) FROM currencies;

-- Show table list
SHOW TABLES;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- FINAL SCHEMA (8 tables):
-- 1. asset_types      - Reference data (STOCK, BOND, CRYPTO, etc.)
-- 2. market_data      - Master data (ALL stocks/assets - reference table)
-- 3. assets           - User's relationship (WATCHLIST or OWNED flags)
-- 4. transactions     - BUY, SELL, DIVIDEND, DEPOSIT records
-- 5. dividends        - Dividend/interest payment tracking
-- 6. news             - Company news with AI summaries  
-- 7. price_history    - Historical OHLCV data for charts
-- 8. user_settings    - User preferences
-- 9. currencies       - Reference data (USD, EUR, etc.)
-- ============================================================================
