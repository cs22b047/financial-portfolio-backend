-- =====================================================================
-- Migration Script: Old Schema → New Normalized Schema
-- =====================================================================
-- This script migrates data from the old tables (market_data, assets,
-- transactions, dividends) to the new normalized schema.
--
-- Run this AFTER the new tables are created by Hibernate.
-- =====================================================================

-- Step 1: Create reference data (Sectors)
INSERT INTO sectors (code, name, is_active, created_date)
SELECT DISTINCT
    UPPER(REPLACE(sector, ' ', '_')) as code,
    sector as name,
    1 as is_active,
    NOW() as created_date
FROM market_data
WHERE sector IS NOT NULL AND sector != ''
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Step 2: Create reference data (Exchanges)
INSERT INTO exchanges (code, name, exchange_type, is_active, created_date)
SELECT DISTINCT
    UPPER(SUBSTRING(exchange, 1, 20)) as code,
    exchange as name,
    'STOCK' as exchange_type,
    1 as is_active,
    NOW() as created_date
FROM market_data
WHERE exchange IS NOT NULL AND exchange != ''
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Step 3: Migrate market_data → instruments
INSERT INTO instruments (
    instrument_type,
    symbol,
    name,
    current_price,
    previous_close,
    day_change,
    day_change_percent,
    day_high,
    day_low,
    week_52_high,
    week_52_low,
    volume,
    market_cap,
    currency_code,
    data_source,
    last_updated,
    market_status,
    is_active,
    created_date,
    updated_date,
    sector_id,
    exchange_id
)
SELECT
    CASE
        WHEN at.code = 'STOCK' THEN 'STOCK'
        WHEN at.code = 'BOND' THEN 'BOND'
        WHEN at.code = 'CRYPTO' THEN 'CRYPTO'
        WHEN at.code = 'CASH' THEN 'STOCK'  -- Cash accounts handled separately
        ELSE 'STOCK'
    END as instrument_type,
    md.symbol,
    md.name,
    md.current_price,
    md.previous_close,
    md.day_change,
    md.day_change_percent,
    md.day_high,
    md.day_low,
    md.week_52_high,
    md.week_52_low,
    md.volume,
    md.market_cap,
    COALESCE(md.currency, 'USD') as currency_code,
    md.data_source,
    md.last_updated,
    md.market_status,
    1 as is_active,
    COALESCE(md.created_date, NOW()) as created_date,
    COALESCE(md.updated_date, NOW()) as updated_date,
    (SELECT id FROM sectors WHERE UPPER(REPLACE(md.sector, ' ', '_')) = code LIMIT 1) as sector_id,
    (SELECT id FROM exchanges WHERE UPPER(SUBSTRING(md.exchange, 1, 20)) = code LIMIT 1) as exchange_id
FROM market_data md
LEFT JOIN asset_types at ON md.asset_type_id = at.id
ON DUPLICATE KEY UPDATE
    current_price = VALUES(current_price),
    last_updated = VALUES(last_updated);

-- Step 4: Migrate stock-specific metrics
INSERT INTO stock_metrics (
    instrument_id,
    pe_ratio,
    dividend_yield,
    beta,
    earnings_per_share,
    updated_date
)
SELECT
    i.id as instrument_id,
    md.pe_ratio,
    md.dividend_yield,
    md.beta,
    md.earnings_per_share,
    NOW() as updated_date
FROM market_data md
JOIN instruments i ON md.symbol = i.symbol
JOIN asset_types at ON md.asset_type_id = at.id
WHERE at.code = 'STOCK'
AND (md.pe_ratio IS NOT NULL OR md.dividend_yield IS NOT NULL OR md.beta IS NOT NULL)
ON DUPLICATE KEY UPDATE
    pe_ratio = VALUES(pe_ratio),
    dividend_yield = VALUES(dividend_yield),
    beta = VALUES(beta);

-- Step 5: Migrate crypto-specific metrics
INSERT INTO crypto_metrics (
    instrument_id,
    max_supply,
    circulating_supply,
    total_supply,
    market_cap_rank,
    volume_24h,
    change_24h,
    change_7d,
    change_30d,
    updated_date
)
SELECT
    i.id as instrument_id,
    md.max_supply,
    md.circulating_supply,
    md.total_supply,
    md.market_cap_rank,
    md.volume_24h,
    md.change_24h,
    md.change_7d,
    md.change_30d,
    NOW() as updated_date
