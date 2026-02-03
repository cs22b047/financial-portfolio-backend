# Portfolio Tracker - API Endpoints

Complete list of all REST API endpoints for the Financial Portfolio Backend.

---

## 📊 Assets API
**Base URL:** `/api/assets`

Track your stock holdings, watchlist, and execute buy/sell transactions.

### Get Assets
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/assets` | Get all assets (all statuses) | - |
| GET | `/api/assets/owned` | Get owned assets | - |
| GET | `/api/assets/watchlist` | Get watchlist assets | - |
| GET | `/api/assets/{id}` | Get asset by ID | `id` (path) |
| GET | `/api/assets/symbol/{symbol}` | Get asset by stock symbol | `symbol` (path) |
| GET | `/api/assets/status/{status}` | Get assets by status | `status` (path: OWNED, WATCHLIST, RESEARCH, SOLD) |

### Manage Assets
| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/assets/buy` | Record stock purchase | `{ symbol, quantity, price, date }` |
| POST | `/api/assets/sell` | Record stock sale | `{ symbol, quantity, price, date }` |
| POST | `/api/assets/watchlist` | Add stock to watchlist | `{ symbol, targetPrice }` |
| DELETE | `/api/assets/watchlist/{id}` | Remove from watchlist | - |
| PUT | `/api/assets/{id}` | Update asset settings | `{ targetPrice, notes, priorityRank, priceAlertsEnabled }` |
| DELETE | `/api/assets/{id}` | Delete asset | - |

**Asset Statuses:**
- **OWNED**: Currently owned stocks
- **WATCHLIST**: Stocks being monitored
- **RESEARCH**: Stocks under research
- **SOLD**: Previously owned, now sold

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

## 🌱 ESG Ratings API
**Base URL:** `/api/esg-ratings`

Environmental, Social, and Governance (ESG) sustainability ratings for stocks.

### Get ESG Ratings
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/esg-ratings` | Get all ESG ratings | - |
| GET | `/api/esg-ratings/{id}` | Get ESG rating by ID | `id` (path) |
| GET | `/api/esg-ratings/symbol/{symbol}` | Get ESG rating by stock symbol | `symbol` (path) |
| GET | `/api/esg-ratings/top-performers` | Top ESG performers | `limit` (query, default=10) |
| GET | `/api/esg-ratings/grade/{grade}` | Filter by grade (A+, A, A-, B+, etc.) | `grade` (path) |
| GET | `/api/esg-ratings/above-score` | Ratings above score threshold | `minScore` (query) |
| GET | `/api/esg-ratings/controversy/{level}` | By controversy level (0-4) | `level` (path) |
| GET | `/api/esg-ratings/low-controversy` | Low controversy ratings (0-2) | - |
| GET | `/api/esg-ratings/risk/{riskLevel}` | By risk level | `riskLevel` (path) |
| GET | `/api/esg-ratings/exists/{symbol}` | Check if rating exists | `symbol` (path) |

### ESG Pillar Scores
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/esg-ratings/high-environment` | High environment scores | `minScore` (query, default=75) |
| GET | `/api/esg-ratings/high-social` | High social scores | `minScore` (query, default=75) |
| GET | `/api/esg-ratings/high-governance` | High governance scores | `minScore` (query, default=75) |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/esg-ratings/statistics` | ESG statistics (averages, grade distribution) |

### Manage ESG Ratings
| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/esg-ratings` | Create new ESG rating | `{ symbol, totalScore, totalGrade, ... }` |
| PUT | `/api/esg-ratings/symbol/{symbol}` | Update ESG rating | `{ totalScore, totalGrade, ... }` |
| DELETE | `/api/esg-ratings/{id}` | Delete by ID | - |
| DELETE | `/api/esg-ratings/symbol/{symbol}` | Delete by symbol | - |

**ESG Rating Structure:**
- **Total Score**: 0-100 (Overall ESG performance)
- **Grades**: A+, A, A-, B+, B, B-, C+, C, C-
- **Pillars**: Environment, Social, Governance (each 0-100)
- **Controversy Level**: 0 (Negligible) to 4 (Severe)
- **Risk Level**: Negligible, Low, Medium, High, Severe

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

## 📦 Database Schema

**Core Tables (10):**
1. **asset_types** - Asset type reference (STOCK, ETF, CRYPTO, BOND, CASH)
2. **assets** - User's portfolio holdings and watchlist
3. **currencies** - Currency reference data
4. **dividends** - Dividend payment history
5. **esg_ratings** - ESG sustainability ratings
6. **market_data** - Real-time market prices and data
7. **news** - Financial news articles
8. **price_history** - Historical OHLCV price data
9. **transactions** - Buy/sell transaction history
10. **user_settings** - User preferences

---

**Total Controllers**: 5  
**Total Endpoints**: 55

Last Updated: February 3, 2026
