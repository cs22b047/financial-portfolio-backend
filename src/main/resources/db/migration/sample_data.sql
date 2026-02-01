-- Portfolio Management System - Sample Data
-- Insert order: asset_types -> market_data -> user_settings -> assets -> transactions -> dividends

-- 1. Insert Asset Types
INSERT INTO asset_types (code, name, description, risk_level, is_active, created_date, updated_date) VALUES
('STOCK', 'Stocks', 'Equity securities representing ownership in public companies', 'MEDIUM', 1, NOW(), NOW()),
('BOND', 'Bonds', 'Fixed-income debt securities issued by corporations or governments', 'LOW', 1, NOW(), NOW()),
('CASH', 'Cash & Cash Equivalents', 'Liquid assets including savings accounts and money market funds', 'LOW', 1, NOW(), NOW()),
('CRYPTO', 'Cryptocurrency', 'Digital assets and cryptocurrencies', 'VERY_HIGH', 1, NOW(), NOW());

-- 2. Insert Market Data
INSERT INTO market_data (asset_type_id, symbol, name, current_price, previous_close, day_change, day_change_percent, volume, day_high, day_low, week_52_high, week_52_low, market_cap, bid_price, ask_price, sector, industry, dividend_yield, pe_ratio, beta, data_source, last_updated, market_status, currency, exchange, created_date, updated_date) VALUES
-- Stock Market Data
(1, 'AAPL', 'Apple Inc.', 175.50, 174.20, 1.30, 0.75, 65432100, 176.80, 173.90, 198.23, 164.08, 2800000000000, 175.48, 175.52, 'Technology', 'Consumer Electronics', 0.52, 28.5, 1.21, 'YAHOO', NOW(), 'OPEN', 'USD', 'NASDAQ', NOW(), NOW()),
(1, 'MSFT', 'Microsoft Corporation', 420.15, 418.90, 1.25, 0.30, 28975400, 422.10, 417.50, 468.35, 362.90, 3100000000000, 420.10, 420.20, 'Technology', 'Software', 0.68, 32.1, 0.89, 'YAHOO', NOW(), 'OPEN', 'USD', 'NASDAQ', NOW(), NOW()),
(1, 'GOOGL', 'Alphabet Inc. Class A', 138.75, 137.20, 1.55, 1.13, 24567800, 139.80, 136.90, 151.55, 121.46, 1750000000000, 138.70, 138.80, 'Technology', 'Internet Services', 0.00, 24.8, 1.05, 'YAHOO', NOW(), 'OPEN', 'USD', 'NASDAQ', NOW(), NOW()),
(1, 'TSLA', 'Tesla, Inc.', 248.90, 245.60, 3.30, 1.34, 89765400, 252.40, 244.10, 299.29, 138.80, 790000000000, 248.85, 248.95, 'Consumer Cyclical', 'Auto Manufacturers', 0.00, 68.2, 2.04, 'YAHOO', NOW(), 'OPEN', 'USD', 'NASDAQ', NOW(), NOW()),
(1, 'NVDA', 'NVIDIA Corporation', 875.20, 868.50, 6.70, 0.77, 45123600, 882.30, 865.40, 1037.99, 765.23, 2150000000000, 875.15, 875.25, 'Technology', 'Semiconductors', 0.03, 74.5, 1.67, 'YAHOO', NOW(), 'OPEN', 'USD', 'NASDAQ', NOW(), NOW()),

-- Bond Market Data
(2, 'US10Y', 'US Treasury 10 Year', 102.45, 102.32, 0.13, 0.13, 125000, 102.56, 102.28, 105.20, 98.75, NULL, 102.43, 102.47, NULL, 'Government Bond', NULL, NULL, NULL, 'YAHOO', NOW(), 'OPEN', 'USD', 'NYSE', NOW(), NOW()),
(2, 'CORP5Y', 'Corporate Bond 5Y AA', 98.75, 98.68, 0.07, 0.07, 85000, 98.82, 98.65, 101.25, 96.50, NULL, 98.73, 98.77, 'Financial', 'Corporate Bond', 4.25, NULL, NULL, 'YAHOO', NOW(), 'OPEN', 'USD', 'NYSE', NOW(), NOW()),

-- Cash Market Data
(3, 'HYSA', 'High Yield Savings Account', 1.00, 1.00, 0.00, 0.00, 0, 1.00, 1.00, 1.00, 1.00, NULL, 1.00, 1.00, NULL, 'Cash Equivalent', 4.50, NULL, 0.00, 'MANUAL', NOW(), 'OPEN', 'USD', 'BANK', NOW(), NOW()),
(3, 'MMF', 'Money Market Fund', 1.00, 1.00, 0.00, 0.00, 0, 1.00, 1.00, 1.00, 1.00, NULL, 1.00, 1.00, NULL, 'Cash Equivalent', 4.75, NULL, 0.00, 'MANUAL', NOW(), 'OPEN', 'USD', 'BANK', NOW(), NOW()),

