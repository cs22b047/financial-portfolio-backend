# Portfolio Tracker - API Endpoints

Complete list of all REST API endpoints for the Financial Portfolio Backend.

---

## 📊 Positions API
**Base URL:** `/api/positions`

Track your stock positions, watchlist, and execute buy/sell transactions.

### Get Positions
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/positions` | Get all positions | - |
| GET | `/api/positions/open` | Get open (owned) positions | - |
| GET | `/api/positions/watchlist` | Get watchlist positions | - |
| GET | `/api/positions/{id}` | Get position by ID | `id` (path) |
| GET | `/api/positions/symbol/{symbol}` | Get positions by stock symbol | `symbol` (path) |
| GET | `/api/positions/type/{type}` | Get positions by instrument type | `type` (path) |
| GET | `/api/positions/{id}/tax-lots` | Get all tax lots for position | `id` (path) |
| GET | `/api/positions/{id}/tax-lots/open` | Get open tax lots for position | `id` (path) |

### Manage Positions
| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/positions/buy` | Record stock purchase | `{ symbol, quantity, price, date }` |
| POST | `/api/positions/sell` | Record stock sale | `{ positionId, quantity, price, date }` |
| POST | `/api/positions/watchlist` | Add stock to watchlist | `{ symbol }` |
| DELETE | `/api/positions/watchlist/{id}` | Remove from watchlist | - |

### Update Position Settings
| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| PUT | `/api/positions/{id}/notes` | Update position notes | `{ notes }` |
| PUT | `/api/positions/{id}/target-sell-price` | Set target sell price | `{ targetPrice }` |
| PUT | `/api/positions/{id}/cost-basis-method` | Change cost basis method | `{ method }` |

---

## 📈 Market Data API
**Base URL:** `/api/market-data`

Access stock prices, market information, and search for securities.

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/market-data` | Get all market data | - |
| GET | `/api/market-data/symbol/{symbol}` | Get price for specific stock | `symbol` (path) |
| GET | `/api/market-data/search` | Search stocks | `query` (query param) |
| GET | `/api/market-data/sector/{sector}` | Get stocks by sector | `sector` (path) |
| GET | `/api/market-data/top-gainers` | Top gaining stocks | `limit` (query, default=10) |
| GET | `/api/market-data/top-losers` | Top losing stocks | `limit` (query, default=10) |
| POST | `/api/market-data` | Add/update market data | `{ symbol, price, ... }` |
| DELETE | `/api/market-data/{id}` | Delete market data | `id` (path) |

---

## 🔄 Transactions API
**Base URL:** `/api/transactions`

View transaction history and calculate gains/losses.

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/transactions` | Get all transactions | - |
| GET | `/api/transactions/symbol/{symbol}` | Transactions for specific stock | `symbol` (path) |
| GET | `/api/transactions/type/{type}` | Filter by BUY/SELL | `type` (path) |
| GET | `/api/transactions/date-range` | Get by date range | `startDate`, `endDate` (query) |
| GET | `/api/transactions/total-invested` | Total amount invested | - |
| GET | `/api/transactions/realized-gains` | Realized profit/loss | - |

---

## 💵 Dividends API
**Base URL:** `/api/dividends`

Track dividend income from your stocks.

### Get Dividends
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/dividends` | Get all dividends | - |
| GET | `/api/dividends/symbol/{symbol}` | Dividends for specific stock | `symbol` (path) |
| GET | `/api/dividends/asset/{assetId}` | Dividends for specific asset | `assetId` (path) |
| GET | `/api/dividends/date-range` | Get by date range | `startDate`, `endDate` (query) |
| GET | `/api/dividends/total-income` | Total dividend income | `startDate`, `endDate` (query) |
| GET | `/api/dividends/recent` | Recent dividends | `months` (query, default=12) |
| GET | `/api/dividends/owned` | Dividends from owned stocks only | - |
| GET | `/api/dividends/{id}` | Get dividend by ID | `id` (path) |

### Manage Dividends
| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/dividends` | Record dividend (auto-calculates from shares) | `{ symbol, paymentDate, amountPerShare }` |
| POST | `/api/dividends/with-shares` | Record dividend with explicit shares | `{ symbol, paymentDate, amountPerShare, sharesAtPayment }` |
| DELETE | `/api/dividends/{id}` | Delete dividend | - |

---

## 🎵 Instruments API
**Base URL:** `/api/instruments`

Manage stock/asset metadata and information.

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/instruments` | Get all instruments | - |
| GET | `/api/instruments/{id}` | Get instrument by ID | `id` (path) |
| GET | `/api/instruments/symbol/{symbol}` | Get instrument by symbol | `symbol` (path) |
| GET | `/api/instruments/type/{type}` | Get by type (STOCK, ETF, BOND, etc.) | `type` (path) |
| GET | `/api/instruments/search` | Search instruments | `query` (query param) |
| POST | `/api/instruments` | Create new instrument | `{ symbol, name, type, ... }` |
| PUT | `/api/instruments/{id}` | Update instrument | `{ name, type, ... }` |
| DELETE | `/api/instruments/{id}` | Delete instrument | `id` (path) |

---

## 📁 Portfolio API
**Base URL:** `/api/portfolio`

Portfolio-level analytics, summaries, and performance metrics.

### Summary & Overview
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolio/summary` | Portfolio summary & metrics |

### Allocation Analysis
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolio/allocation/type` | Asset allocation by type |
| GET | `/api/portfolio/allocation/sector` | Asset allocation by sector |

### Performance Metrics
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/portfolio/top-performers` | Top performing positions | `limit` (query, default=5) |
| GET | `/api/portfolio/worst-performers` | Worst performing positions | `limit` (query, default=5) |
| GET | `/api/portfolio/largest-positions` | Largest positions by value | `limit` (query, default=10) |

### Realized Gains
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/portfolio/realized-gains` | Realized gains summary | `startDate`, `endDate` (query) |
| GET | `/api/portfolio/realized-gains/ytd` | Year-to-date realized gains | - |

---

## 📊 Quick Reference

### Common Query Parameters
- **Date Range**: `startDate` and `endDate` in ISO format (YYYY-MM-DD)
- **Limit**: Maximum number of results to return (default varies by endpoint)
- **Search**: `query` parameter for text search

### Common Response Codes
- **200 OK**: Successful GET/PUT request
- **201 Created**: Successful POST request
- **204 No Content**: Successful DELETE request
- **400 Bad Request**: Invalid request parameters
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

### Authentication
Currently, all endpoints are open (CORS enabled for all origins).

---

**Total Controllers**: 6  
**Total Endpoints**: 60+

Last Updated: February 3, 2026
