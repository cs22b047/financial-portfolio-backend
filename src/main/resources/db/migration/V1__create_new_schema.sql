-- =====================================================================
-- V1: Create New Normalized Schema Tables
-- =====================================================================
-- Run this BEFORE V2 migration script
-- =====================================================================

-- Reference Tables
CREATE TABLE IF NOT EXISTS sectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS industries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sector_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sector_id) REFERENCES sectors(id)
);

CREATE TABLE IF NOT EXISTS exchanges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    full_name VARCHAR(200),
    exchange_type VARCHAR(20) DEFAULT 'STOCK',
    country VARCHAR(50),
    timezone VARCHAR(50),
    open_time TIME,
    close_time TIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10),
    is_crypto BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS credit_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agency VARCHAR(20) NOT NULL,
    rating VARCHAR(20) NOT NULL,
    rating_category VARCHAR(20),
    description TEXT,
    numeric_score INT,
    is_investment_grade BOOLEAN,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agency_rating (agency, rating)
);

CREATE TABLE IF NOT EXISTS blockchains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    native_token VARCHAR(20),
    consensus_type VARCHAR(30),
    is_evm_compatible BOOLEAN DEFAULT FALSE,
    avg_block_time_seconds INT,
    explorer_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Core Market Data Tables
CREATE TABLE IF NOT EXISTS instruments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200),
    current_price DECIMAL(19,8),
    previous_close DECIMAL(19,8),
    day_change DECIMAL(19,8),
    day_change_percent DECIMAL(10,4),
    day_high DECIMAL(19,8),
    day_low DECIMAL(19,8),
    day_open DECIMAL(19,8),
    week_52_high DECIMAL(19,8),
    week_52_low DECIMAL(19,8),
    volume BIGINT,
    avg_volume BIGINT,
    market_cap BIGINT,
    currency_code VARCHAR(10) DEFAULT 'USD',
    data_source VARCHAR(50),
    last_updated DATETIME,
    market_status VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sector_id BIGINT,
    industry_id BIGINT,
    exchange_id BIGINT,
    FOREIGN KEY (sector_id) REFERENCES sectors(id),
    FOREIGN KEY (industry_id) REFERENCES industries(id),
    FOREIGN KEY (exchange_id) REFERENCES exchanges(id),
    INDEX idx_symbol (symbol),
    INDEX idx_type (instrument_type),
    INDEX idx_sector (sector_id)
);

CREATE TABLE IF NOT EXISTS stock_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL UNIQUE,
    pe_ratio DECIMAL(10,4),
    forward_pe DECIMAL(10,4),
    peg_ratio DECIMAL(10,4),
    price_to_book DECIMAL(10,4),
    price_to_sales DECIMAL(10,4),
    dividend_yield DECIMAL(10,6),
    dividend_rate DECIMAL(10,4),
    ex_dividend_date DATE,
    payout_ratio DECIMAL(10,4),
    beta DECIMAL(10,4),
    earnings_per_share DECIMAL(10,4),
    revenue BIGINT,
    profit_margin DECIMAL(10,4),
    operating_margin DECIMAL(10,4),
    return_on_equity DECIMAL(10,4),
    return_on_assets DECIMAL(10,4),
    debt_to_equity DECIMAL(10,4),
    current_ratio DECIMAL(10,4),
    quick_ratio DECIMAL(10,4),
    analyst_target_price DECIMAL(19,4),
    analyst_rating VARCHAR(20),
    short_ratio DECIMAL(10,4),
    shares_outstanding BIGINT,
    float_shares BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bond_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL UNIQUE,
    coupon_rate DECIMAL(10,6),
    coupon_frequency VARCHAR(20),
    maturity_date DATE,
    issue_date DATE,
    face_value DECIMAL(19,4),
    yield_to_maturity DECIMAL(10,6),
    current_yield DECIMAL(10,6),
    duration DECIMAL(10,4),
    modified_duration DECIMAL(10,4),
    convexity DECIMAL(10,4),
    credit_rating_id BIGINT,
    issuer VARCHAR(200),
    bond_type VARCHAR(50),
    is_callable BOOLEAN DEFAULT FALSE,
    call_date DATE,
    call_price DECIMAL(19,4),
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE,
    FOREIGN KEY (credit_rating_id) REFERENCES credit_ratings(id)
);