FROM market_data md
JOIN instruments i ON md.symbol = i.symbol
JOIN asset_types at ON md.asset_type_id = at.id
WHERE at.code = 'CRYPTO'
ON DUPLICATE KEY UPDATE
    circulating_supply = VALUES(circulating_supply),
    volume_24h = VALUES(volume_24h);

-- Step 6: Migrate assets → positions (OWNED status)
INSERT INTO positions (
    instrument_id,
    status,
    total_quantity,
    average_cost_basis,
    total_cost_basis,
    first_purchase_date,
    last_purchase_date,
    target_sell_price,
    notes,
    alerts_enabled,
    cost_basis_method,
    created_date,
    updated_date
)
SELECT
    i.id as instrument_id,
    'OPEN' as status,
    a.quantity as total_quantity,
    a.purchase_price as average_cost_basis,
    (a.quantity * a.purchase_price) as total_cost_basis,
    a.purchase_date as first_purchase_date,
    a.purchase_date as last_purchase_date,
    a.target_price as target_sell_price,
    a.notes,
    COALESCE(a.price_alerts_enabled, 0) as alerts_enabled,
    'FIFO' as cost_basis_method,
    COALESCE(a.created_date, NOW()) as created_date,
    COALESCE(a.updated_date, NOW()) as updated_date
FROM assets a
JOIN instruments i ON a.symbol = i.symbol
WHERE a.status = 'OWNED'
ON DUPLICATE KEY UPDATE
    total_quantity = VALUES(total_quantity),
    updated_date = NOW();

-- Step 7: Migrate assets → positions (WATCHLIST status)
INSERT INTO positions (
    instrument_id,
    status,
    target_buy_price,
    added_to_watchlist_date,
    priority_rank,
    notes,
    alerts_enabled,
    cost_basis_method,
    created_date,
    updated_date
)
SELECT
    i.id as instrument_id,
    'WATCHLIST' as status,
    a.target_price as target_buy_price,
    COALESCE(a.added_to_watchlist_date, CURDATE()) as added_to_watchlist_date,
    a.priority_rank,
    a.notes,
    COALESCE(a.price_alerts_enabled, 0) as alerts_enabled,
    'FIFO' as cost_basis_method,
    COALESCE(a.created_date, NOW()) as created_date,
    COALESCE(a.updated_date, NOW()) as updated_date
FROM assets a
JOIN instruments i ON a.symbol = i.symbol
WHERE a.status IN ('WATCHLIST', 'RESEARCH', 'PENDING_PURCHASE')
ON DUPLICATE KEY UPDATE
    target_buy_price = VALUES(target_buy_price),
    updated_date = NOW();

-- Step 8: Create tax lots from existing owned assets
-- (Initial purchase as a single lot)
INSERT INTO tax_lots (
    position_id,
    purchase_date,
    purchase_price,
    original_quantity,
    remaining_quantity,
    cost_basis_per_unit,
    total_cost_basis,
    is_wash_sale,
    split_adjusted,
    is_closed,
    created_date,
    updated_date
)
SELECT
    p.id as position_id,
    COALESCE(p.first_purchase_date, CURDATE()) as purchase_date,
    p.average_cost_basis as purchase_price,
    p.total_quantity as original_quantity,
    p.total_quantity as remaining_quantity,
    p.average_cost_basis as cost_basis_per_unit,
    p.total_cost_basis,
    0 as is_wash_sale,
    0 as split_adjusted,
    0 as is_closed,
    p.created_date,
    p.updated_date
FROM positions p
WHERE p.status = 'OPEN' AND p.total_quantity > 0;

-- Step 9: Migrate transactions → portfolio_transactions
INSERT INTO portfolio_transactions (
    position_id,
    transaction_type,
    transaction_date,
    quantity,
    price_per_unit,
    fees,
    total_amount,
    currency_code,
    notes,
    created_date,
    updated_date
)
SELECT
    p.id as position_id,
    CASE
        WHEN t.transaction_type = 'BUY' THEN 'BUY'
        WHEN t.transaction_type = 'SELL' THEN 'SELL'
        WHEN t.transaction_type IN ('DEPOSIT', 'TRANSFER_IN') THEN 'TRANSFER_IN'
        WHEN t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN 'TRANSFER_OUT'
        ELSE 'BUY'
    END as transaction_type,
    t.transaction_date,
    t.quantity,
    t.price as price_per_unit,
    t.fees,
    (t.quantity * t.price) as total_amount,
    COALESCE(t.currency, 'USD') as currency_code,
    t.notes,
    COALESCE(t.created_date, NOW()) as created_date,
    COALESCE(t.updated_date, NOW()) as updated_date
