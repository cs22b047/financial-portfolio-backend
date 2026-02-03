-- =====================================================
-- Portfolio Tracker - Complete Database Schema
-- Version: 3.0
-- Date: 2026-02-03
-- Description: Complete schema with all tables and relationships
-- =====================================================

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS `dividends`;
DROP TABLE IF EXISTS `transactions`;
DROP TABLE IF EXISTS `assets`;
DROP TABLE IF EXISTS `market_data`;
DROP TABLE IF EXISTS `asset_types`;

-- =====================================================
-- REFERENCE DATA TABLES
-- =====================================================

-- Asset Types (STOCK, ETF, BOND, CRYPTO, etc.)
CREATE TABLE `asset_types` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `risk_level` VARCHAR(20) DEFAULT 'MEDIUM',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT `uk_asset_type_code` UNIQUE (`code`),
    INDEX `idx_asset_type_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- MASTER DATA TABLES
-- =====================================================

-- Market Data - Master table for ALL stocks/assets
CREATE TABLE `market_data` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_type_id` BIGINT,
    `symbol` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(200) NOT NULL,
    
    -- Price Information
    `current_price` DECIMAL(15,4),
    `previous_close` DECIMAL(15,4),
    `day_change` DECIMAL(15,4),
    `day_change_percent` DECIMAL(8,4),
    `volume` BIGINT,
    `day_high` DECIMAL(15,4),
    `day_low` DECIMAL(15,4),
    `week_52_high` DECIMAL(15,4),
    `week_52_low` DECIMAL(15,4),
    `market_cap` BIGINT,
    `bid_price` DECIMAL(15,4),
    `ask_price` DECIMAL(15,4),
    
    -- Company Information
    `sector` VARCHAR(100),
    `industry` VARCHAR(150),
    `exchange` VARCHAR(50),
    `currency` VARCHAR(10) DEFAULT 'USD',
    
    -- Financial Metrics
    `dividend_yield` DECIMAL(8,4),
    `pe_ratio` DECIMAL(10,2),
    `beta` DECIMAL(8,4),
    `eps` DECIMAL(10,4),
    
    -- Metadata
    `data_source` VARCHAR(50),
    `last_updated` DATETIME,
    `is_tradable` BOOLEAN DEFAULT TRUE,
    `created_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT `uk_market_data_symbol` UNIQUE (`symbol`),
    INDEX `idx_market_data_sector` (`sector`),
    INDEX `idx_market_data_last_updated` (`last_updated`),
    
    CONSTRAINT `fk_market_data_asset_type` 
        FOREIGN KEY (`asset_type_id`) 
        REFERENCES `asset_types`(`id`) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USER PORTFOLIO TABLES
-- =====================================================

-- Assets - User's relationship with stocks (WATCHLIST or OWNED)
CREATE TABLE `assets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_type_id` BIGINT,
    `market_data_id` BIGINT NOT NULL UNIQUE,
    `status` VARCHAR(20) NOT NULL DEFAULT 'WATCHLIST',
    `symbol` VARCHAR(20) NOT NULL,
    `name` VARCHAR(200),
    
    -- Ownership Details (NULL for WATCHLIST)
    `quantity` DECIMAL(15,8),
    `purchase_price` DECIMAL(15,4),
    `current_price` DECIMAL(15,4),
    `purchase_date` DATE,
    
    -- Analysis & Tracking
    `target_price` DECIMAL(15,4),
    `added_to_watchlist_date` DATE,
    `price_alerts_enabled` BOOLEAN DEFAULT FALSE,
    `notes` TEXT,
    `priority_rank` INT,
    `sector` VARCHAR(100),
    
    -- Timestamps
    `created_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT `uk_asset_market_data` UNIQUE (`market_data_id`),
    INDEX `idx_asset_status` (`status`),
    INDEX `idx_asset_symbol` (`symbol`),
    
    CONSTRAINT `fk_asset_asset_type` 
        FOREIGN KEY (`asset_type_id`) 
        REFERENCES `asset_types`(`id`) 
        ON DELETE SET NULL,
    CONSTRAINT `fk_asset_market_data` 
        FOREIGN KEY (`market_data_id`) 
        REFERENCES `market_data`(`id`) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TRANSACTION TABLES