CREATE TABLE IF NOT EXISTS crypto_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL UNIQUE,
    blockchain_id BIGINT,
    contract_address VARCHAR(100),
    max_supply DECIMAL(30,8),
    circulating_supply DECIMAL(30,8),
    total_supply DECIMAL(30,8),
    market_cap_rank INT,
    volume_24h DECIMAL(19,4),
    change_24h DECIMAL(10,4),
    change_7d DECIMAL(10,4),
    change_30d DECIMAL(10,4),
    all_time_high DECIMAL(19,8),
    all_time_high_date DATETIME,
    all_time_low DECIMAL(19,8),
    all_time_low_date DATETIME,
    staking_apy DECIMAL(10,4),
    is_stakeable BOOLEAN DEFAULT FALSE,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE,
    FOREIGN KEY (blockchain_id) REFERENCES blockchains(id)
);

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    open_price DECIMAL(19,8),
    high_price DECIMAL(19,8),
    low_price DECIMAL(19,8),
    close_price DECIMAL(19,8),
    adjusted_close DECIMAL(19,8),
    volume BIGINT,
    source VARCHAR(50),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE,
    UNIQUE KEY uk_instrument_date (instrument_id, price_date),
    INDEX idx_date (price_date)
);

-- Portfolio Tables
CREATE TABLE IF NOT EXISTS positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    total_quantity DECIMAL(19,8) DEFAULT 0,
    average_cost_basis DECIMAL(19,8),
    total_cost_basis DECIMAL(19,4),
    first_purchase_date DATE,
    last_purchase_date DATE,
    target_buy_price DECIMAL(19,8),
    target_sell_price DECIMAL(19,8),
    stop_loss_price DECIMAL(19,8),
    added_to_watchlist_date DATE,
    priority_rank INT,
    notes TEXT,
    alerts_enabled BOOLEAN DEFAULT FALSE,
    cost_basis_method VARCHAR(20) DEFAULT 'FIFO',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id),
    INDEX idx_status (status),
    INDEX idx_instrument (instrument_id)
);

CREATE TABLE IF NOT EXISTS tax_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_id BIGINT NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_price DECIMAL(19,8) NOT NULL,
    original_quantity DECIMAL(19,8) NOT NULL,
    remaining_quantity DECIMAL(19,8) NOT NULL,
    cost_basis_per_unit DECIMAL(19,8) NOT NULL,
    total_cost_basis DECIMAL(19,4),
    fees DECIMAL(19,4),
    transaction_id BIGINT,
    is_wash_sale BOOLEAN DEFAULT FALSE,
    wash_sale_adjustment DECIMAL(19,4),
    disallowed_loss DECIMAL(19,4),
    split_adjusted BOOLEAN DEFAULT FALSE,
    split_ratio VARCHAR(20),
    original_lot_id BIGINT,
    is_closed BOOLEAN DEFAULT FALSE,
    close_date DATE,
    close_price DECIMAL(19,8),
    realized_gain_loss DECIMAL(19,4),
    is_long_term BOOLEAN,
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
    FOREIGN KEY (original_lot_id) REFERENCES tax_lots(id),
    INDEX idx_position (position_id),
    INDEX idx_purchase_date (purchase_date),
    INDEX idx_is_closed (is_closed)
);

CREATE TABLE IF NOT EXISTS cash_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) DEFAULT 'CHECKING',
    institution VARCHAR(100),
    account_number_last4 VARCHAR(4),
    currency_code VARCHAR(10) DEFAULT 'USD',
    current_balance DECIMAL(19,4) DEFAULT 0,
    available_balance DECIMAL(19,4) DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS portfolio_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_id BIGINT,
    cash_account_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    settlement_date DATE,
    quantity DECIMAL(19,8),
    price_per_unit DECIMAL(19,8),
    fees DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    currency_code VARCHAR(10) DEFAULT 'USD',
    exchange_rate DECIMAL(19,8),
    cost_basis_method VARCHAR(20),
    realized_gain_loss DECIMAL(19,4),
    is_long_term BOOLEAN,
    notes TEXT,
    external_transaction_id VARCHAR(100),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(id),
    FOREIGN KEY (cash_account_id) REFERENCES cash_accounts(id),
    INDEX idx_position (position_id),
    INDEX idx_date (transaction_date),
    INDEX idx_type (transaction_type)
);