-- Crypto Market Data
(4, 'BTC', 'Bitcoin', 67450.50, 66890.25, 560.25, 0.84, 28567890000, 68200.75, 66125.40, 73737.94, 38505.44, 1330000000000, 67445.30, 67455.70, NULL, 'Cryptocurrency', NULL, NULL, NULL, 'COINBASE', NOW(), 'OPEN', 'USD', 'CRYPTO', NOW(), NOW()),
(4, 'ETH', 'Ethereum', 3285.75, 3251.20, 34.55, 1.06, 15234567000, 3312.90, 3240.15, 4891.70, 2159.43, 395000000000, 3284.50, 3287.00, NULL, 'Cryptocurrency', NULL, NULL, NULL, 'COINBASE', NOW(), 'OPEN', 'USD', 'CRYPTO', NOW(), NOW()),
(4, 'ADA', 'Cardano', 0.485, 0.478, 0.007, 1.46, 523456789, 0.492, 0.475, 1.356, 0.323, 17200000000, 0.484, 0.486, NULL, 'Cryptocurrency', NULL, NULL, NULL, 'COINBASE', NOW(), 'OPEN', 'USD', 'CRYPTO', NOW(), NOW());

-- 3. Insert User Settings
INSERT INTO user_settings (user_name, default_currency, timezone, date_format, decimal_places, theme, notifications_enabled, price_alerts_enabled, created_date, updated_date) VALUES
('John Investor', 'USD', 'America/New_York', 'yyyy-MM-dd', 2, 'dark', 1, 1, NOW(), NOW());

-- 4. Insert Assets (Mix of owned and watchlist)
INSERT INTO assets (asset_type_id, market_data_id, status, symbol, name, quantity, purchase_price, current_price, purchase_date, target_price, added_to_watchlist_date, price_alerts_enabled, notes, priority_rank, sector, created_date, updated_date) VALUES
-- Owned Stocks
(1, 1, 'OWNED', 'AAPL', 'Apple Inc.', 150.0000, 168.50, 175.50, '2025-11-15', NULL, '2025-11-10', 1, 'Core holding in tech sector', 1, 'Technology', NOW(), NOW()),
(1, 2, 'OWNED', 'MSFT', 'Microsoft Corporation', 75.0000, 405.25, 420.15, '2025-12-01', NULL, '2025-11-20', 1, 'Solid cloud computing exposure', 1, 'Technology', NOW(), NOW()),
(1, 4, 'OWNED', 'TSLA', 'Tesla, Inc.', 25.0000, 195.75, 248.90, '2025-09-20', NULL, '2025-09-15', 1, 'EV growth play', 2, 'Consumer Cyclical', NOW(), NOW()),

-- Owned Bonds
(2, 6, 'OWNED', 'US10Y', 'US Treasury 10 Year', 10000.0000, 101.25, 102.45, '2025-10-10', NULL, '2025-10-01', 0, 'Safe government bond allocation', 1, NULL, NOW(), NOW()),
(2, 7, 'OWNED', 'CORP5Y', 'Corporate Bond 5Y AA', 5000.0000, 99.50, 98.75, '2025-11-20', NULL, '2025-11-15', 0, 'Higher yield corporate exposure', 2, 'Financial', NOW(), NOW()),

-- Owned Cash
(3, 8, 'OWNED', 'HYSA', 'High Yield Savings Account', 25000.0000, 1.00, 1.00, '2025-08-01', NULL, '2025-08-01', 0, 'Emergency fund in high yield savings', 1, NULL, NOW(), NOW()),
(3, 9, 'OWNED', 'MMF', 'Money Market Fund', 15000.0000, 1.00, 1.00, '2025-09-15', NULL, '2025-09-15', 0, 'Liquid cash for opportunities', 1, NULL, NOW(), NOW()),

-- Owned Crypto
(4, 10, 'OWNED', 'BTC', 'Bitcoin', 0.5000, 62450.00, 67450.50, '2025-10-25', NULL, '2025-10-20', 1, 'Digital gold allocation', 1, NULL, NOW(), NOW()),
(4, 11, 'OWNED', 'ETH', 'Ethereum', 2.2500, 3100.50, 3285.75, '2025-11-05', NULL, '2025-11-01', 1, 'DeFi and smart contracts exposure', 1, NULL, NOW(), NOW()),

-- Watchlist Items
(1, 3, 'WATCHLIST', 'GOOGL', 'Alphabet Inc. Class A', NULL, NULL, 138.75, NULL, 130.00, '2026-01-15', 1, 'Waiting for better entry point in AI leader', 2, 'Technology', NOW(), NOW()),
(1, 5, 'WATCHLIST', 'NVDA', 'NVIDIA Corporation', NULL, NULL, 875.20, NULL, 800.00, '2026-01-20', 1, 'Expensive but dominant in AI chips', 3, 'Technology', NOW(), NOW()),
(4, 12, 'RESEARCH', 'ADA', 'Cardano', NULL, NULL, 0.485, NULL, 0.40, '2026-01-25', 1, 'Researching proof-of-stake blockchain', 4, NULL, NOW(), NOW());

