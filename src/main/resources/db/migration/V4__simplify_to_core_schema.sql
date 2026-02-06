-- =====================================================================
-- V4: Final Core Schema (Queried from Database)
-- =====================================================================
-- This schema reflects the actual database structure as of 2026-02-05
-- Database: portfolio_db (MySQL)
-- Tables: 14
-- =====================================================================

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

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
    is_active BIT(1) NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_type_code (code),
    KEY idx_asset_type_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Currencies (Reference Table)
CREATE TABLE IF NOT EXISTS currencies (
    code VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) DEFAULT NULL,
    decimal_places INT DEFAULT NULL,
    exchange_rate_to_usd DECIMAL(18,8) DEFAULT NULL,
    is_active BIT(1) DEFAULT NULL,
    is_fiat BIT(1) DEFAULT NULL,
    rate_updated_at DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- USER SETTINGS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(100) DEFAULT NULL,
    default_currency VARCHAR(3) DEFAULT NULL,
    currency VARCHAR(3) DEFAULT NULL,
    timezone VARCHAR(50) DEFAULT NULL,
    time_zone VARCHAR(50) DEFAULT NULL,
    date_format VARCHAR(20) DEFAULT NULL,
    decimal_places INT DEFAULT NULL,
    theme VARCHAR(20) DEFAULT NULL,
    notifications_enabled BIT(1) DEFAULT NULL,
    price_alerts_enabled BIT(1) DEFAULT NULL,
    wallet DECIMAL(15,2) DEFAULT NULL,
    target DECIMAL(15,2) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
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
    currency VARCHAR(10) DEFAULT NULL,
    dividend_yield DECIMAL(8,4) DEFAULT NULL,
    pe_ratio DECIMAL(10,2) DEFAULT NULL,
    beta DECIMAL(8,4) DEFAULT NULL,
    eps DECIMAL(10,4) DEFAULT NULL,
    data_source VARCHAR(50) DEFAULT NULL,
    market_status VARCHAR(20) DEFAULT NULL,
    last_updated DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_market_data_symbol (symbol),
    KEY idx_market_data_sector (sector),
    KEY idx_market_data_last_updated (last_updated),
    KEY FK9bh9qb9lmuj6ed1apg26u0v (asset_type_id),
    CONSTRAINT FK9bh9qb9lmuj6ed1apg26u0v FOREIGN KEY (asset_type_id) REFERENCES asset_types (id)
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
    price_alerts_enabled BIT(1) DEFAULT NULL,
    notes TEXT,
    priority_rank INT DEFAULT NULL,
    sector VARCHAR(100) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_market_data (market_data_id),
    KEY idx_asset_status (status),
    KEY idx_asset_symbol (symbol),
    KEY FK6wo1vfa2qskcca211v9l28ol2 (asset_type_id),
    CONSTRAINT FK6wo1vfa2qskcca211v9l28ol2 FOREIGN KEY (asset_type_id) REFERENCES asset_types (id),
    CONSTRAINT FKqyj348m48hs6gb9moc41cgokg FOREIGN KEY (market_data_id) REFERENCES market_data (id)
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
    fees DECIMAL(15,4) DEFAULT NULL,
    notes TEXT,
    currency VARCHAR(10) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_transaction_asset (asset_id),
    KEY idx_transaction_type (transaction_type),
    KEY idx_transaction_date (transaction_date),
    CONSTRAINT FKpxknnk31w8gmwnnir0kg21mv7 FOREIGN KEY (asset_id) REFERENCES assets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- ALERTS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    above_or_below ENUM('ABOVE','BELOW') NOT NULL,
    target_price DECIMAL(15,4) NOT NULL,
    triggered BIT(1) NOT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_alerts_asset_id (asset_id),
    KEY idx_alerts_triggered (triggered),
    KEY idx_alerts_asset_triggered (asset_id, triggered),
    CONSTRAINT FK101omdbkskjijl6apqw8ot19q FOREIGN KEY (asset_id) REFERENCES assets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- CONVERSATIONS TABLE (Chatbot)