CREATE TABLE IF NOT EXISTS income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_id BIGINT,
    cash_account_id BIGINT,
    income_type VARCHAR(30) NOT NULL,
    payment_date DATE NOT NULL,
    ex_date DATE,
    record_date DATE,
    amount_per_unit DECIMAL(19,8),
    units_held DECIMAL(19,8),
    gross_amount DECIMAL(19,4) NOT NULL,
    net_amount DECIMAL(19,4),
    tax_withheld DECIMAL(19,4),
    currency_code VARCHAR(10) DEFAULT 'USD',
    exchange_rate DECIMAL(19,8),
    is_qualified BOOLEAN,
    is_reinvested BOOLEAN DEFAULT FALSE,
    reinvested_shares DECIMAL(19,8),
    reinvested_price DECIMAL(19,8),
    notes TEXT,
    external_id VARCHAR(100),
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(id),
    FOREIGN KEY (cash_account_id) REFERENCES cash_accounts(id),
    INDEX idx_position (position_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_type (income_type)
);

CREATE TABLE IF NOT EXISTS corporate_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT NOT NULL,
    position_id BIGINT,
    action_type VARCHAR(30) NOT NULL,
    announcement_date DATE,
    ex_date DATE,
    record_date DATE,
    effective_date DATE NOT NULL,
    ratio_numerator DECIMAL(10,4),
    ratio_denominator DECIMAL(10,4),
    cash_amount DECIMAL(19,4),
    new_symbol VARCHAR(20),
    new_instrument_id BIGINT,
    description TEXT,
    is_processed BOOLEAN DEFAULT FALSE,
    processed_date DATETIME,
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id),
    FOREIGN KEY (new_instrument_id) REFERENCES instruments(id),
    INDEX idx_instrument (instrument_id),
    INDEX idx_effective_date (effective_date)
);

CREATE TABLE IF NOT EXISTS price_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_id BIGINT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    target_price DECIMAL(19,8),
    percent_change DECIMAL(10,4),
    is_active BOOLEAN DEFAULT TRUE,
    is_triggered BOOLEAN DEFAULT FALSE,
    triggered_at DATETIME,
    triggered_price DECIMAL(19,8),
    notification_sent BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
    INDEX idx_position (position_id),
    INDEX idx_active (is_active)
);

CREATE TABLE IF NOT EXISTS investor_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investor_name VARCHAR(100) DEFAULT 'Investor',
    default_currency VARCHAR(10) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'America/New_York',
    date_format VARCHAR(20) DEFAULT 'MM/dd/yyyy',
    decimal_places INT DEFAULT 2,
    theme VARCHAR(20) DEFAULT 'light',
    default_cost_basis_method VARCHAR(20) DEFAULT 'FIFO',
    short_term_tax_rate DECIMAL(5,4),
    long_term_tax_rate DECIMAL(5,4),
    state_tax_rate DECIMAL(5,4),
    wash_sale_tracking_enabled BOOLEAN DEFAULT TRUE,
    auto_lot_selection BOOLEAN DEFAULT TRUE,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    email_alerts BOOLEAN DEFAULT FALSE,
    email_address VARCHAR(255),
    auto_refresh_enabled BOOLEAN DEFAULT TRUE,
    refresh_interval_minutes INT DEFAULT 5,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default investor settings
INSERT INTO investor_settings (investor_name, default_currency, timezone, default_cost_basis_method)
VALUES ('Investor', 'USD', 'America/New_York', 'FIFO')
ON DUPLICATE KEY UPDATE updated_date = NOW();

-- Insert common currencies
INSERT INTO currencies (code, name, symbol, is_crypto) VALUES
('USD', 'US Dollar', '$', FALSE),
('EUR', 'Euro', '€', FALSE),
('GBP', 'British Pound', '£', FALSE),
('JPY', 'Japanese Yen', '¥', FALSE),
('BTC', 'Bitcoin', '₿', TRUE),
('ETH', 'Ethereum', 'Ξ', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert common blockchains
INSERT INTO blockchains (code, name, native_token, consensus_type, is_evm_compatible) VALUES
('BITCOIN', 'Bitcoin', 'BTC', 'PROOF_OF_WORK', FALSE),
('ETHEREUM', 'Ethereum', 'ETH', 'PROOF_OF_STAKE', TRUE),
('SOLANA', 'Solana', 'SOL', 'PROOF_OF_STAKE', FALSE),
('POLYGON', 'Polygon', 'MATIC', 'PROOF_OF_STAKE', TRUE),
('AVALANCHE', 'Avalanche', 'AVAX', 'PROOF_OF_STAKE', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);