-- =====================================================

-- Transactions - All buy/sell/dividend activities
CREATE TABLE `transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT NOT NULL,
    `transaction_type` VARCHAR(20) NOT NULL,
    `quantity` DECIMAL(15,8),
    `price` DECIMAL(15,4),
    `transaction_date` DATETIME NOT NULL,
    `fees` DECIMAL(15,4) DEFAULT 0.00,
    `notes` TEXT,
    `currency` VARCHAR(10) DEFAULT 'USD',
    
    -- Timestamps
    `created_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX `idx_transaction_asset` (`asset_id`),
    INDEX `idx_transaction_type` (`transaction_type`),
    INDEX `idx_transaction_date` (`transaction_date`),
    
    CONSTRAINT `fk_transaction_asset` 
        FOREIGN KEY (`asset_id`) 
        REFERENCES `assets`(`id`) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dividends - Dividend income tracking
CREATE TABLE `dividends` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT NOT NULL,
    `payment_date` DATE NOT NULL,
    `amount_per_share` DECIMAL(15,8) NOT NULL,
    `shares_held` DECIMAL(15,8),
    `total_amount` DECIMAL(15,4),
    `currency` VARCHAR(10) DEFAULT 'USD',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT `uk_dividend_asset_payment` UNIQUE (`asset_id`, `payment_date`),
    INDEX `idx_dividend_asset` (`asset_id`),
    INDEX `idx_dividend_payment_date` (`payment_date`),
    
    CONSTRAINT `fk_dividend_asset` 
        FOREIGN KEY (`asset_id`) 
        REFERENCES `assets`(`id`) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INITIAL REFERENCE DATA
-- =====================================================

-- Insert default asset types
INSERT INTO `asset_types` (`code`, `name`, `description`, `risk_level`, `is_active`) VALUES
('STOCK', 'Common Stock', 'Equity shares in publicly traded companies', 'MEDIUM', TRUE),
('ETF', 'Exchange-Traded Fund', 'Investment fund traded on stock exchanges', 'LOW', TRUE),
('BOND', 'Bond', 'Fixed income debt securities', 'LOW', TRUE),
('CRYPTO', 'Cryptocurrency', 'Digital or virtual currencies', 'HIGH', TRUE),
('MUTUAL_FUND', 'Mutual Fund', 'Pooled investment vehicle', 'MEDIUM', TRUE),
('REIT', 'Real Estate Investment Trust', 'Real estate investment companies', 'MEDIUM', TRUE),
('OPTION', 'Option', 'Derivative contracts', 'HIGH', TRUE),
('FUTURE', 'Future', 'Futures contracts', 'HIGH', TRUE),
('INDEX', 'Index', 'Market indices', 'LOW', TRUE),
('COMMODITY', 'Commodity', 'Physical goods or raw materials', 'HIGH', TRUE);

-- =====================================================
-- SCHEMA INFORMATION
-- =====================================================

-- Entity Relationships:
--
-- 1. asset_types (1) → (N) market_data
--    - One asset type can have many market data entries
--    - market_data.asset_type_id references asset_types.id
--
-- 2. asset_types (1) → (N) assets
--    - One asset type can have many user assets
--    - assets.asset_type_id references asset_types.id
--
-- 3. market_data (1) → (1) assets
--    - One market data entry maps to one user asset (unique relationship)
--    - assets.market_data_id references market_data.id (UNIQUE)
--
-- 4. assets (1) → (N) transactions
--    - One asset can have many transactions
--    - transactions.asset_id references assets.id
--
-- 5. assets (1) → (N) dividends
--    - One asset can have many dividend payments
--    - dividends.asset_id references assets.id
--
-- Key Design Principles:
-- - market_data: Universal stock/asset catalog (all available stocks)
-- - assets: User's specific relationship with stocks (watchlist or owned)
-- - transactions: Complete audit trail of all buy/sell activities
-- - dividends: Income tracking with auto-calculation from owned shares
-- - asset_types: Reference data for classification and risk assessment

-- =====================================================
-- END OF SCHEMA
-- =====================================================