FROM transactions t
JOIN assets a ON t.asset_id = a.id
JOIN instruments i ON a.symbol = i.symbol
JOIN positions p ON p.instrument_id = i.id AND p.status IN ('OPEN', 'CLOSED')
WHERE t.transaction_type IN ('BUY', 'SELL', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT');

-- Step 10: Migrate dividends → income
INSERT INTO income (
    position_id,
    income_type,
    payment_date,
    ex_date,
    record_date,
    amount_per_unit,
    units_held,
    gross_amount,
    net_amount,
    tax_withheld,
    currency_code,
    is_qualified,
    notes,
    created_date,
    updated_date
)
SELECT
    p.id as position_id,
    CASE
        WHEN d.dividend_type = 'CASH_DIVIDEND' THEN 'CASH_DIVIDEND'
        WHEN d.dividend_type = 'STOCK_DIVIDEND' THEN 'STOCK_DIVIDEND'
        WHEN d.dividend_type = 'SPECIAL_DIVIDEND' THEN 'SPECIAL_DIVIDEND'
        WHEN d.dividend_type = 'COUPON_PAYMENT' THEN 'COUPON_PAYMENT'
        WHEN d.dividend_type = 'INTEREST_PAYMENT' THEN 'INTEREST'
        WHEN d.dividend_type = 'SAVINGS_INTEREST' THEN 'INTEREST'
        WHEN d.dividend_type = 'STAKING_REWARD' THEN 'STAKING_REWARD'
        ELSE 'CASH_DIVIDEND'
    END as income_type,
    d.payment_date,
    d.ex_dividend_date,
    d.record_date,
    d.amount_per_share as amount_per_unit,
    d.shares_held as units_held,
    d.total_amount as gross_amount,
    (d.total_amount - COALESCE(d.tax_withheld, 0)) as net_amount,
    d.tax_withheld,
    COALESCE(d.currency, 'USD') as currency_code,
    COALESCE(d.is_qualified, 1) as is_qualified,
    d.notes,
    COALESCE(d.created_date, NOW()) as created_date,
    COALESCE(d.updated_date, NOW()) as updated_date
FROM dividends d
JOIN assets a ON d.asset_id = a.id
JOIN instruments i ON a.symbol = i.symbol
JOIN positions p ON p.instrument_id = i.id;

-- Step 11: Migrate user_settings → investor_settings
INSERT INTO investor_settings (
    investor_name,
    default_currency,
    timezone,
    date_format,
    decimal_places,
    theme,
    default_cost_basis_method,
    notifications_enabled,
    auto_refresh_enabled,
    created_date,
    updated_date
)
SELECT
    us.user_name as investor_name,
    COALESCE(us.default_currency, 'USD') as default_currency,
    COALESCE(us.timezone, 'America/New_York') as timezone,
    COALESCE(us.date_format, 'MM/dd/yyyy') as date_format,
    COALESCE(us.decimal_places, 2) as decimal_places,
    COALESCE(us.theme, 'light') as theme,
    'FIFO' as default_cost_basis_method,
    COALESCE(us.notifications_enabled, 1) as notifications_enabled,
    1 as auto_refresh_enabled,
    COALESCE(us.created_date, NOW()) as created_date,
    COALESCE(us.updated_date, NOW()) as updated_date
FROM user_settings us
LIMIT 1;

-- Step 12: Insert default investor settings if none exist
INSERT INTO investor_settings (
    investor_name,
    default_currency,
    timezone,
    default_cost_basis_method,
    created_date,
    updated_date
)
SELECT
    'Investor',
    'USD',
    'America/New_York',
    'FIFO',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM investor_settings);

-- =====================================================================
-- Migration Complete
-- =====================================================================
-- After running this migration:
-- 1. Verify data in new tables
-- 2. Update Python extractor to use new schema
-- 3. Remove old entity files from Java code
-- 4. Drop old tables (market_data, assets, transactions, dividends, asset_types, user_settings)
-- =====================================================================
