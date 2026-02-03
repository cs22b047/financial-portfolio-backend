-- ============================================================================
-- PORTFOLIO DATABASE SIMPLIFICATION MIGRATION
-- ============================================================================
-- Purpose: Simplify 19-table over-engineered schema to 5-table single-user schema
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
DROP TABLE IF EXISTS positions;  -- Will merge into portfolio

-- ============================================================================
-- STEP 2: CREATE SIMPLIFIED PORTFOLIO TABLE (Merge: instruments + positions)
-- ============================================================================

DROP TABLE IF EXISTS portfolio;

CREATE TABLE portfolio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Asset Identification
    instrument_type ENUM('STOCK', 'BOND', 'CASH', 'CRYPTO', 'ETF', 'MUTUAL_FUND') NOT NULL DEFAULT 'STOCK',
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    
    -- Current Market Data (from Yahoo Finance API)
    current_price DECIMAL(10,2),
    previous_close DECIMAL(10,2),
    day_change_percent DECIMAL(5,2),
    volume BIGINT,
    market_cap BIGINT,
    currency_code VARCHAR(3) DEFAULT 'USD',
    
    -- Stock-Specific (from Yahoo Finance)
    sector VARCHAR(50),
    industry VARCHAR(100),
    pe_ratio DECIMAL(8,2),
    dividend_yield DECIMAL(5,2),
    beta DECIMAL(6,3),
    week_52_high DECIMAL(10,2),
    week_52_low DECIMAL(10,2),
    
    -- Ownership Data (for OWNED status)
    quantity DECIMAL(15,4),
    average_cost_basis DECIMAL(10,2),
    total_cost_basis DECIMAL(15,2),
    first_purchase_date DA NOT NULL DEFAULT 0,
    average_cost_basis DECIMAL(10,2),
    total_cost_basis DECIMAL(15,2),
    first_purchase_date DATEe DECIMAL(5,2),
    maturity_date DATE,
    
    -- Crypto-Specific (optional)
    change_24h DECIMAL(5,2),
    change_7d DECIMAL(5,2),
    
    -- Cash-Specific (optional)
    interest_rate DECIMAL(5,2),
    bank_name VARCHAR(100),
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Indexes
    UNIQUE KEY uk_symbol_status (symbol, status),
    INDEX idx_instrument_type (instrument_type),
    INDEX idx_status (status),
    INDEX idx_symbol (symbol)
) ENGINE=InnoDB DEFAULT  (symbol),
    INDEX idx_instrument_type (instrument_type===================================================
-- STEP 3: MIGRATE DATA FROM instruments TO portfolio
-- ============================================================================

INSERT INTO portfolio (
    instrument_type, symbol, name, status,
    current_price, previous_close, day_change_percent, volume, market_cap, currency_code,
    sector, industry, pe_ratio, dividend_yield, beta, week_52_high, week_52_low,
    created_date, updated_date
)
SELECT 
    COALESCE(instrument_type, 'STOCK'),
    symbol,
    name,
    'WATCHLIST',  -- Default all to watchlist
    current_price,
    previous_close,
    day_change_percent,
    volume,
    market_cap,
    COALESCE(currency_code, 'USD'),
    NULL, NULL, NULL, NULL, NULL, NULL, NULL,  -- Will be populated by extractors
    created_date,
    updated_date
FROM instruments
WHERE symbol IS NOT NULL;

-- ============================================================================
-- STEP 4: CREATE SIMPLIFIED TRANSACTIONS TABLE
-- ============================================================================

DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to portfolio
    portfolio_id BIGINT NOT NULL,
    
    -- Transaction Details (SIMPLIFIED: Only 3 types)
    transaction_type ENUM('BUY', 'SELL', 'DIVIDEND') NOT NULL,
    quantity DECIMAL(15,4) NOT NULL,
    price_per_unit DECIMAL(10,2) NOT NULL,
    transaction_date DATE NOT NULL,
    
    -- Optional Fields
    fees DECIMAL(8,2),
    total_amount DECIMAL(15,2),
    notes TEXT,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_portfolio (portfolio_id),
    INDEX idx_date (transaction_date),
    INDEX idx_type (transaction_type),
    
    -- Foreign Key
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 5: MIGRATE DATA FROM portfolio_transactions TO transactions
-- ============================================================================

INSERT INTO transactions (
    portfolio_id, transaction_type, quantity, price_per_unit, transaction_date,
    fees, total_amount, notes, created_date
)
SELECT 
    pt.position_id,
    CASE 
        WHEN pt.transaction_type IN ('BUY', 'PURCHASE') THEN 'BUY'
        WHEN pt.transaction_type IN ('SELL', 'SALE') THEN 'SELL'
        WHEN pt.transaction_type LIKE '%DIVIDEND%' THEN 'DIVIDEND'
        ELSE 'BUY'
    END,
    pt.quantity,
    pt.price_per_unit,
    pt.transaction_date,
    pt.fees,
    pt.total_amount,
    pt.notes,
    pt.created_date
FROM portfolio_transactions pt
WHERE pt.position_id IS NOT NULL 
  AND pt.transaction_date IS NOT NULL;

-- ============================================================================
-- STEP 6: SIMPLIFY NEWS TABLE (Already exists, just clean up)
-- ============================================================================

-- News table already created earlier, just ensure it's compatible
-- DROP and RECREATE to ensure clean state

DROP TABLE IF EXISTS news;

CREATE TABLE news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Link to portfolio
    portfolio_id BIGINT NULL,
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
    
    -- User Flags
    is_read BIT DEFAULT 0,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_portfolio (portfolio_id),
    INDEX idx_symbol (symbol),
    INDEX idx_published_date (published_date),
    INDEX idx_source (source),
    UNIQUE KEY uk_link (link(500)),
    
    -- Foreign Key
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 8: KEEP PRICE_HISTORY (Rename instrument_id to stock_id)
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

-- Rename column
ALTER TABLE price_history
CHANGE COLUMN instrument_id stock_id BIGINT NOT NULL;

-- Add new foreign key
ALTER TABLE price_history
ADD CONSTRAINT fk_price_history_stock 
FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 9: SIMPLIFY SETTINGS TABLE
-- ============================================================================

DROP TABLE IF EXISTS settings;

CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Essential Settings Only
    default_currency VARCHAR(3) DEFAULT 'USD',
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BIT DEFAULT 1,
    
    -- Timestamps
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default row
INSERT INTO settings (id, default_currency, theme, notifications_enabled)
VALUES (1, 'USD', 'light', 1)
ON DUPLICATE KEY UPDATE updated_date = CURRENT_TIMESTAMP(6);

-- Migrate essential data from investor_settings if exists
UPDATE settings s
SET s.default_currency = (SELECT COALESCE(default_currency, 'USD') FROM investor_settings LIMIT 1),
    s.theme = (SELECT COALESCE(theme, 'light') FROM investor_settings LIMIT 1),
    s.notifications_enabled = (SELECT COALESCE(notifications_enabled, 1) FROM investor_settings LIMIT 1)
WHERE s.id = 1 AND EXISTS (SELECT 1 FROM investor_settings);

-- ============================================================================
-- STEP 10: DROP OLD TABLES
-- ============================================================================

DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS investor_settings;
DROP TABLE IF EXISTS instruments;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS portfolio;

-- ============================================================================
-- STEP 10: KEEP CURRENCIES (Simplify)
-- ============================================================================

-- Keep currencies but only essential columns
CREATE TABLE IF NOT EXISTS currencies_temp AS
SELECT id, code, name, symbol, is_active
FROM currencies;

DROP TABLE IF EXISTS currencies;

CREATE TABLE currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    is_active BIT DEFAULT 1,
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO currencies (id, code, name, symbol, is_active)
SELECT id, code, name, symbol, is_active FROM currencies_temp;

DROP TABLE IF EXISTS currencies_temp;

-- Ensure USD, EUR, GBP exist
INSERT IGNORE INTO currencies (code, name, symbol, is_active) VALUES
('USD', 'US Dollar', '$', 1),
('EUR', 'Euro', '€', 1),
('GBP', 'British Pound', '£', 1),
('JPY', 'Japanese Yen', '¥', 1),
('INR', 'Indian Rupee', '₹', 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check final table count
SELECT 'Table Count' AS check_type, COUNT(*) AS count 
FROM information_schema.tables 
WHERE table_schema = 'portfolio_db';

-- Check portfolio table
SELECT 'Portfolio Rows' AS check_type, COUNT(*) AS count FROM portfolio;

-- Check watchlist table
SELECT 'Watchlist Rows' AS check_type, COUNT(*) AS count FROM watchlist;

-- Check transactions table
SELECT 'Transactions Rows' AS check_type, COUNT(*) AS count FROM transactions;

-- Check watchlist table
SELECT 'Watchlist Rows' AS check_type, COUNT(*) AS count FROM watchlist;

-- Check news table
SELECT 'News Rows' AS check_type, COUNT(*) AS count FROM news;

-- Check price_history table
SELECT 'Price History Rows' AS check_type, COUNT(*) AS count FROM price_history;

-- Check settings table
SELECT 'Settings Rows' AS check_type, COUNT(*) AS count FROM settings;

-- ============================================================================
-- SUMMARY
-- ========6 tables, ~120 columns
-- 
-- FINAL TABLES:
-- 1. stocks           - Master data (ALL stocks, reference data)
-- 2. user_stocks      - User's relationship (WATCHLIST or OWNED)
-- 3. transactions     - BUY, SELL, DIVIDEND records
-- 4. news             - Company news with AI summaries  
-- 5. price_history    - Historical OHLCV data for charts
-- 6. settings         - Single row with user preferences
-- 7. currencies       - Reference data (USD, EUR, etc.)
--
-- WORKFLOW:
-- 1. User browses 'stocks' table
-- 2. User flags stock → INSERT into 'user_stocks' (status=WATCHLIST)
-- 3. User buys stock → UPDATE 'user_stocks' (status=OWNED) + INSERT 'transactions's
-- 6. settings         - Single row with user preferences
-- 7. currencies       - Reference data (USD, EUR, etc.)
-- ============================================================================
