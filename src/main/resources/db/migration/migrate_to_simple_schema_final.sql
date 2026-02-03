-- ============================================================================
-- PORTFOLIO DATABASE MIGRATION - FINAL VERSION
-- ============================================================================
-- Purpose: Simplify 19-table schema using original table names
-- Architecture: market_data (master) + assets (user relationship)
-- For: Single investor portfolio tracking
-- Date: 2026-02-02
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- STEP 1: DROP UNNECESSARY TABLES
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
-- STEP 2: CREATE ASSET_TYPES TABLE (Reference Data)
-- ============================================================================

DROP TABLE IF EXISTS asset_types;

CREATE TABLE asset_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    risk_level ENUM('VERY_LOW', 'LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH') DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    INDEX idx_code (code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default asset types
INSERT INTO asset_types (code, name, description, risk_level, is_active) VALUES
('STOCK', 'Stocks', 'Equity securities representing ownership in public companies', 'MEDIUM', TRUE),
('BOND', 'Bonds', 'Fixed-income debt securities', 'LOW', TRUE),
('ETF', 'ETFs', 'Exchange-traded funds', 'MEDIUM', TRUE),
('MUTUAL_FUND', 'Mutual Funds', 'Pooled investment funds', 'MEDIUM', TRUE),
('CRYPTO', 'Cryptocurrency', 'Digital assets and cryptocurrencies', 'VERY_HIGH', TRUE),
('CASH', 'Cash & Equivalents', 'Liquid assets including savings accounts', 'VERY_LOW', TRUE);

-- ============================================================================
-- STEP 3: CREATE MARKET_DATA TABLE (Master Data - ALL Stocks/Assets)
-- ============================================================================

DROP TABLE IF EXISTS market_data;

CREATE TABLE market_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Asset Classification
    asset_type_id BIGINT,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    
    -- Current Market Data
    current_price DECIMAL(15,4),
    previous_close DECIMAL(15,4),
    day_change DECIMAL(15,4),
    day_change_percent DECIMAL(8,4),
    volume BIGINT,
    day_high DECIMAL(15,4),
    day_low DECIMAL(15,4),
    week_52_high DECIMAL(15,4),
    week_52_low DECIMAL(15,4),
    market_cap BIGINT,
    
    -- Trading Data
    bid_price DECIMAL(15,4),
    ask_price DECIMAL(15,4),
    
    -- Fundamental Data
    sector VARCHAR(100),
    industry VARCHAR(150),
    exchange VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'USD',
    
    -- Valuation Metrics
    dividend_yield DECIMAL(8,4),
    pe_ratio DECIMAL(10,2),
    beta DECIMAL(8,4),
    eps DECIMAL(10,4),
    
    -- Additional Info
    data_source VARCHAR(50),
    last_updated DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    market_status VARCHAR(20),
    
    -- Timestamps
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_symbol (symbol),
    INDEX idx_sector (sector),
    INDEX idx_asset_type (asset_type_id),
    INDEX idx_last_updated (last_updated),
    
    -- Foreign Key
    CONSTRAINT fk_market_data_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 4: MIGRATE DATA TO market_data
-- ============================================================================

-- Migrate from instruments table if exists
INSERT INTO market_data (
    asset_type_id,
    symbol,
    name,
    current_price,
    currency,
    data_source,
    last_updated,
    market_status,
    created_date,
    updated_date
)
SELECT 
    (SELECT id FROM asset_types WHERE code = UPPER(i.instrument_type) LIMIT 1),
    i.symbol,
    i.name,
    i.current_price,
    COALESCE(i.currency_code, 'USD'),
    i.data_source,
    i.last_updated,
    i.market_status,
    i.created_date,
    i.updated_date
FROM instruments i
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'portfolio_db' AND table_name = 'instruments')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    current_price = VALUES(current_price),
    last_updated = VALUES(last_updated);

-- ============================================================================
-- STEP 5: CREATE ASSETS TABLE (User's Relationship to Market Data)
-- ============================================================================

DROP TABLE IF EXISTS assets;

CREATE TABLE assets (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 6: CREATE TRANSACTIONS TABLE
-- ============================================================================

DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 7: CREATE DIVIDENDS TABLE
-- ============================================================================

DROP TABLE IF EXISTS dividends;

CREATE TABLE dividends (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 8: CREATE NEWS TABLE
-- ============================================================================

DROP TABLE IF EXISTS news;

CREATE TABLE news (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 9: UPDATE PRICE_HISTORY TABLE
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
CHANGE COLUMN instrument_id market_data_id BIGINT NOT NULL;

-- Add new foreign key
ALTER TABLE price_history
ADD CONSTRAINT fk_price_history_market_data 
FOREIGN KEY (market_data_id) REFERENCES market_data(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 10: CREATE USER_SETTINGS TABLE
-- ============================================================================

DROP TABLE IF EXISTS user_settings;

CREATE TABLE user_settings (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default row
INSERT INTO user_settings (id, user_name, default_currency, theme)
VALUES (1, 'Investor', 'USD', 'light')
ON DUPLICATE KEY UPDATE updated_date = CURRENT_TIMESTAMP(6);

-- Migrate from investor_settings if exists
UPDATE user_settings us
SET us.default_currency = (SELECT COALESCE(default_currency, 'USD') FROM investor_settings LIMIT 1),
    us.theme = (SELECT COALESCE(theme, 'light') FROM investor_settings LIMIT 1),
    us.notifications_enabled = (SELECT COALESCE(notifications_enabled, 1) FROM investor_settings LIMIT 1)
WHERE us.id = 1 AND EXISTS (SELECT 1 FROM investor_settings);

-- ============================================================================
-- STEP 11: SIMPLIFY CURRENCIES TABLE
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
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
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
-- STEP 12: DROP OLD TABLES
-- ============================================================================

DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS investor_settings;
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

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- BEFORE: 19 tables, ~380 columns
-- AFTER:  9 tables, matching original schema names
-- 
-- FINAL SCHEMA:
-- 1. asset_types      - Reference data (STOCK, BOND, CRYPTO, etc.)
-- 2. market_data      - Master data (ALL stocks/assets - reference table)
-- 3. assets           - User's relationship (WATCHLIST or OWNED flags)
-- 4. transactions     - BUY, SELL, DIVIDEND, DEPOSIT records
-- 5. dividends        - Dividend/interest payment tracking
-- 6. news             - Company news with AI summaries  
-- 7. price_history    - Historical OHLCV data for charts
-- 8. user_settings    - User preferences
-- 9. currencies       - Reference data (USD, EUR, etc.)
--
-- WORKFLOW:
-- 1. User browses 'market_data' table (master data)
-- 2. User flags stock → INSERT into 'assets' with status='WATCHLIST'
-- 3. User buys stock → UPDATE 'assets' to status='OWNED' + INSERT 'transactions'
-- 4. Dividends recorded → INSERT into 'dividends' table
-- ============================================================================