-- =====================================================================

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) DEFAULT NULL,
    title VARCHAR(200) DEFAULT NULL,
    conversation_status ENUM('ACTIVE','ARCHIVED','DELETED') NOT NULL,
    last_message_at DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_conversation_session (session_id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_status (conversation_status),
    KEY idx_last_message (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- CHAT MESSAGES TABLE (Chatbot)
-- =====================================================================

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role ENUM('ASSISTANT','SYSTEM','USER') NOT NULL,
    content TEXT NOT NULL,
    query_type ENUM('DATA','ERROR','EXPLANATION','GENERAL') DEFAULT NULL,
    sql_query TEXT DEFAULT NULL,
    sql_result JSON DEFAULT NULL,
    tables_accessed JSON DEFAULT NULL,
    model_used VARCHAR(100) DEFAULT NULL,
    tokens_used INT DEFAULT NULL,
    processing_time_ms INT DEFAULT NULL,
    error_message TEXT DEFAULT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_conversation (conversation_id),
    KEY idx_role (role),
    KEY idx_query_type (query_type),
    KEY idx_created_date (created_date),
    CONSTRAINT FKc8ljv426x8fj9tcywei40stu9 FOREIGN KEY (conversation_id) REFERENCES conversations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- ESG RATINGS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS esg_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    total_score DECIMAL(5,2) DEFAULT NULL,
    total_grade VARCHAR(20) DEFAULT NULL,
    environment_score DECIMAL(5,2) DEFAULT NULL,
    environment_grade VARCHAR(20) DEFAULT NULL,
    social_score DECIMAL(5,2) DEFAULT NULL,
    social_grade VARCHAR(20) DEFAULT NULL,
    governance_score DECIMAL(5,2) DEFAULT NULL,
    governance_grade VARCHAR(20) DEFAULT NULL,
    controversy_level INT DEFAULT NULL,
    risk_level VARCHAR(20) DEFAULT NULL,
    data_source VARCHAR(100) DEFAULT NULL,
    last_updated DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_esg_symbol (symbol),
    KEY idx_market_data (market_data_id),
    KEY idx_total_score (total_score),
    KEY idx_esg_total_score (total_score),
    KEY idx_controversy (controversy_level),
    KEY idx_esg_controversy (controversy_level),
    CONSTRAINT fk_esg_market_data FOREIGN KEY (market_data_id) REFERENCES market_data (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- NEWS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS news (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT DEFAULT NULL,
    symbol VARCHAR(20) DEFAULT NULL,
    title VARCHAR(500) NOT NULL,
    summary TEXT DEFAULT NULL,
    link VARCHAR(1000) DEFAULT NULL,
    image_url VARCHAR(1000) DEFAULT NULL,
    publisher VARCHAR(200) DEFAULT NULL,
    source VARCHAR(50) DEFAULT NULL,
    sentiment VARCHAR(20) DEFAULT NULL,
    is_read BIT(1) DEFAULT NULL,
    published_date DATETIME(6) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_news_symbol (symbol),
    KEY idx_news_market_data (market_data_id),
    KEY idx_news_published_date (published_date),
    KEY idx_news_sentiment (sentiment),
    CONSTRAINT FKhkdiowmgm1ue8qdo5wbrexq57 FOREIGN KEY (market_data_id) REFERENCES market_data (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- PRICE HISTORY TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    open_price DECIMAL(18,8) DEFAULT NULL,
    high_price DECIMAL(18,8) DEFAULT NULL,
    low_price DECIMAL(18,8) DEFAULT NULL,
    close_price DECIMAL(18,8) NOT NULL,
    adjusted_close DECIMAL(18,8) DEFAULT NULL,
    volume BIGINT DEFAULT NULL,
    data_source VARCHAR(50) DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKg54ndcfbj2wu1jg986jf87645 (market_data_id, price_date),
    KEY idx_price_history_market_data (market_data_id),
    KEY idx_price_history_date (price_date),
    CONSTRAINT FKqw0cpinve7vy3itc94q3ovgpo FOREIGN KEY (market_data_id) REFERENCES market_data (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- STOCK SUMMARY TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS stock_summary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market_data_id BIGINT NOT NULL,
    period VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_price DECIMAL(18,8) DEFAULT NULL,
    end_price DECIMAL(18,8) DEFAULT NULL,
    min_price DECIMAL(18,8) DEFAULT NULL,
    max_price DECIMAL(18,8) DEFAULT NULL,
    avg_price DECIMAL(18,8) DEFAULT NULL,
    total_return DECIMAL(10,4) DEFAULT NULL,
    annualized_return DECIMAL(10,4) DEFAULT NULL,
    daily_volatility DECIMAL(10,6) DEFAULT NULL,
    annualized_volatility DECIMAL(10,4) DEFAULT NULL,
    avg_daily_return DECIMAL(10,6) DEFAULT NULL,
    max_daily_gain DECIMAL(10,4) DEFAULT NULL,
    max_daily_loss DECIMAL(10,4) DEFAULT NULL,
    max_drawdown DECIMAL(10,4) DEFAULT NULL,
    sharpe_ratio DECIMAL(10,4) DEFAULT NULL,
    var_95 DECIMAL(10,4) DEFAULT NULL,
    var_99 DECIMAL(10,4) DEFAULT NULL,
    min_volume BIGINT DEFAULT NULL,
    max_volume BIGINT DEFAULT NULL,
    avg_volume BIGINT DEFAULT NULL,
    trading_days INT DEFAULT NULL,
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKmm86hi8hv2ap00d2jyr536mwu (market_data_id, period, start_date, end_date),
    KEY idx_stock_summary_market_data (market_data_id),
    KEY idx_stock_summary_period (period),
    KEY idx_stock_summary_dates (start_date, end_date),
    CONSTRAINT FK9u665idgskbnqigyisdi6fmnt FOREIGN KEY (market_data_id) REFERENCES market_data (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- TECHNICAL INDICATORS TABLE
-- =====================================================================

CREATE TABLE IF NOT EXISTS technical_indicators (
    id BIGINT NOT NULL AUTO_INCREMENT,
    price_history_id BIGINT NOT NULL,
    indicator_date DATE NOT NULL,
    -- Moving Averages
    sma_20 DECIMAL(18,8) DEFAULT NULL,
    sma_50 DECIMAL(18,8) DEFAULT NULL,
    sma_200 DECIMAL(18,8) DEFAULT NULL,
    ema_12 DECIMAL(18,8) DEFAULT NULL,
    ema_26 DECIMAL(18,8) DEFAULT NULL,
    -- MACD
    macd DECIMAL(18,8) DEFAULT NULL,
    macd_signal DECIMAL(18,8) DEFAULT NULL,
    macd_histogram DECIMAL(18,8) DEFAULT NULL,
    -- RSI
    rsi DECIMAL(8,4) DEFAULT NULL,
    -- Bollinger Bands
    bb_upper DECIMAL(18,8) DEFAULT NULL,
    bb_middle DECIMAL(18,8) DEFAULT NULL,
    bb_lower DECIMAL(18,8) DEFAULT NULL,
    bb_width DECIMAL(18,8) DEFAULT NULL,
    -- Stochastic
    stochastic_k DECIMAL(8,4) DEFAULT NULL,
    stochastic_d DECIMAL(8,4) DEFAULT NULL,
    -- ATR
    atr DECIMAL(18,8) DEFAULT NULL,
    -- Momentum
    momentum_10 DECIMAL(18,8) DEFAULT NULL,
    roc_10 DECIMAL(10,4) DEFAULT NULL,
    -- Returns
    daily_return DECIMAL(18,8) DEFAULT NULL,
    log_return DECIMAL(18,8) DEFAULT NULL,
    cumulative_return DECIMAL(18,8) DEFAULT NULL,
    -- Volume
    volume_sma_20 BIGINT DEFAULT NULL,
    volume_ratio DECIMAL(10,4) DEFAULT NULL,
    -- Timestamps
    created_date DATETIME(6) DEFAULT NULL,
    updated_date DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK3i60m4tdf42nctcrn40jq07k4 (price_history_id),
    KEY idx_technical_indicators_price_history (price_history_id),
    KEY idx_technical_indicators_date (indicator_date),
    CONSTRAINT FKhrh6f3dmsc7jml9rkfkwp0b18 FOREIGN KEY (price_history_id) REFERENCES price_history (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- SCHEMA SUMMARY
-- =====================================================================
-- Tables: 14
--   1. asset_types           - Reference table for asset categories
--   2. currencies            - Reference table for currency codes
--   3. user_settings         - User preferences and settings
--   4. market_data           - Real-time market data for all assets
--   5. assets                - User's owned/watchlist assets
--   6. transactions          - Buy/Sell/Dividend transactions
--   7. alerts                - Price alerts for assets
--   8. conversations         - Chatbot conversation sessions
--   9. chat_messages         - Individual chat messages
--  10. esg_ratings           - Environmental, Social, Governance scores
--  11. news                  - Financial news articles
--  12. price_history         - Historical OHLCV data
--  13. stock_summary         - Computed statistics per period
--  14. technical_indicators  - Computed technical analysis indicators
--
-- Key Relationships:
--   market_data -> asset_types
--   assets -> market_data, asset_types
--   transactions -> assets
--   alerts -> assets
--   chat_messages -> conversations
--   esg_ratings -> market_data
--   news -> market_data
--   price_history -> market_data
--   stock_summary -> market_data
--   technical_indicators -> price_history
-- =====================================================================
