-- =====================================================================
-- V3: Current Schema Snapshot
-- =====================================================================
-- This migration represents the complete current database schema
-- Generated on: 2026-02-03
-- Database: portfolio_db
-- =====================================================================

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS portfolio_transactions;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS tax_lots;
DROP TABLE IF EXISTS price_alerts;
DROP TABLE IF EXISTS price_history;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS dividends;
DROP TABLE IF EXISTS corporate_actions;
DROP TABLE IF EXISTS stock_metrics;
DROP TABLE IF EXISTS crypto_metrics;
DROP TABLE IF EXISTS bond_metrics;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS esg_ratings;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS assets;
DROP TABLE IF EXISTS instruments;
DROP TABLE IF EXISTS market_data;
DROP TABLE IF EXISTS cash_accounts;
DROP TABLE IF EXISTS investor_settings;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS credit_ratings;
DROP TABLE IF EXISTS blockchains;
DROP TABLE IF EXISTS industries;
DROP TABLE IF EXISTS exchanges;
DROP TABLE IF EXISTS sectors;
DROP TABLE IF EXISTS currencies;
DROP TABLE IF EXISTS asset_types;

-- =====================================================================
-- REFERENCE TABLES
-- =====================================================================

-- Asset Types
CREATE TABLE asset_types (
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
    KEY idx_is_active (is_active),
    KEY idx_asset_type_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Currencies
CREATE TABLE currencies (
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

-- Sectors
CREATE TABLE sectors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    description TEXT,
    is_active BIT(1) DEFAULT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKppjndtw48toeogtpelv6p7lqs (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Industries
CREATE TABLE industries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    description TEXT,
    is_active BIT(1) DEFAULT NULL,
    name VARCHAR(100) NOT NULL,
    sector_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK9aqrfmkdrpb30rfj8pfn4mogx (code),
    KEY FK5xjvrggwilnv0kwk9pt83e58e (sector_id),
    CONSTRAINT FK5xjvrggwilnv0kwk9pt83e58e FOREIGN KEY (sector_id) REFERENCES sectors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Exchanges
CREATE TABLE exchanges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    country VARCHAR(3) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    exchange_type ENUM('BOND','COMMODITY','CRYPTO','DERIVATIVES','FOREX','OTC','STOCK') DEFAULT NULL,
    is_active BIT(1) DEFAULT NULL,
    market_close TIME DEFAULT NULL,
    market_open TIME DEFAULT NULL,
    mic_code VARCHAR(10) DEFAULT NULL,
    name VARCHAR(100) NOT NULL,
    timezone VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK6ivebaeumsyuvxt9htpeeqxy4 (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Blockchains
CREATE TABLE blockchains (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chain_id BIGINT DEFAULT NULL,
    code VARCHAR(20) NOT NULL,
    consensus_type ENUM('BYZANTINE_FAULT_TOLERANT','DELEGATED_POS','PROOF_OF_AUTHORITY','PROOF_OF_HISTORY','PROOF_OF_STAKE','PROOF_OF_WORK') DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    explorer_url VARCHAR(200) DEFAULT NULL,
    is_active BIT(1) DEFAULT NULL,
    name VARCHAR(50) NOT NULL,
    native_token VARCHAR(20) DEFAULT NULL,
    supports_smart_contracts BIT(1) DEFAULT NULL,
    supports_staking BIT(1) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKeq6wutfgcoxx29jiym296jasy (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Credit Ratings
CREATE TABLE credit_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agency ENUM('DBRS','FITCH','KROLL','MOODYS','SP') NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    description TEXT,
    rating VARCHAR(10) NOT NULL,
    rating_category ENUM('DEFAULT','INVESTMENT_GRADE','SPECULATIVE') DEFAULT NULL,
    rating_rank INT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK23dhglmctwphv4jg201t121gl (agency, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- CORE MARKET DATA TABLES
-- =====================================================================

-- Market Data (Legacy)
CREATE TABLE market_data (
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
    KEY idx_market_data_sector (sector),
    KEY idx_market_data_last_updated (last_updated),
    CONSTRAINT fk_market_data_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Instruments (New Schema)
CREATE TABLE instruments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_date DATETIME(6) DEFAULT NULL,
    currency_code VARCHAR(10) DEFAULT NULL,
    current_price DECIMAL(18,8) DEFAULT NULL,
    data_source VARCHAR(50) DEFAULT NULL,
    day_change DECIMAL(18,8) DEFAULT NULL,
    day_change_percent DECIMAL(8,4) DEFAULT NULL,
    day_high DECIMAL(18,8) DEFAULT NULL,
    day_low DECIMAL(18,8) DEFAULT NULL,
    instrument_type ENUM('BOND','COMMODITY','CRYPTO','ETF','FOREX','FUTURE','MUTUAL_FUND','OPTION','STOCK','TREASURY') NOT NULL,
    is_active BIT(1) DEFAULT NULL,
    last_updated DATETIME(6) DEFAULT NULL,
    market_cap BIGINT DEFAULT NULL,
    market_status VARCHAR(20) DEFAULT NULL,
    name VARCHAR(200) NOT NULL,
    previous_close DECIMAL(18,8) DEFAULT NULL,
    symbol VARCHAR(20) NOT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    volume BIGINT DEFAULT NULL,
    week_52_high DECIMAL(18,8) DEFAULT NULL,
    week_52_low DECIMAL(18,8) DEFAULT NULL,
    exchange_id BIGINT DEFAULT NULL,
    industry_id BIGINT DEFAULT NULL,
    sector_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK1c76bvmq7y3kw65kbn353ujlm (instrument_type, symbol),
    KEY FKiapwmub13qniuu080ttm14cfo (exchange_id),
    KEY FKqarx3hy46523l50lm44v1jgub (industry_id),
    KEY FKmedk1sttgk1odqdysmnp1j8ye (sector_id),
    CONSTRAINT FKiapwmub13qniuu080ttm14cfo FOREIGN KEY (exchange_id) REFERENCES exchanges (id),
    CONSTRAINT FKmedk1sttgk1odqdysmnp1j8ye FOREIGN KEY (sector_id) REFERENCES sectors (id),
    CONSTRAINT FKqarx3hy46523l50lm44v1jgub FOREIGN KEY (industry_id) REFERENCES industries (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- ASSET MANAGEMENT TABLES
-- =====================================================================

-- Assets (Legacy)
CREATE TABLE assets (
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
    KEY idx_asset_status (status),
    KEY idx_asset_symbol (symbol),
    CONSTRAINT fk_assets_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE SET NULL,
    CONSTRAINT fk_assets_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Positions (New Schema)
CREATE TABLE positions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    added_to_watchlist_date DATE DEFAULT NULL,
    alerts_enabled BIT(1) DEFAULT NULL,
    average_cost_basis DECIMAL(18,8) DEFAULT NULL,
    cost_basis_method ENUM('AVERAGE_COST','FIFO','HIFO','LIFO','LIFO_BY_LOT','SPECIFIC_ID') DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    first_purchase_date DATE DEFAULT NULL,
    last_purchase_date DATE DEFAULT NULL,
    notes TEXT,
    priority_rank INT DEFAULT NULL,
    status ENUM('CLOSED','OPEN','PENDING_BUY','PENDING_SELL','RESEARCH','WATCHLIST') NOT NULL,
    target_buy_price DECIMAL(18,8) DEFAULT NULL,
    target_sell_price DECIMAL(18,8) DEFAULT NULL,
    total_cost_basis DECIMAL(18,2) DEFAULT NULL,
    total_quantity DECIMAL(18,8) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    instrument_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK76b4x8081kgrl0eo1l23my3yb (instrument_id, status),
    CONSTRAINT FKdrv1t4vrxo7n27fs6a4w3pv6l FOREIGN KEY (instrument_id) REFERENCES instruments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- FINANCIAL METRICS TABLES
-- =====================================================================

-- Stock Metrics
CREATE TABLE stock_metrics (
    instrument_id BIGINT NOT NULL,
    analyst_rating VARCHAR(20) DEFAULT NULL,
    analyst_target_price DECIMAL(10,2) DEFAULT NULL,
    beta DECIMAL(8,4) DEFAULT NULL,
    dividend_per_share DECIMAL(10,4) DEFAULT NULL,
    dividend_yield DECIMAL(6,3) DEFAULT NULL,
    earnings_per_share DECIMAL(10,4) DEFAULT NULL,
    enterprise_value BIGINT DEFAULT NULL,
    ev_to_ebitda DECIMAL(10,2) DEFAULT NULL,
    ex_dividend_date DATE DEFAULT NULL,
    float_shares BIGINT DEFAULT NULL,
    forward_pe DECIMAL(10,2) DEFAULT NULL,
    gross_margin DECIMAL(6,2) DEFAULT NULL,
    operating_margin DECIMAL(6,2) DEFAULT NULL,
    payout_ratio DECIMAL(6,2) DEFAULT NULL,
    pe_ratio DECIMAL(10,2) DEFAULT NULL,
    peg_ratio DECIMAL(8,3) DEFAULT NULL,
    price_to_book DECIMAL(10,2) DEFAULT NULL,
    price_to_sales DECIMAL(10,2) DEFAULT NULL,
    profit_margin DECIMAL(6,2) DEFAULT NULL,
    return_on_assets DECIMAL(6,2) DEFAULT NULL,
    return_on_equity DECIMAL(6,2) DEFAULT NULL,
    revenue BIGINT DEFAULT NULL,
    shares_outstanding BIGINT DEFAULT NULL,
    short_percent_of_float DECIMAL(6,2) DEFAULT NULL,
    short_ratio DECIMAL(8,2) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    volatility_52w DECIMAL(6,2) DEFAULT NULL,
    PRIMARY KEY (instrument_id),
    CONSTRAINT FKd6yee2mfxr3w50a9tekshdp0f FOREIGN KEY (instrument_id) REFERENCES instruments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bond Metrics
CREATE TABLE bond_metrics (
    instrument_id BIGINT NOT NULL,
    bond_type ENUM('AGENCY','ASSET_BACKED','CONVERTIBLE','CORPORATE','HIGH_YIELD','INFLATION_LINKED','INVESTMENT_GRADE','MORTGAGE_BACKED','MUNICIPAL','TREASURY','ZERO_COUPON') DEFAULT NULL,
    call_date DATE DEFAULT NULL,
    call_price DECIMAL(10,4) DEFAULT NULL,
    convexity DECIMAL(10,4) DEFAULT NULL,
    coupon_frequency ENUM('ANNUAL','MONTHLY','QUARTERLY','SEMI_ANNUAL','ZERO_COUPON') DEFAULT NULL,
    coupon_rate DECIMAL(6,3) DEFAULT NULL,
    credit_rating_string VARCHAR(10) DEFAULT NULL,
    current_yield DECIMAL(8,4) DEFAULT NULL,
    duration DECIMAL(8,4) DEFAULT NULL,
    face_value DECIMAL(15,2) DEFAULT NULL,
    is_callable BIT(1) DEFAULT NULL,
    is_puttable BIT(1) DEFAULT NULL,
    issue_date DATE DEFAULT NULL,
    issuer VARCHAR(200) DEFAULT NULL,
    macaulay_duration DECIMAL(8,4) DEFAULT NULL,
    maturity_date DATE DEFAULT NULL,
    put_date DATE DEFAULT NULL,
    put_price DECIMAL(10,4) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    yield_to_call DECIMAL(8,4) DEFAULT NULL,
    yield_to_maturity DECIMAL(8,4) DEFAULT NULL,
    yield_to_worst DECIMAL(8,4) DEFAULT NULL,
    credit_rating_id BIGINT DEFAULT NULL,
    PRIMARY KEY (instrument_id),
    KEY FKnlmjad10s0524wnpaq957dfs3 (credit_rating_id),
    CONSTRAINT FKasgx2ma297lhq4lwudevdeb9x FOREIGN KEY (instrument_id) REFERENCES instruments (id),
    CONSTRAINT FKnlmjad10s0524wnpaq957dfs3 FOREIGN KEY (credit_rating_id) REFERENCES credit_ratings (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Crypto Metrics
CREATE TABLE crypto_metrics (
    instrument_id BIGINT NOT NULL,
    ath DECIMAL(18,8) DEFAULT NULL,
    atl DECIMAL(18,8) DEFAULT NULL,
    ath_change_percent DECIMAL(10,2) DEFAULT NULL,
    ath_date DATETIME(6) DEFAULT NULL,
    atl_date DATETIME(6) DEFAULT NULL,
    change_1h DECIMAL(10,4) DEFAULT NULL,
    change_1y DECIMAL(10,4) DEFAULT NULL,
    change_24h DECIMAL(10,4) DEFAULT NULL,
    change_30d DECIMAL(10,4) DEFAULT NULL,
    change_7d DECIMAL(10,4) DEFAULT NULL,
    circulating_supply DECIMAL(30,0) DEFAULT NULL,
    contract_address VARCHAR(100) DEFAULT NULL,
    fully_diluted_valuation BIGINT DEFAULT NULL,
    github_stars INT DEFAULT NULL,
    is_stakeable BIT(1) DEFAULT NULL,
    lock_period_days INT DEFAULT NULL,
    market_cap_rank INT DEFAULT NULL,
    max_supply DECIMAL(30,0) DEFAULT NULL,
    min_stake_amount DECIMAL(20,8) DEFAULT NULL,
    reddit_subscribers BIGINT DEFAULT NULL,
    staking_apy DECIMAL(8,4) DEFAULT NULL,
    token_standard VARCHAR(20) DEFAULT NULL,
    total_supply DECIMAL(30,0) DEFAULT NULL,
    total_value_locked DECIMAL(20,2) DEFAULT NULL,
    twitter_followers BIGINT DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    volume_24h DECIMAL(20,2) DEFAULT NULL,
    volume_change_24h DECIMAL(8,2) DEFAULT NULL,
    blockchain_id BIGINT DEFAULT NULL,
    PRIMARY KEY (instrument_id),
    KEY FKekhc60qcsggby9af7y7jiwwdx (blockchain_id),
    CONSTRAINT FKekhc60qcsggby9af7y7jiwwdx FOREIGN KEY (blockchain_id) REFERENCES blockchains (id),
    CONSTRAINT FKpwhand7j6foa7uwaijct7lkii FOREIGN KEY (instrument_id) REFERENCES instruments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- TRANSACTION TABLES
-- =====================================================================

-- Transactions (Legacy)
CREATE TABLE transactions (
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
    KEY idx_transaction_asset (asset_id),
    CONSTRAINT fk_transactions_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tax Lots
CREATE TABLE tax_lots (
    id BIGINT NOT NULL AUTO_INCREMENT,
    adjustment_factor DECIMAL(10,6) DEFAULT NULL,
    buy_transaction_id BIGINT DEFAULT NULL,
    closed_date DATE DEFAULT NULL,
    cost_basis_per_unit DECIMAL(18,8) NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    fees DECIMAL(10,2) DEFAULT NULL,
    is_closed BIT(1) DEFAULT NULL,
    is_wash_sale BIT(1) DEFAULT NULL,
    notes TEXT,
    original_quantity DECIMAL(18,8) NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_price DECIMAL(18,8) NOT NULL,
    remaining_quantity DECIMAL(18,8) NOT NULL,
    split_adjusted BIT(1) DEFAULT NULL,
    total_cost_basis DECIMAL(18,2) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    wash_sale_adjusted_basis DECIMAL(18,8) DEFAULT NULL,
    wash_sale_disallowed_loss DECIMAL(10,2) DEFAULT NULL,
    position_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_tax_lots_position (position_id),
    KEY idx_tax_lots_purchase_date (purchase_date),
    CONSTRAINT FKkxjslguihn6ctlcoq0t49kxxv FOREIGN KEY (position_id) REFERENCES positions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Portfolio Transactions (New Schema)
CREATE TABLE portfolio_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    blockchain_tx_hash VARCHAR(100) DEFAULT NULL,
    broker VARCHAR(50) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    currency_code VARCHAR(10) DEFAULT NULL,
    exchange_rate DECIMAL(12,6) DEFAULT NULL,
    fees DECIMAL(10,2) DEFAULT NULL,
    gas_fee DECIMAL(18,8) DEFAULT NULL,
    is_long_term BIT(1) DEFAULT NULL,
    notes TEXT,
    order_id VARCHAR(50) DEFAULT NULL,
    price_per_unit DECIMAL(18,8) NOT NULL,
    quantity DECIMAL(18,8) NOT NULL,
    realized_gain_loss DECIMAL(18,2) DEFAULT NULL,
    total_amount DECIMAL(18,2) DEFAULT NULL,
    transaction_date DATE NOT NULL,
    transaction_type ENUM('BUY','DEPOSIT','SELL','TRANSFER_IN','TRANSFER_OUT','WITHDRAWAL') NOT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    position_id BIGINT NOT NULL,
    tax_lot_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_transactions_position (position_id),
    KEY idx_transactions_date (transaction_date),
    KEY FK5omfufdt7aywtrbuboe8djidj (tax_lot_id),
    CONSTRAINT FK5omfufdt7aywtrbuboe8djidj FOREIGN KEY (tax_lot_id) REFERENCES tax_lots (id),
    CONSTRAINT FKakm100k0agv4gaxuu4yrhgjrg FOREIGN KEY (position_id) REFERENCES positions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- INCOME TABLES
-- =====================================================================

-- Dividends (Legacy)
CREATE TABLE dividends (
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
    KEY idx_dividend_asset (asset_id),
    KEY idx_dividend_payment_date (payment_date),
    CONSTRAINT fk_dividends_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Income (New Schema)
CREATE TABLE income (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount_per_unit DECIMAL(12,6) DEFAULT NULL,
    blockchain_tx_hash VARCHAR(100) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    currency_code VARCHAR(10) DEFAULT NULL,
    ex_date DATE DEFAULT NULL,
    gross_amount DECIMAL(18,2) NOT NULL,
    income_type ENUM('ACCRUED_INTEREST','AIRDROP','CASH_DIVIDEND','COUPON_PAYMENT','DISTRIBUTION','INTEREST','LIQUIDITY_MINING','MINING_REWARD','OTHER','QUALIFIED_DIVIDEND','RETURN_OF_CAPITAL','SPECIAL_DIVIDEND','STAKING_REWARD','STOCK_DIVIDEND') NOT NULL,
    is_qualified BIT(1) DEFAULT NULL,
    is_reinvested BIT(1) DEFAULT NULL,
    is_return_of_capital BIT(1) DEFAULT NULL,
    net_amount DECIMAL(18,2) DEFAULT NULL,
    notes TEXT,
    payment_date DATE NOT NULL,
    record_date DATE DEFAULT NULL,
    reinvest_price DECIMAL(18,8) DEFAULT NULL,
    reinvest_quantity DECIMAL(18,8) DEFAULT NULL,
    staking_validator VARCHAR(100) DEFAULT NULL,
    tax_withheld DECIMAL(10,2) DEFAULT NULL,
    units_held DECIMAL(18,8) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    yield_at_payment DECIMAL(8,4) DEFAULT NULL,
    cash_account_id BIGINT DEFAULT NULL,
    position_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_income_position (position_id),
    KEY idx_income_payment_date (payment_date),
    KEY idx_income_type (income_type),
    KEY FKhfupi8i3ygfnie2qn26pyrg95 (cash_account_id),
    CONSTRAINT FKhfupi8i3ygfnie2qn26pyrg95 FOREIGN KEY (cash_account_id) REFERENCES cash_accounts (id),
    CONSTRAINT FKia4wyi3egv27spf4ydhrfd0x8 FOREIGN KEY (position_id) REFERENCES positions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- CASH MANAGEMENT TABLES
-- =====================================================================

-- Cash Accounts
CREATE TABLE cash_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_name VARCHAR(100) NOT NULL,
    account_number_last4 VARCHAR(4) DEFAULT NULL,
    account_type ENUM('BROKERAGE_CASH','CD','CHECKING','EMERGENCY_FUND','HIGH_YIELD_SAVINGS','MONEY_MARKET','SAVINGS','TREASURY_DIRECT') NOT NULL,
    bank_name VARCHAR(100) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    currency_code VARCHAR(10) DEFAULT NULL,
    current_balance DECIMAL(18,2) NOT NULL,
    early_withdrawal_penalty DECIMAL(6,3) DEFAULT NULL,
    include_in_net_worth BIT(1) DEFAULT NULL,
    interest_compounding VARCHAR(20) DEFAULT NULL,
    interest_rate_apy DECIMAL(6,3) DEFAULT NULL,
    is_active BIT(1) DEFAULT NULL,
    maturity_date DATE DEFAULT NULL,
    minimum_balance DECIMAL(18,2) DEFAULT NULL,
    notes TEXT,
    term_months INT DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- MARKET DATA & ANALYSIS TABLES
-- =====================================================================

-- Price History
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
    instrument_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKc5t0exdpbd79f0kqk3jm5s915 (market_data_id, price_date),
    KEY idx_date (price_date),
    KEY idx_price_history_date (price_date),
    KEY idx_price_history_instrument (market_data_id),
    KEY FKp80uv6s7muellfv29ghadvryx (instrument_id),
    CONSTRAINT fk_price_history_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id) ON DELETE CASCADE,
    CONSTRAINT FKp80uv6s7muellfv29ghadvryx FOREIGN KEY (instrument_id) REFERENCES instruments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ESG Ratings
CREATE TABLE esg_ratings (
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

-- News
CREATE TABLE news (
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
-- ALERTS & MONITORING TABLES
-- =====================================================================

-- Price Alerts
CREATE TABLE price_alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alert_type ENUM('PERCENT_CHANGE_DOWN','PERCENT_CHANGE_UP','PRICE_ABOVE','PRICE_BELOW','TARGET_REACHED','VOLUME_SPIKE') NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    is_active BIT(1) DEFAULT NULL,
    is_recurring BIT(1) DEFAULT NULL,
    is_triggered BIT(1) DEFAULT NULL,
    notes TEXT,
    notify_email BIT(1) DEFAULT NULL,
    notify_push BIT(1) DEFAULT NULL,
    notify_sms BIT(1) DEFAULT NULL,
    reference_price DECIMAL(18,8) DEFAULT NULL,
    target_percent DECIMAL(8,4) DEFAULT NULL,
    target_price DECIMAL(18,8) DEFAULT NULL,
    times_triggered INT DEFAULT NULL,
    triggered_at DATETIME(6) DEFAULT NULL,
    triggered_price DECIMAL(18,8) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    position_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_price_alerts_position (position_id),
    KEY idx_price_alerts_active (is_active),
    CONSTRAINT FKi2thjotej2yvl5nk6ddql1ne1 FOREIGN KEY (position_id) REFERENCES positions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- CORPORATE ACTIONS TABLE
-- =====================================================================

-- Corporate Actions
CREATE TABLE corporate_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    action_type ENUM('ACQUISITION','BOND_CALL','BOND_MATURITY','CUSIP_CHANGE','DELISTING','MERGER','NAME_CHANGE','REVERSE_SPLIT','RIGHTS_ISSUE','SPIN_OFF','STOCK_DIVIDEND','STOCK_SPLIT','TENDER_OFFER') NOT NULL,
    announcement_date DATE DEFAULT NULL,
    cash_component DECIMAL(18,2) DEFAULT NULL,
    cost_basis_allocation DECIMAL(6,4) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    description TEXT,
    effective_date DATE NOT NULL,
    exchange_ratio DECIMAL(10,6) DEFAULT NULL,
    is_processed BIT(1) DEFAULT NULL,
    notes TEXT,
    positions_affected INT DEFAULT NULL,
    processed_date DATETIME(6) DEFAULT NULL,
    record_date DATE DEFAULT NULL,
    spinoff_ratio DECIMAL(10,6) DEFAULT NULL,
    split_ratio_from DECIMAL(10,4) DEFAULT NULL,
    split_ratio_to DECIMAL(10,4) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    instrument_id BIGINT NOT NULL,
    target_instrument_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_corp_actions_instrument (instrument_id),
    KEY idx_corp_actions_date (effective_date),
    KEY FKdn15he2c916gi2bxq0yrpxq7p (target_instrument_id),
    CONSTRAINT FKdn15he2c916gi2bxq0yrpxq7p FOREIGN KEY (target_instrument_id) REFERENCES instruments (id),
    CONSTRAINT FKt1p9101tttsby38983l6hd1vw FOREIGN KEY (instrument_id) REFERENCES instruments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- USER SETTINGS TABLES
-- =====================================================================

-- User Settings
CREATE TABLE user_settings (
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

-- Investor Settings
CREATE TABLE investor_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    after_hours_data_enabled BIT(1) DEFAULT NULL,
    alert_email VARCHAR(200) DEFAULT NULL,
    auto_refresh_enabled BIT(1) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    daily_summary_enabled BIT(1) DEFAULT NULL,
    date_format VARCHAR(20) DEFAULT NULL,
    decimal_places INT DEFAULT NULL,
    default_chart_period VARCHAR(10) DEFAULT NULL,
    default_cost_basis_method ENUM('AVERAGE_COST','FIFO','HIFO','LIFO','LIFO_BY_LOT','SPECIFIC_ID') DEFAULT NULL,
    default_currency VARCHAR(10) DEFAULT NULL,
    email_alerts_enabled BIT(1) DEFAULT NULL,
    hide_balances_by_default BIT(1) DEFAULT NULL,
    hide_small_positions BIT(1) DEFAULT NULL,
    investor_name VARCHAR(100) DEFAULT NULL,
    notifications_enabled BIT(1) DEFAULT NULL,
    refresh_interval_minutes INT DEFAULT NULL,
    show_cents BIT(1) DEFAULT NULL,
    show_percentages BIT(1) DEFAULT NULL,
    small_position_threshold DECIMAL(10,2) DEFAULT NULL,
    tax_rate_long_term DECIMAL(5,2) DEFAULT NULL,
    tax_rate_qualified_dividends DECIMAL(5,2) DEFAULT NULL,
    tax_rate_short_term DECIMAL(5,2) DEFAULT NULL,
    theme VARCHAR(20) DEFAULT NULL,
    timezone VARCHAR(50) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- END OF MIGRATION
-- =====================================================================
