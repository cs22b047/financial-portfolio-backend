-- =====================================================================
-- V4: Simplify to Core Schema (Based on ER Diagram)
-- =====================================================================
-- This migration simplifies the database to focus on core tables
-- Removes complex normalized schema in favor of simpler structure
-- Generated on: 2026-02-03
-- =====================================================================

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop complex tables that are not in the simplified schema (in dependency order)
DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS tax_lots;
DROP TABLE IF EXISTS price_alerts;
DROP TABLE IF EXISTS corporate_actions;
DROP TABLE IF EXISTS stock_metrics;
DROP TABLE IF EXISTS crypto_metrics;
DROP TABLE IF EXISTS bond_metrics;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS instruments;
DROP TABLE IF EXISTS cash_accounts;
DROP TABLE IF EXISTS investor_settings;
DROP TABLE IF EXISTS credit_ratings;
DROP TABLE IF EXISTS blockchains;
DROP TABLE IF EXISTS industries;
DROP TABLE IF EXISTS exchanges;
DROP TABLE IF EXISTS sectors;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- REFERENCE TABLES
-- =====================================================================

-- Asset Types (Reference Table)
CREATE TABLE IF NOT EXISTS asset_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    risk_level ENUM('HIGH','LOW','MEDIUM','VERY_HIGH','VERY_LOW') DEFAULT NULL,
    is_active TINYINT(1) DEFAULT '1',
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_type_code (code),
    KEY idx_code (code),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Currencies (Reference Table)
