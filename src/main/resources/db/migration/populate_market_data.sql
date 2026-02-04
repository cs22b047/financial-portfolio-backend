-- Populate Market Data with Sample Stocks
USE portfolio_db;

-- Clear existing market data
DELETE FROM market_data;

-- Insert popular stocks with realistic data
INSERT INTO market_data (
    symbol, name, current_price, previous_close, day_change, day_change_percent,
    volume, day_high, day_low, week_52_high, week_52_low,
    market_cap, sector, industry, exchange, currency,
    dividend_yield, pe_ratio, beta, eps, data_source
) VALUES
-- Technology Stocks
('AAPL', 'Apple Inc.', 150.25, 148.50, 1.75, 1.18, 65000000, 151.20, 148.30, 199.62, 124.17, 2400000000000, 'Technology', 'Consumer Electronics', 'NASDAQ', 'USD', 0.52, 25.3, 1.2, 6.15, 'Manual'),
('MSFT', 'Microsoft Corporation', 380.50, 378.20, 2.30, 0.61, 28000000, 382.00, 377.50, 384.30, 213.43, 2800000000000, 'Technology', 'Software', 'NASDAQ', 'USD', 0.78, 32.1, 0.9, 11.86, 'Manual'),
('GOOGL', 'Alphabet Inc.', 140.75, 142.30, -1.55, -1.09, 32000000, 143.50, 139.80, 153.13, 83.34, 1750000000000, 'Technology', 'Internet Services', 'NASDAQ', 'USD', 0.00, 24.8, 1.1, 5.80, 'Manual'),
('NVDA', 'NVIDIA Corporation', 500.80, 495.20, 5.60, 1.13, 45000000, 505.00, 493.50, 502.66, 108.13, 1230000000000, 'Technology', 'Semiconductors', 'NASDAQ', 'USD', 0.04, 65.2, 1.7, 7.69, 'Manual'),
('META', 'Meta Platforms Inc.', 485.30, 482.10, 3.20, 0.66, 18000000, 488.75, 480.20, 531.49, 88.09, 1250000000000, 'Technology', 'Social Media', 'NASDAQ', 'USD', 0.00, 28.5, 1.3, 17.03, 'Manual'),
('TSLA', 'Tesla Inc.', 180.25, 178.90, 1.35, 0.75, 120000000, 183.50, 177.20, 299.29, 101.81, 575000000000, 'Technology', 'Automotive', 'NASDAQ', 'USD', 0.00, 55.2, 2.1, 3.26, 'Manual'),
('AMD', 'Advanced Micro Devices', 165.40, 163.80, 1.60, 0.98, 52000000, 167.20, 162.50, 227.30, 93.12, 267000000000, 'Technology', 'Semiconductors', 'NASDAQ', 'USD', 0.00, 58.3, 1.9, 2.84, 'Manual'),

-- Finance
('JPM', 'JPMorgan Chase & Co.', 185.60, 184.20, 1.40, 0.76, 12000000, 186.80, 183.50, 194.16, 135.19, 535000000000, 'Finance', 'Banking', 'NYSE', 'USD', 2.15, 11.2, 1.1, 16.56, 'Manual'),
('BAC', 'Bank of America Corp.', 34.85, 34.50, 0.35, 1.01, 45000000, 35.20, 34.30, 39.98, 26.52, 270000000000, 'Finance', 'Banking', 'NYSE', 'USD', 2.48, 10.8, 1.3, 3.22, 'Manual'),
('V', 'Visa Inc.', 275.90, 274.30, 1.60, 0.58, 7500000, 277.50, 273.20, 290.96, 211.38, 550000000000, 'Finance', 'Payment Systems', 'NYSE', 'USD', 0.72, 32.5, 0.95, 8.48, 'Manual'),

-- Healthcare
('JNJ', 'Johnson & Johnson', 158.75, 157.90, 0.85, 0.54, 8500000, 159.80, 157.20, 169.94, 143.13, 385000000000, 'Healthcare', 'Pharmaceuticals', 'NYSE', 'USD', 2.95, 15.8, 0.7, 10.05, 'Manual'),
('PFE', 'Pfizer Inc.', 28.45, 28.20, 0.25, 0.89, 35000000, 28.90, 27.95, 61.71, 25.20, 160000000000, 'Healthcare', 'Pharmaceuticals', 'NYSE', 'USD', 5.98, 9.2, 0.6, 3.09, 'Manual'),
('UNH', 'UnitedHealth Group', 520.30, 518.50, 1.80, 0.35, 3200000, 523.75, 516.80, 559.38, 445.68, 485000000000, 'Healthcare', 'Health Insurance', 'NYSE', 'USD', 1.25, 24.3, 0.75, 21.41, 'Manual'),

-- Consumer
('WMT', 'Walmart Inc.', 165.25, 164.10, 1.15, 0.70, 8800000, 166.50, 163.75, 169.94, 117.27, 445000000000, 'Consumer', 'Retail', 'NYSE', 'USD', 1.41, 28.5, 0.5, 5.80, 'Manual'),
('DIS', 'The Walt Disney Co.', 98.75, 97.50, 1.25, 1.28, 12500000, 99.80, 97.20, 123.74, 78.73, 180000000000, 'Consumer', 'Entertainment', 'NYSE', 'USD', 0.00, 42.3, 1.2, 2.33, 'Manual'),
('NFLX', 'Netflix Inc.', 485.60, 482.30, 3.30, 0.68, 4500000, 488.90, 480.50, 700.99, 344.73, 210000000000, 'Consumer', 'Streaming', 'NASDAQ', 'USD', 0.00, 35.6, 1.3, 13.64, 'Manual'),

-- Energy
('XOM', 'Exxon Mobil Corp.', 108.40, 107.20, 1.20, 1.12, 18000000, 109.50, 106.80, 120.20, 95.63, 425000000000, 'Energy', 'Oil & Gas', 'NYSE', 'USD', 3.42, 10.5, 1.0, 10.32, 'Manual'),
('CVX', 'Chevron Corporation', 155.80, 154.50, 1.30, 0.84, 9500000, 157.20, 153.90, 189.68, 135.37, 285000000000, 'Energy', 'Oil & Gas', 'NYSE', 'USD', 3.68, 11.2, 1.1, 13.91, 'Manual');

-- Update timestamps
UPDATE market_data SET 
    last_updated = NOW(),
    created_date = NOW(),
    updated_date = NOW();

SELECT COUNT(*) as total_stocks FROM market_data;
SELECT symbol, name, current_price, sector FROM market_data ORDER BY symbol;
