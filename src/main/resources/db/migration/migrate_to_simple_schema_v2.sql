-- ============================================================================
-- PORTFOLIO DATABASE SIMPLIFICATION MIGRATION V2
-- ============================================================================
-- Purpose: Simplify 19-table schema to 6-table normalized schema
-- Architecture: Master stocks table + User relationship table
-- For: Single investor portfolio tracking
-- Date: 2026-02-02
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- STEP 1: DROP UNNECESSARY TABLES (14 tables)
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

-- ============================================================================
-- STEP 2: CREATE STOCKS TABLE (Master Data - ALL Stocks)
-- ============================================================================

DROP TABLE IF EXISTS stocks;

CREATE TABLE stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Stock Identification
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    asset_type ENUM('STOCK', 'ETF', 'MUTUAL_FUND', 'BOND', 'CRYPTO', 'CASH') NOT NULL DEFAULT 'STOCK',
    
    -- Current Market Data (Auto-refreshed from Yahoo Finance/Finnhub)
    current_price DECIMAL(15,4),
    previous_close DECIMAL(15,4),
    day_change DECIMAL(15,4),
    day_change_percent DECIMAL(8,4),
    volume BIGINT,
    avg_volume BIGINT,
    market_cap BIGINT,
    
    -- Fundamental Data
    sector VARCHAR(100),
    industry VARCHAR(150),
    exchange VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'USD',
    country VARCHAR(50),
    
    -- Valuation Metrics
    pe_ratio DECIMAL(10,2),
    pb_ratio DECIMAL(10,2),
    dividend_yield DECIMAL(8,4),
    dividend_rate DECIMAL(10,4),
    beta DECIMAL(8,4),
    eps DECIMAL(10,4),
    
    -- Price Range
    week_52_high DECIMAL(15,4),
    week_52_low DECIMAL(15,4),
    day_high DECIMAL(15,4),
    day_low DECIMAL(15,4),
    
    -- Additional Info
    description TEXT,
    website VARCHAR(255),
    
    -- Timestamps
    last_updated DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_symbol (symbol),
    INDEX idx_sector (sector),
    INDEX idx_asset_type (asset_type),
    INDEX idx_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 3: CREATE USER_STOCKS TABLE (User's Relationship to Stocks)
-- ============================================================================

DROP TABLE IF EXISTS user_stocks;

CREATE TABLE user_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign Key to stocks table
    stock_id BIGINT NOT NULL,
    
    -- User's Relationship (WATCHLIST or OWNED)
    status ENUM('WATCHLIST', 'OWNED') NOT NULL DEFAULT 'WATCHLIST',
    
    -- Ownership Data (for OWNED status only)
    quantity DECIMAL(15,8) DEFAULT 0,
    average_cost_basis DECIMAL(15,4),
    total_cost_basis DECIMAL(15,2),
    unrealized_gain_loss DECIMAL(15,2),
    unrealized_gain_loss_percent DECIMAL(8,4),
    first_purchase_date DATE,
    last_transaction_date DATE,
    
    -- Watchlist Data (for WATCHLIST status)
    target_buy_price DECIMAL(15,4),
    target_sell_price DECIMAL(15,4),
    alert_enabled BOOLEAN DEFAULT FALSE,
    notes TEXT,
    
    -- Timestamps
    added_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Constraints
    UNIQUE KEY uk_stock (stock_id),
    INDEX idx_status (status),
    INDEX idx_stock_id (stock_id),
    
    -- Foreign Key
    CONSTRAINT fk_user_stocks_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 4: MIGRATE DATA FROM instruments TO stocks
-- ============================================================================

INSERT INTO stocks (
    symbol,
    name,
    asset_type,
    current_price,
    currency,
    sector,
    industry,
    created_at
)
SELECT DISTINCT
    symbol,
    name,
    COALESCE(instrument_type, 'STOCK'),
    current_price,
    COALESCE(currency_code, 'USD'),
    sector,
    industry,
    created_date
FROM instruments
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'portfolio_db' AND table_name = 'instruments')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    current_price = VALUES(current_price);

-- ============================================================================
-- STEP 5: CREATE TRANSACTIONS TABLE
-- ============================================================================

DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to user_stocks
    user_stock_id BIGINT NOT NULL,
    
    -- Transaction Type (SIMPLIFIED: Only 3 types)
    transaction_type ENUM('BUY', 'SELL', 'DIVIDEND') NOT NULL,
    
    -- Transaction Details
    quantity DECIMAL(15,8),
    price_per_unit DECIMAL(15,4),
    fees DECIMAL(15,4) DEFAULT 0,
    total_amount DECIMAL(15,2),
    transaction_date DATETIME(6) NOT NULL,
    
    -- Notes
    notes TEXT,
    
    -- Timestamps
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_user_stock (user_stock_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    
    -- Foreign Key
    CONSTRAINT fk_transactions_user_stock FOREIGN KEY (user_stock_id) REFERENCES user_stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 6: CREATE NEWS TABLE
-- ============================================================================

DROP TABLE IF EXISTS news;

CREATE TABLE news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to Stock
    stock_id BIGINT,
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
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_stock (stock_id),
    INDEX idx_symbol (symbol),
    INDEX idx_published_date (published_date),
    INDEX idx_source (source),
    UNIQUE KEY uk_link (link(500)),
    
    -- Foreign Key
    CONSTRAINT fk_news_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 7: UPDATE PRICE_HISTORY TABLE
-- ============================================================================

-- Drop existing foreign key if exists
SET @fk_name = (
    SELECT CONSTRAINT_NAME 
    FROM information_schema.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'portfolio_db' 
    AND TABLE_NAME = 'price_history' 
    AND COLUMN_NAME = 'instrument_id'
    LIMIT 1
);

SET @drop_fk = IF(@fk_name IS NOT NULL, 
    CONCAT('ALTER TABLE price_history DROP FOREIGN KEY ', @fk_name),
    'SELECT 1'
);

PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Rename column if it exists
ALTER TABLE price_history
CHANGE COLUMN instrument_id stock_id BIGINT NOT NULL;

-- Add new foreign key
ALTER TABLE price_history
ADD CONSTRAINT fk_price_history_stock 
FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 8: CREATE SETTINGS TABLE
-- ============================================================================

DROP TABLE IF EXISTS settings;

CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Essential Settings Only
    default_currency VARCHAR(3) DEFAULT 'USD',
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default row
INSERT INTO settings (id, default_currency, theme, notifications_enabled)
VALUES (1, 'USD', 'light', TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6);

-- Migrate from investor_settings if exists
UPDATE settings s
SET s.default_currency = (SELECT COALESCE(default_currency, 'USD') FROM investor_settings LIMIT 1),
    s.theme = (SELECT COALESCE(theme, 'light') FROM investor_settings LIMIT 1),
    s.notifications_enabled = (SELECT COALESCE(notifications_enabled, 1) FROM investor_settings LIMIT 1)
WHERE s.id = 1 AND EXISTS (SELECT 1 FROM investor_settings);

-- ============================================================================
-- STEP 9: SIMPLIFY CURRENCIES TABLE
-- ============================================================================

-- Backup existing data
CREATE TABLE IF NOT EXISTS currencies_temp AS
SELECT id, code, name, symbol, is_active
FROM currencies
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'portfolio_db' AND table_name = 'currencies');

DROP TABLE IF EXISTS currencies;

CREATE TABLE currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Restore data
INSERT INTO currencies (id, code, name, symbol, is_active)
SELECT id, code, name, symbol, is_active 
FROM currencies_temp
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'portfolio_db' AND table_name = 'currencies_temp');

DROP TABLE IF EXISTS currencies_temp;

-- Ensure essential currencies exist
INSERT IGNORE INTO currencies (code, name, symbol, is_active) VALUES
('USD', 'US Dollar', '$', TRUE),
('EUR', 'Euro', '€', TRUE),
('GBP', 'British Pound', '£', TRUE),
('JPY', 'Japanese Yen', '¥', TRUE),
('INR', 'Indian Rupee', '₹', TRUE);

-- ============================================================================
-- STEP 10: DROP OLD TABLES
-- ============================================================================

DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS investor_settings;
DROP TABLE IF EXISTS instruments;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check final table count
SELECT 'Table Count' AS check_type, COUNT(*) AS count 
FROM information_schema.tables 
WHERE table_schema = 'portfolio_db';

-- Check each table
SELECT 'Stocks' AS table_name, COUNT(*) AS row_count FROM stocks
UNION ALL
SELECT 'User Stocks', COUNT(*) FROM user_stocks
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'News', COUNT(*) FROM news
UNION ALL
SELECT 'Price History', COUNT(*) FROM price_history
UNION ALL
SELECT 'Settings', COUNT(*) FROM settings
UNION ALL
SELECT 'Currencies', COUNT(*) FROM currencies;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- BEFORE: 19 tables, ~380 columns
-- AFTER:  6 tables, ~120 columns
-- 
-- FINAL SCHEMA:
-- 1. stocks        - Master data (ALL stocks - reference table)
-- 2. user_stocks   - User's relationship (WATCHLIST or OWNED flags)
-- 3. transactions  - BUY, SELL, DIVIDEND records
-- 4. news          - Company news with AI summaries  
-- 5. price_history - Historical OHLCV data for charts
-- 6. settings      - User preferences (single row)
-- 7. currencies    - Reference data (USD, EUR, etc.)
--
-- WORKFLOW:
-- 1. User browses 'stocks' table (master data)
-- 2. User flags stock → INSERT into 'user_stocks' with status='WATCHLIST'
-- 3. User buys stock → UPDATE 'user_stocks' to status='OWNED' + INSERT 'transactions'
-- ============================================================================