CREATE TABLE IF NOT EXISTS currencies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) DEFAULT NULL,
    is_crypto TINYINT(1) DEFAULT '0',
    is_active TINYINT(1) DEFAULT '1',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    decimal_places INT DEFAULT NULL,
    exchange_rate_to_usd DECIMAL(18,8) DEFAULT NULL,
    is_fiat BIT(1) DEFAULT NULL,
    rate_updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- USER SETTINGS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(100) DEFAULT NULL,
    default_currency VARCHAR(3) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',
    date_format VARCHAR(20) DEFAULT 'yyyy-MM-dd',
    decimal_places INT DEFAULT '2',
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled TINYINT(1) DEFAULT '1',
    price_alerts_enabled TINYINT(1) DEFAULT '1',
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- MARKET DATA TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS market_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_type_id BIGINT DEFAULT NULL,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    current_price DECIMAL(15,4) DEFAULT NULL,
    previous_close DECIMAL(15,4) DEFAULT NULL,
    day_change DECIMAL(15,4) DEFAULT NULL,
    day_change_percent DECIMAL(8,4) DEFAULT NULL,
    volume BIGINT DEFAULT NULL,
    day_high DECIMAL(15,4) DEFAULT NULL,
    day_low DECIMAL(15,4) DEFAULT NULL,
    week_52_high DECIMAL(15,4) DEFAULT NULL,
    week_52_low DECIMAL(15,4) DEFAULT NULL,
    market_cap BIGINT DEFAULT NULL,
    bid_price DECIMAL(15,4) DEFAULT NULL,
    ask_price DECIMAL(15,4) DEFAULT NULL,
    sector VARCHAR(100) DEFAULT NULL,
    industry VARCHAR(150) DEFAULT NULL,
    exchange VARCHAR(50) DEFAULT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    dividend_yield DECIMAL(8,4) DEFAULT NULL,
    pe_ratio DECIMAL(10,2) DEFAULT NULL,
    beta DECIMAL(8,4) DEFAULT NULL,
    eps DECIMAL(10,4) DEFAULT NULL,
    data_source VARCHAR(50) DEFAULT NULL,
    last_updated DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    market_status VARCHAR(20) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_data_symbol (symbol),
    KEY idx_symbol (symbol),
    KEY idx_sector (sector),
    KEY idx_asset_type (asset_type_id),
    KEY idx_last_updated (last_updated),
    CONSTRAINT fk_market_data_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- ASSETS TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS assets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_type_id BIGINT DEFAULT NULL,
    market_data_id BIGINT NOT NULL,
    status ENUM('OWNED','RESEARCH','SOLD','WATCHLIST') NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(200) DEFAULT NULL,
    quantity DECIMAL(15,8) DEFAULT NULL,
    purchase_price DECIMAL(15,4) DEFAULT NULL,
    current_price DECIMAL(15,4) DEFAULT NULL,
    purchase_date DATE DEFAULT NULL,
    target_price DECIMAL(15,4) DEFAULT NULL,
    added_to_watchlist_date DATE DEFAULT NULL,
    price_alerts_enabled TINYINT(1) DEFAULT '0',
    notes TEXT,
    priority_rank INT DEFAULT NULL,
    sector VARCHAR(100) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_market_data (market_data_id),
    KEY idx_status (status),
    KEY idx_symbol (symbol),
    KEY idx_asset_type (asset_type_id),
    CONSTRAINT fk_assets_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE SET NULL,
    CONSTRAINT fk_assets_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- TRANSACTIONS TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    transaction_type ENUM('BUY','DEPOSIT','DIVIDEND','SELL','TRANSFER','WITHDRAWAL') NOT NULL,
    quantity DECIMAL(15,8) DEFAULT NULL,
    price DECIMAL(15,4) DEFAULT NULL,
    transaction_date DATETIME(6) NOT NULL,
    fees DECIMAL(15,4) DEFAULT '0.0000',
    notes TEXT,
    currency VARCHAR(10) DEFAULT 'USD',
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_asset (asset_id),
    KEY idx_transaction_type (transaction_type),
    KEY idx_transaction_date (transaction_date),
    CONSTRAINT fk_transactions_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- DIVIDENDS TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS dividends (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    amount_per_share DECIMAL(15,8) NOT NULL,
    shares_held DECIMAL(15,8) DEFAULT NULL,
    total_amount DECIMAL(15,4) DEFAULT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dividend_asset_payment (asset_id, payment_date),
    KEY idx_asset (asset_id),
    KEY idx_payment_date (payment_date),
    CONSTRAINT fk_dividends_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- PRICE HISTORY TABLE (Core)
-- =====================================================================

-- Drop and recreate to remove old foreign keys
DROP TABLE IF EXISTS price_history;

CREATE TABLE price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    open_price DECIMAL(18,8) DEFAULT NULL,
    high_price DECIMAL(18,8) DEFAULT NULL,
    low_price DECIMAL(18,8) DEFAULT NULL,
    close_price DECIMAL(18,8) NOT NULL,
    adjusted_close DECIMAL(18,8) DEFAULT NULL,
    volume BIGINT DEFAULT NULL,
    source VARCHAR(50) DEFAULT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_source VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_data_price_date (market_data_id, price_date),
    KEY idx_date (price_date),
    KEY idx_price_history_date (price_date),
    KEY idx_price_history_market_data (market_data_id),
    CONSTRAINT fk_price_history_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- ESG RATINGS TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS esg_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT NOT NULL,
    symbol VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    total_score DECIMAL(5,2) DEFAULT NULL,
    total_grade VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    environment_score DECIMAL(5,2) DEFAULT NULL,
    environment_grade VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    social_score DECIMAL(5,2) DEFAULT NULL,
    social_grade VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    governance_score DECIMAL(5,2) DEFAULT NULL,
    governance_grade VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    controversy_level INT DEFAULT NULL,
    risk_level VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    data_source VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    last_updated DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_symbol (symbol),
    KEY idx_market_data (market_data_id),
    KEY idx_total_score (total_score),
    KEY idx_controversy (controversy_level),
    CONSTRAINT fk_esg_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- NEWS TABLE (Core)
-- =====================================================================

CREATE TABLE IF NOT EXISTS news (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT DEFAULT NULL,
    symbol VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    link VARCHAR(1000) DEFAULT NULL,
    image_url VARCHAR(1000) DEFAULT NULL,
    source VARCHAR(50) DEFAULT NULL,
    publisher VARCHAR(200) DEFAULT NULL,
    published_date DATETIME(6) DEFAULT NULL,
    sentiment VARCHAR(20) DEFAULT NULL,
    is_read TINYINT(1) DEFAULT '0',
    created_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_link (link(500)),
    KEY idx_market_data (market_data_id),
    KEY idx_symbol (symbol),
    KEY idx_published_date (published_date),
    KEY idx_source (source),
    CONSTRAINT fk_news_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- DATA RETENTION
-- =====================================================================
-- Note: This migration drops several complex tables. 
-- If you have existing data in those tables that needs to be preserved,
-- you should create a separate data migration script to move the data
-- to the simplified schema before running this migration.
-- =====================================================================

-- Insert default user settings if not exists
INSERT INTO user_settings (user_name, default_currency, timezone, theme)
SELECT 'Default User', 'USD', 'UTC', 'light'
WHERE NOT EXISTS (SELECT 1 FROM user_settings LIMIT 1);

-- =====================================================================
-- END OF MIGRATION
-- =====================================================================