-- 5. Insert Transactions
INSERT INTO transactions (asset_id, transaction_type, quantity, price, transaction_date, fees, notes, currency, created_date, updated_date) VALUES
-- Stock Transactions
(1, 'BUY', 150.0000, 168.50, '2025-11-15', 9.99, 'Initial AAPL position', 'USD', NOW(), NOW()),
(2, 'BUY', 75.0000, 405.25, '2025-12-01', 12.50, 'Added MSFT to portfolio', 'USD', NOW(), NOW()),
(3, 'BUY', 25.0000, 195.75, '2025-09-20', 8.75, 'TSLA speculative position', 'USD', NOW(), NOW()),

-- Bond Transactions
(4, 'BUY', 10000.0000, 101.25, '2025-10-10', 25.00, 'Treasury bond for stability', 'USD', NOW(), NOW()),
(5, 'BUY', 5000.0000, 99.50, '2025-11-20', 15.00, 'Corporate bond for yield', 'USD', NOW(), NOW()),

-- Cash Transactions
(6, 'DEPOSIT', 25000.0000, 1.00, '2025-08-01', 0.00, 'Initial emergency fund deposit', 'USD', NOW(), NOW()),
(7, 'DEPOSIT', 15000.0000, 1.00, '2025-09-15', 0.00, 'Additional cash allocation', 'USD', NOW(), NOW()),

-- Crypto Transactions
(8, 'BUY', 0.5000, 62450.00, '2025-10-25', 45.25, 'Initial Bitcoin purchase', 'USD', NOW(), NOW()),
(9, 'BUY', 2.2500, 3100.50, '2025-11-05', 28.75, 'Ethereum for DeFi exposure', 'USD', NOW(), NOW());

-- 6. Insert Dividends
INSERT INTO dividends (asset_id, dividend_type, payment_date, ex_dividend_date, record_date, declaration_date, amount_per_share, shares_held, total_amount, tax_withheld, currency, notes, dividend_yield, is_qualified, created_date, updated_date) VALUES
-- Stock Dividends
(1, 'CASH_DIVIDEND', '2025-11-15', '2025-11-01', '2025-11-03', '2025-10-28', 0.24, 150.0000, 36.00, 5.40, 'USD', 'Q4 2025 AAPL dividend', 0.52, 1, NOW(), NOW()),
(1, 'CASH_DIVIDEND', '2025-12-15', '2025-12-01', '2025-12-03', '2025-11-25', 0.24, 150.0000, 36.00, 5.40, 'USD', 'Q1 2026 AAPL dividend', 0.52, 1, NOW(), NOW()),
(2, 'CASH_DIVIDEND', '2025-12-10', '2025-11-20', '2025-11-22', '2025-11-15', 0.75, 75.0000, 56.25, 8.44, 'USD', 'Q4 2025 MSFT dividend', 0.68, 1, NOW(), NOW()),

-- Bond Interest
(4, 'INTEREST_PAYMENT', '2025-10-15', NULL, NULL, NULL, 0.0225, 10000.0000, 225.00, 0.00, 'USD', 'Semi-annual treasury interest', 2.25, 1, NOW(), NOW()),
(5, 'COUPON_PAYMENT', '2025-12-01', NULL, NULL, NULL, 0.02125, 5000.0000, 106.25, 0.00, 'USD', 'Corporate bond coupon payment', 4.25, 1, NOW(), NOW()),

-- Cash Interest
(6, 'SAVINGS_INTEREST', '2025-12-31', NULL, NULL, NULL, 0.00375, 25000.0000, 93.75, 14.06, 'USD', 'Monthly savings account interest', 4.50, 0, NOW(), NOW()),
(7, 'MONEY_MARKET_INTEREST', '2025-12-31', NULL, NULL, NULL, 0.00396, 15000.0000, 59.38, 8.91, 'USD', 'Monthly money market interest', 4.75, 0, NOW(), NOW());

-- Show summary of inserted data
SELECT 'Asset Types' as table_name, COUNT(*) as record_count FROM asset_types
UNION ALL
SELECT 'Market Data' as table_name, COUNT(*) as record_count FROM market_data
UNION ALL
SELECT 'User Settings' as table_name, COUNT(*) as record_count FROM user_settings
UNION ALL
SELECT 'Assets' as table_name, COUNT(*) as record_count FROM assets
UNION ALL
SELECT 'Transactions' as table_name, COUNT(*) as record_count FROM transactions
UNION ALL
SELECT 'Dividends' as table_name, COUNT(*) as record_count FROM dividends;