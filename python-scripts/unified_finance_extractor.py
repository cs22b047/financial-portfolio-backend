"""
Unified Financial Data Extractor with MySQL Integration
========================================================
Extracts comprehensive financial data from multiple APIs and inserts into MySQL:
- Yahoo Finance (free, no API key needed)
- Finnhub (API key required)

Data is structured to match the Portfolio Management Application schema.

Setup:
    1. Create .env file with:
       FINNHUB_API_KEY=your_finnhub_key
       DB_HOST=localhost
       DB_USER=root
       DB_PASSWORD=
       DB_NAME=portfolio_db

    2. Install dependencies:
       pip install yfinance requests pandas python-dotenv mysql-connector-python

Usage:
    python unified_finance_extractor.py
"""

import yfinance as yf
import requests
import pandas as pd
from datetime import datetime, timedelta
import os
import time
from typing import List, Dict, Optional
import mysql.connector
from mysql.connector import Error

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass


# =============================================================================
# DATABASE CONFIGURATION
# =============================================================================

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'portfolio_db')
}

# =============================================================================
# ENUM MAPPINGS (matching database schema)
# =============================================================================

ASSET_TYPE_MAPPING = {
    'STOCK': 1,
    'EQUITY': 1,
    'ETF': 1,
    'MUTUALFUND': 1,
    'BOND': 2,
    'CASH': 3,
    'CRYPTOCURRENCY': 4,
    'CRYPTO': 4
}

ASSET_STATUS = {
    'OWNED': 'OWNED',
    'WATCHLIST': 'WATCHLIST',
    'SOLD': 'SOLD',
    'PENDING_PURCHASE': 'PENDING_PURCHASE',
    'RESEARCH': 'RESEARCH'
}

TRANSACTION_TYPE = {
    'BUY': 'BUY',
    'SELL': 'SELL',
    'DIVIDEND': 'DIVIDEND',
    'STOCK_SPLIT': 'STOCK_SPLIT',
    'DEPOSIT': 'DEPOSIT',
    'WITHDRAWAL': 'WITHDRAWAL',
    'INTEREST_PAYMENT': 'INTEREST_PAYMENT',
    'STAKING_REWARD': 'STAKING_REWARD'
}

DIVIDEND_TYPE = {
    'CASH_DIVIDEND': 'CASH_DIVIDEND',
    'STOCK_DIVIDEND': 'STOCK_DIVIDEND',
    'INTEREST_PAYMENT': 'INTEREST_PAYMENT',
    'COUPON_PAYMENT': 'COUPON_PAYMENT',
    'SAVINGS_INTEREST': 'SAVINGS_INTEREST',
    'MONEY_MARKET_INTEREST': 'MONEY_MARKET_INTEREST',
    'STAKING_REWARD': 'STAKING_REWARD'
}

# =============================================================================
# STOCK AND INDEX LISTS
# =============================================================================

TOP_COMPANIES = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS",
    "VZ", "ADBE", "PM", "NEE", "WFC", "BAC", "RTX", "UPS", "COP", "QCOM"
]

# Benchmark indices for portfolio comparison (Performance Tracking module)
BENCHMARK_INDICES = {
    "^GSPC": "S&P 500",
    "^IXIC": "NASDAQ Composite",
    "^DJI": "Dow Jones Industrial Average",
    "^RUT": "Russell 2000"
}


# =============================================================================
# DATABASE INSERTER
# =============================================================================

class DatabaseInserter:
    """Handles all MySQL database insertions with proper schema mapping."""
    
    def __init__(self, db_config: dict):
        self.db_config = db_config
        self.connection = None
        
    def connect(self):
        """Establish database connection."""
        try:,
        db_config: dict = None
    ):
        self.finnhub_key = finnhub_api_key or os.getenv("FINNHUB_API_KEY")
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
        
        # Database inserter
        self.db_inserter = DatabaseInserter(db_config or DB_CONFIG)

        print("\n" + "=" * 70)
        print("UNIFIED FINANCIAL DATA EXTRACTOR WITH DATABASE INTEGRATION")
        print("(Structured for Portfolio Management Application)")
        print("=" * 70)
        print("\nAPI Status:")
        print(f"  Yahoo Finance: Available (no key needed)")
        print(f"  Finnhub:       {'Key found' if self.finnhub_key else 'No key (set FINNHUB_API_KEY)'}")
        print(f"\nDatabase: {db_config or DB_CONFIG
            print("✓ Database connection closed")
    
    def get_asset_type_id(self, quote_type: str) -> int:
        """Map quote type to asset_type_id."""
        return ASSET_TYPE_MAPPING.get(quote_type.upper(), 1)  # Default to STOCK
    
    def insert_market_data(self, data: dict) -> Optional[int]:
        """
        Insert or update market_data table.
        Returns market_data_id if successful.
        """
            hist = stock.history(period="5d")

            # Determine asset type based on quoteType
            quote_type = info.get("quoteType", "EQUITY")

            current_price = info.get("currentPrice") or info.get("regularMarketPrice")
            if current_price is None and not hist.empty:
                current_price = hist['Close'].iloc[-1]

            # Calculate day change from history
            previous_close = info.get("previousClose")
            day_change = None
            day_change_percent = None
            
            if current_price and previous_close:
                day_change = current_price - previous_close
                day_change_percent = (day_change / previous_close) * 100
            
            # Get today's high/low from history
            day_high = info.get("dayHigh")
            day_low = info.get("dayLow")
            if not hist.empty and len(hist) > 0:
                if day_high is None:
                    day_high = hist['High'].iloc[-1]
                if day_low is None:
                    day_low = hist['Low'].iloc[-1]

            return {
                # Core fields for market_data table
                "ticker": ticker,
                "name": info.get("longName") or info.get("shortName") or ticker,
                "quote_type": quote_type,
                "currentPrice": current_price,
                "previousClose": previous_close,
                "dayChange": day_change,
                "dayChangePercent": day_change_percent,
                "volume": info.get("volume"),
                "dayHigh": day_high,
                "dayLow": day_low,
                "currency": info.get("currency", "USD"),
                "exchange": info.get("exchange"),
                "bidPrice": info.get("bid"),
                "askPrice": info.get("ask"),

                # Market data fields
                "sector": info.get("sector"),
                "industry": info.get("industry"),
                "country": info.get("country"),
                "marketCap": info.get("marketCap"),

                # Risk metrics
                "beta": info.get("beta"),
                "52WeekHigh": info.get("fiftyTwoWeekHigh"),
                "52WeekLow": info.get("fiftyTwoWeekLow"),
                "50DayAverage": info.get("fiftyDayAverage"),
                "200DayAverage": info.get("twoHundredDayAverage"),

                # Performance metrics
                "peRatio": info.get("trailingPE"),
                "forwardPE": info.get("forwardPE"),
                "pegRatio": info.get("pegRatio"),
                "priceToBook": info.get("priceToBook"),
                "profitMargin": info.get("profitMargins"),
                "operatingMargin": info.get("operatingMargins"),
                "returnOnEquity": info.get("returnOnEquity"),
                "returnOnAssets": info.get("returnOnAssets"),
                "revenueGrowth": info.get("revenueGrowth"),
                "earningsGrowth": info.get("earningsGrowth"),

                # Dividend info
                "dividendYield": info.get("dividendYield"),
                "dividendRate": info.get("dividendRate"),
                "payoutRatio": info.get("payoutRatio"),

                # EPS data
                "trailingEPS": info.get("trailingEps"),
                "forwardEPS": info.get("forwardEps"),

                # Additional
                "debtToEquity": info.get("debtToEquity"),
                "currentRatio": info.get("currentRatio"),
                "bookValue": info.get("bookValu
            cursor.execute(sql, values)
            self.connection.commit()
            
            if not result:
                market_data_id = cursor.lastrowid
            
            return market_data_id
            
        except Error as e:
            print(f"    ✗ Error inserting market data for {data.get('ticker')}: {e}")
            self.connection.rollback()
            return None
        finally:
            cursor.close()
    
    def add_to_watchlist(self, ticker: str, target_price: float = None, notes: str = None) -> bool:
        """Add ticker to watchlist (status = WATCHLIST)."""
        cursor = self.connection.cursor()
        
        try:
            # Get market_data_id and asset_type_id
            cursor.execute(
                "SELECT id, asset_type_id, name, current_price FROM market_data WHERE symbol = %s",
                (ticker,)
            )
            result = cursor.fetchone()
            
            if not result:
                print(f"    ✗ Market data not found for {ticker}. Insert market data first.")
                return False
            
            market_data_id, asset_type_id, name, current_price = result
            
            # Check if already in watchlist
            cursor.execute(
                "SELECT id FROM assets WHERE asset_type_id = %s AND symbol = %s AND status = %s",
                (asset_type_id, ticker, 'WATCHLIST')
            )
            
            if cursor.fetchone():
                print(f"    → {ticker} already in watchlist")
                return True
            
            # Insert into assets as watchlist
            sql = """
                INSERT INTO assets (
                    asset_type_id, market_data_id, status, symbol, name,
                    current_price, target_price, added_to_watchlist_date,
                    price_alerts_enabled, notes, created_date, updated_date
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            values = (
                asset_type_id, market_data_id, 'WATCHLIST', ticker, name,
                current_price, target_price, datetime.now().date(),
                1 if target_price else 0, notes, datetime.now(), datetime.now()
            )
            
            cursor.execute(sql, values)
            self.connection.commit()
            print(f"    ✓ Added {ticker} to watchlist")
            return True
            
        except Error as e:
            print(f"    ✗ Error adding {ticker} to watchlist: {e}")
            self.connection.rollback()
            return False
        finally:
            cursor.close()
    
    def record_purchase(self, ticker: str, quantity: float, price: float, 
                       purchase_date: datetime = None, notes: str = None) -> bool:
        """Record an asset purchase (status = OWNED) and create BUY transaction."""
        cursor = self.connection.cursor()
        
        try:
            if purchase_date is None:
                purchase_date = datetime.now().date()
            
            # Get market_data_id and asset_type_id
            cursor.execute(
                "SELECT id, asset_type_id, name, current_price FROM market_data WHERE symbol = %s",
                (ticker,)
            )
            result = cursor.fetchone()
            
            if not result:
                print(f"    ✗ Market data not found for {ticker}")
                return False
            
            market_data_id, asset_type_id, name, current_price = result
            
            # Check if already owned
            cursor.execute(
                "SELECT id, quantity FROM assets WHERE asset_type_id = %s AND symbol = %s AND status = %s",
                (asset_type_id, ticker, 'OWNED')
            )
            existing = cursor.fetchone()
            
            if existing:
                # Update quantity (add to existing position)
                asset_id = existing[0]
                new_quantity = existing[1] + quantity
                cursor.execute(
                    "UPDATE assets SET quantity = %s, updated_date = %s WHERE id = %s",
                    (new_quantity, datetime.now(), asset_id)
                )
            else:
                # Insert new owned asset
                sql = """
                    INSERT INTO assets (
                        asset_type_id, market_data_id, status, symbol, name,
                        quantity, purchase_price, current_price, purchase_date,
                        notes, created_date, updated_date
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """
                values = (
                    asset_type_id, market_data_id, 'OWNED', ticker, name,
                    quantity, price, current_price, purchase_date,
                    notes, datetime.now(), datetime.now()
                )
                cursor.execute(sql, values)
                asset_id = cursor.lastrowid
            
            # Create BUY transaction
            trans_sql = """
                INSERT INTO transactions (
                    asset_id, transaction_type, quantity, price,
                    transaction_date, notes, currency, created_date, updated_date
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            trans_values = (
                asset_id, 'BUY', quantity, price, purchase_date,
                notes, 'USD', datetime.now(), datetime.now()
            )
            cursor.execute(trans_sql, trans_values)
            
            self.connection.commit()
            print(f"    ✓ Recorded purchase: {quantity} shares of {ticker} @ ${price}")
            return True
            
        except Error as e:
            print(f"    ✗ Error recording purchase for {ticker}: {e}")
            self.connection.rollback()
            return False
        finally:
            cursor.close()
    
    def insert_dividend(self, ticker: str, dividend_date: datetime, 
                       amount_per_share: float) -> bool:
        """Insert dividend record for an owned asset."""
        cursor = self.connection.cursor()
        
        try:
            # Get asset info
            cursor.execute(
                """SELECT a.id, a.quantity FROM assets a
                   JOIN market_data m ON a.market_data_id = m.id
                   WHERE m.symbol = %s AND a.status = 'OWNED'""",
                (ticker,)
            )
            result = cursor.fetchone()
            
            if not result:
                return False  # Asset not owned, skip silently
            
            asset_id, shares_held = result
            total_amount = amount_per_share * shares_held
            
            # Check if dividend already recorded
            cursor.execute(
                "SELECT id FROM dividends WHERE asset_id = %s AND payment_date = %s",
                (asset_id, dividend_date.date())
            )
            
            if cursor.fetchone():
                return True  # Already recorded
            
            # Insert simplified dividend
            sql = """
                INSERT INTO dividends (
                    asset_id, payment_date, amount_per_share,
                    shares_held, total_amount, currency
                ) VALUES (%s, %s, %s, %s, %s, %s)
            """
            values = (
                asset_id, dividend_date.date(), amount_per_share,
                shares_held, total_amount, 'USD'
            )
            cursor.execute(sql, values)
            
            # Also create DIVIDEND transaction
            trans_sql = """
                INSERT INTO transactions (
                    asset_id, transaction_type, quantity, price,
                    transaction_date, currency
                ) VALUES (%s, %s, %s, %s, %s, %s)
            """
            trans_values = (
                asset_id, 'DIVIDEND', shares_held, amount_per_share,
                dividend_date.date(), 'USD'
            )
            cursor.execute(trans_sql, trans_values)
            
            self.connection.commit()
            return True
            
        except Error as e:
            print(f"    ✗ Error inserting dividend for {ticker}: {e}")
            self.connection.rollback()
            return False
        finally:
            cursor.close()


# =============================================================================
# UNIFIED DATA EXTRACTOR
# =============================================================================

class UnifiedFinanceExtractor:
    """
    Unified extractor that pulls data from Yahoo Finance and Finnhub.
    Structures data according to Portfolio Management Application schema.
    """

    def __init__(
        self,
        finnhub_api_key: str = None,
        output_dir: str = "new_financial_data"
    ):
        self.finnhub_key = finnhub_api_key or os.getenv("FINNHUB_API_KEY")
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        print("\n" + "=" * 70)
        print("UNIFIED FINANCIAL DATA EXTRACTOR")
        print("(Structured for Portfolio Management Application)")
        print("=" * 70)
        print("\nAPI Status:")
        print(f"  Yahoo Finance: Available (no key needed)")
        print(f"  Finnhub:       {'Key found' if self.finnhub_key else 'No key (set FINNHUB_API_KEY)'}")

    # =========================================================================
    # YAHOO FINANCE METHODS
    # =========================================================================

    def _yf_get_asset_data(self, ticker: str) -> dict:
        """
        Get asset data matching the schema:
        - ticker, name, type (stocks/bonds/cash/other)
        - currentPrice, marketCap, sector, industry
        - Risk metrics: beta, volatility
        - Dividend info
        """
        try:
            stock = yf.Ticker(ticker)
            info = stock.info

            # Determine asset type based on quoteType (matches schema enum)
            quote_type = info.get("quoteType", "EQUITY")
            if quote_type == "ETF":
                asset_type = "other"
            elif quote_type == "MUTUALFUND":
                asset_type = "other"
            elif quote_type in ["EQUITY", "STOCK"]:
                asset_type = "stocks"
            else:
                asset_type = "other"

            current_price = info.get("currentPrice") or info.get("regularMarketPrice") or 0

            return {
                # Core asset fields (matches assets table in schema)
                "ticker": ticker,
                "name": info.get("longName") or info.get("shortName") or ticker,
                "type": asset_type,  # stocks, bonds, cash, other
                "currentPrice": current_price,
                "currency": info.get("currency", "USD"),

                # Additional fields for portfolio management
                "sector": info.get("sector"),
                "industry": info.get("industry"),
                "country": info.get("country"),
                "exchange": info.get("exchange"),
                "marketCap": info.get("marketCap"),

                # Risk metrics (for Risk Management module)
                "nd_insert(self, tickers: List[str], add_to_watchlist: bool = True, 
                          delay: float = 0.5) -> Dict[str, List]:
        """
        Extract data and insert into MySQL database.
        
        Args:
            tickers: List of ticker symbols to extract
            add_to_watchlist: If True, adds tickers to watchlist. If False, only updates market_data
            delay: Delay between API calls
        """
        print(f"\n{'='*70}")
        print(f"EXTRACTING AND INSERTING DATA FOR {len(tickers)} COMPANIES")
        print(f"{'='*70}")
        
        # Connect to database
        if not self.db_inserter.connect():
            print("✗ Cannot proceed without database connection")
            return {}

        # Initialize data storage for CSV export
        data = {
            "market_data": [],
            "price_history": [],
            "benchmark_indices": [],
            "income_statements": [],
            "balance_sheets": [],
            "cash_flows": [],
            "dividends": [],
            "dividend_calendar": [],
            "analyst_recommendations": [],
            "price_targets": [],
            "insider_trading": [],
            "institutional_holders": [],
            "news": [],urns for volatility calculation.
        """
        try:
            stock = yf.Ticker(ticker)
            hist = stock.history(period=period)
            if not hist.empty:
                hist = hist.reset_index()
                hist["ticker"] = ticker
                # Calculate daily returns for volatility (Risk Management module)
                hist["dailyReturn"] = hist["Close"].pct_change()
                return hist[["Date", "ticker", "Open", "High", "Low", "Close", "Volume", "Dividends", "Stock Splits", "dailyReturn"]]
            return pd.DataFrame()
        except:
            return pd.DataFrame()

    def _yf_get_dividends(self, ticker: str) -> pd.DataFrame:
        """Get dividend history for Transactions module (dividend type)."""
        try:
            stock = yf.Ticker(ticker)
            divs = stock.div AND INSERT TO DATABASE
        # -----------------------------------------------------------------
        for i, ticker in enumerate(tickers):
            print(f"\n[{i+1}/{len(tickers)}] {ticker}")
            print("-" * 50)

            # -----------------------------------------------------------------
            # YAHOO FINANCE
            # -----------------------------------------------------------------
            print("  Yahoo Finance:")

            # Asset data (insert into market_data table)
            print("    - Market data...", end=" ")
            asset_data = self._yf_get_asset_data(ticker)
            if "error" not in asset_data:
                data["market_data"].append(asset_data)
                
                # Insert into database
                market_data_id = self.db_inserter.insert_market_data(asset_data)
                if market_data_id:
                    print(f"OK (ID: {market_data_id})")
                    
                    # Add to watchlist if requested
                    if add_to_watchlist:
                        self.db_inserter.add_to_watchlist(
                            ticker, 
                            target_price=None,
                            notes=f"Auto-added on {datetime.now().date()}"
                        )
                else:
                    print("INSERTEDinfo

            result = {
                "ticker": ticker,
                "dividendRate": info.get("dividendRate"),
                "dividendYield": info.get("dividendYield"),
                "exDividendDate": None,
                "paymentDate": None,
                "lastDividendValue": info.get("lastDividendValue"),
                "lastDividendDate": None,
            }

            # Get ex-dividend date
            ex_div = info.get("exDividendDate")
            if ex_div:
                if isinstance(ex_div, (int, float)):
                    result["exDividendDate"] = datetime.fromtimestamp(ex_div).strftime("%Y-%m-%d")
                else:
                    result["exDividendDate"] = str(ex_div)

            # Get last dividend date
            last_div_date = info.get("lastDividendDate")
            if last_div_date:
                if isinstance(last_div_date, (int, float)):
                    result["lastDividendDate"] = datetime.fromtimestamp(last_div_date).strftime("%Y-%m-%d")

            # Try to get calendar data
            try:
                calendar = stock.calendar
                if calendar is not None and isinstance(calendar, dict):
                    if "Dividend Date" in calendar:
                        result["paymentDate"] = str(calendar["Dividend Date"])
                    if "Ex-Dividend Date" in calendar:
                        result["exDividendDate"] = str(calendar["Ex-Dividend Date"])
            except:
                pass

            return result
        except:
            return {"ticker": ticker}

    def _yf_get_financials(self, ticker: str) -> Dict[str, pd.DataFrame]:
        """Get financial statements."""
        result = {"income": pd.DataFrame(), "balance": pd.DataFrame(), "cashflow": pd.DataFrame()}
        try:
            stock = yf.Ticker(ticker)

            inc = stock.income_stmt
            if inc is not None and not inc.empty:
                inc = inc.T.reset_index()
                inc["ticker"] = ticker
                result["income"] = inc

            bal = stock.balance_sheet
            if bal is not None and not bal.empty:
                bal = bal.T.reset_index()
                bal["ticker"] = ticker
                result["balance"] = bal

            cf = stock.cashflow
            if cf is not None and not cf.empty:
                cf = cf.T.reset_index()
                cf["ticker"] = ticker
                result["cashflow"] = cf

        except:
            pass
        return result

    def _yf_get_recommendations(self, ticker: str) -> pd.DataFrame:
        """Get analyst recommendations."""
        try:
            stock = yf.Ticker(ticker)
            recs = stock.recommendations
            if recs is not None and not recs.empty:
                recs = recs.reset_index()
                recs["ticker"] = ticker
                return recs
            return pd.DataFrame()
        except:
            return pd.DataFrame()

    def _yf_get_holders(self, ticker: str) -> pd.DataFrame:
        """Get institutional holders."""
        try:
            stock = yf.Ticker(ticker)
            holders = stock.institutional_holders
            if holders is not None and not holders.empty:
                holders["ticker"] = ticker
                return holders
            return pd.DataFrame()
        except:
            return pd.DataFrame()

    # =========================================================================
    # BENCHMARK INDEX METHODS (for Performance Tracking module)
    # =========================================================================

    def _get_benchmark_data(self, period: str = "2y") -> pd.DataFrame:
        """
        Get benchmark index data for portfolio comparison.
        Required for: "Portfolio performance vs benchmark (e.g., S&P 500)"
        """
        print("\n  Extracting Benchmark Indices...")
        all_benchmarks = []

        for symbol, name in BENCHMARK_INDICES.items():
            print(f"    - {name} ({symbol})...", end=" ")
            try:
                index = yf.Ticker(symbol)
                hist = index.history(period=period)
                if not hist.empty:
                    hist = hist.reset_index()
                    hist["ticker"] = symbol
                    hist["name"] = name
                    hist["dailyReturn"] = hist["Close"].pct_change()
                    all_benchmarks.append(hist[["Date", "ticker", "name", "Open", "High", "Low", "Close", "Volume", "dailyReturn"]])
                    print(f"OK ({len(hist)} days)")
                else:
                    print("SKIP")
            except Exception as e:
                print(f"ERROR: {e}")
            time.sleep(0.3)

        if all_benchmarks:
            return pd.concat(all_benchmarks, ignore_index=True)
        return pd.DataFrame()

    # =========================================================================
    # FINNHUB METHODS
    # =========================================================================

    def _fh_request(self, endpoint: str, params: dict = None) -> dict:
        """Make Finnhub API request."""
        if not self.finnhub_key:
            return {"error": "No Finnhub API key"}

        if params is None:
            params = {}
        params["token"] = self.finnhub_key

        url = f"https://finnhub.io/api/v1/{endpoint}"

        try:
            response = requests.get(url, params=params, timeout=30)
            if response.status_code == 403:
                return {"error": "Finnhub premium required"}
            if response.status_code == 429:
                return {"error": "Rate limit exceeded"}
            response.raise_for_status()
            data = response.json()
            if data == {} or data == []:
                return {"error": "No data"}
            return data
        except Exception as e:
            return {"error": str(e)}

    def _fh_get_price_target(self, ticker: str) -> dict:
        """Get analyst price targets for Price Target display."""
        data = self._fh_request("stock/price-target", {"symbol": ticker})
        if "error" not in data:
            return {
                "ticker": ticker,
                "targetHigh": data.get("targetHigh"),
                "targetLow": data.get("targetLow"),
                "targetMean": data.get("targetMean"),
                "targetMedian": data.get("targetMedian"),
                "lastUpdated": data.get("lastUpdated"),
            }
        return {"ticker": ticker, "error": data.get("error")}

    def _fh_get_metrics(self, ticker: str) -> dict:
        """Get basic financials/metrics from Finnhub for Risk Management."""
        data = self._fh_request("stock/metric", {"symbol": ticker, "metric": "all"})
        if "error" not in data and "metric" in data:
            metrics = data["metric"]
            return {
                "ticker": ticker,
                "beta": metrics.get("beta"),
                "52WeekHigh": metrics.get("52WeekHigh"),
                "52WeekLow": metrics.get("52WeekLow"),
                "peRatioAnnual": metrics.get("peAnnual"),
                "pbRatioAnnual": metrics.get("pbAnnual"),
                "dividendYieldTTM": metrics.get("currentDividendYieldTTM"),
                "epsGrowth5Y": metrics.get("epsGrowth5Y"),
                "revenueGrowth5Y": metrics.get("revenueGrowth5Y"),
                "roeTTM": metrics.get("roeTTM"),
                "roaTTM": metrics.get("roaTTM"),
                "currentRatioAnnual": metrics.get("currentRatioAnnual"),
                "debtEquityAnnual": metrics.get("totalDebt/totalEquityAnnual"),
            }
        return {"ticker": ticker}

    def _fh_get_recommendations(self, ticker: str) -> List[dict]:
        """Get analyst recommendations from Finnhub."""
        data = self._fh_request("stock/recommendation", {"symbol": ticker})
        if isinstance(data, list):
            for item in data:
                item["ticker"] = ticker
            return data
        return []

    def _fh_get_insider(self, ticker: str) -> List[dict]:
        """Get insider transactions from Finnhub."""
        data = self._fh_request("stock/insider-transactions", {"symbol": ticker})
        if isinstance(data, dict) and "data" in data:
            for item in data["data"]:
                item["ticker"] = ticker
            return data["data"]
        return []

    def _fh_get_news(self, ticker: str) -> List[dict]:
        """Get company news from Finnhub for Alerts module."""
        from_date = (datetime.now() - timedelta(days=30)).strftime("%Y-%m-%d")
        to_date = datetime.now().strftime("%Y-%m-%d")

        data = self._fh_request("company-news", {
            "symbol": ticker,
            "from": from_date,
            "to": to_date
        })

        if isinstance(data, list):
            for item in data:
                item["ticker"] = ticker
            return data[:20]
        return []

    # =========================================================================
    # VOLATILITY CALCULATION (for Risk Management module)
    # =========================================================================

    def calculate_volatility(self, price_history_df: pd.DataFrame) -> pd.DataFrame:
        """
        Calculate volatility metrics for each ticker.
        Required for Risk Management module: Volatility, Beta, VaR
        """
        if price_history_df.empty:
            return pd.DataFrame()

        volatility_data = []
        for ticker in price_history_df["ticker"].unique():
            ticker_data = price_history_df[price_history_df["ticker"] == ticker]
            daily_returns = ticker_data["dailyReturn"].dropna()
            if len(daily_returns) > 20:
                # Annualized volatility (252 trading days)
                vol = daily_returns.std() * (252 ** 0.5)
                # Value at Risk (95% confidence)
                var_95 = daily_returns.quantile(0.05)
                volatility_data.append({
                    "ticker": ticker,
                    "volatility": round(vol, 4),
                    "avgDailyReturn": round(daily_returns.mean(), 6),
                    "maxDailyReturn": round(daily_returns.max(), 4),
                    "minDailyReturn": round(daily_returns.min(), 4),
                    "valueAtRisk95": round(var_95, 4),
                    "dataPoints": len(daily_returns)
                })

        return pd.DataFrame(volatility_data)

    # =========================================================================
    # UNIFIED EXTRACTION
    # =========================================================================

    def extract_all(self, tickers: List[str], delay: float = 0.5) -> Dict[str, List]:
        """
        Extract all data from all sources for given tickers.
        Data is structured to match Portfolio Management Application schema.
        """
        print(f"\n{'='*70}")
        print(f"EXTRACTING DATA FOR {len(tickers)} COMPANIES")
        print(f"{'='*70}")

        # Initialize data storage matching schema
        data = {
            # Core asset data (matches assets table)
            "assets": [],

            # Price history (for Performance Tracking)
            "price_history": [],

            # Benchmark indices (for portfolio vs benchmark comparison)
            "benchmark_indices": [],

            # Financial statements
            "income_statements": [],
            "balance_sheets": [],
            "cash_flows": [],

            # Dividend data (for Transactions and Alerts)
            "dividends": [],
            "dividend_calendar": [],

            # Analyst data
            "analyst_recommendations": [],
            "price_targets": [],

            # Trading data
            "insider_trading": [],
            "institutional_holders": [],

            # News (for Alerts)
            "news": [],

            # Risk metrics
            "finnhub_metrics": [],
        }

        # -----------------------------------------------------------------
        # EXTRACT BENCHMARK DATA FIRST (for Performance Tracking module)
        # -----------------------------------------------------------------
        benchmark_df = self._get_benchmark_data()
        if not benchmark_df.empty:
            data["benchmark_indices"].append(benchmark_df)

        # -----------------------------------------------------------------
        # EXTRACT STOCK DATA
        # -----------------------------------------------------------------
        for i, ticker in enumerate(tickers):
            print(f"\n[{i+1}/{len(tickers)}] {ticker}")
            print("-" * 50)

            # -----------------------------------------------------------------
            # YAHOO FINANCE
            # -----------------------------------------------------------------
            print("  Yahoo Finance:")

            # Asset dat (insert for OWNED assets only)
            print("    - Dividends...", end=" ")
            divs = self._yf_get_dividends(ticker)
            if not divs.empty:
                data["dividends"].append(divs)
                
                # Try to insert dividends (will only work if asset is OWNED)
                inserted_count = 0
                for _, row in divs.iterrows():
                    if self.db_inserter.insert_dividend(
                        ticker, 
                        row['date'].to_pydatetime(), 
                        row['dividend']
                    ):
                        inserted_count += 1
                
                if inserted_count > 0:
                    print(f"OK ({len(divs)} found, {inserted_count} inserted)")
                else:
                    print(f"OK ({len(divs)} found, 0 inserted - asset not owned
            else:
                print("SKIP")
            time.sleep(delay)

            # Price history
            print("    - Price history...", end=" ")
            prices = self._yf_get_price_history(ticker)
            if not prices.empty:
                data["price_history"].append(prices)
                print(f"OK ({len(prices)} days)")
            else:
                print("SKIP")
            time.sleep(delay)

            # Financial statements
            print("    - Financial statements...", end=" ")
            fin = self._yf_get_financials(ticker)
            if not fin["income"].empty:
                data["income_statements"].append(fin["income"])
            if not fin["balance"].empty:
                data["balance_sheets"].append(fin["balance"])
            if not fin["cashflow"].empty:
                data["cash_flows"].append(fin["cashflow"])
            print("OK")
            time.sleep(delay)

            # Dividends
            print("    - Dividends...", end=" ")
            divs = self._yf_get_dividends(ticker)
            if not divs.empty:
                data["dividends"].append(divs)
                print(f"OK ({len(divs)})")
            else:
                print("SKIP")
            time.sleep(delay)

            # Dividend calendar (for Alerts)
            print("    - Dividend calendar...", end=" ")
            div_cal = self._yf_get_dividend_calendar(ticker)
            if div_cal.get("dividendRate") or div_cal.get("exDividendDate"):
                data["dividend_calendar"].append(div_cal)
                print("OK")
            else:
                print("SKIP")
            time.sleep(delay)

            # Recommendations
            print("    - Analyst recommendations...", end=" ")
            recs = self._yf_get_recommendations(ticker)
            if not recs.empty:
                data["analyst_recommendations"].append(recs)
                print("OK")
            else:
                print("SKIP")
            time.sleep(delay)

            # Institutional holders
            print("    - Institutional holders...", end=" ")
            holders = self._yf_get_holders(ticker)
            if not holders.empty:
                data["institutional_holders"].append(holders)
                print("OK")
            else:
                print("SKIP")
            time.sleep(delay)

            # -----------------------------------------------------------------
            # FINNHUB
            # -----------------------------------------------------------------
            if self.finnhub_key:
                print("  Finnhub:")

                # Price target
                print("    - Price target...", end=" ")
                target = self._fh_get_price_target(ticker)
                if "error" not in target:
                    data["price_targets"].append(target)
                    print("OK")
                else:
                    print("SKIP")
                time.sleep(delay)

                # Metrics (for Risk Management)
                print("    - Risk metrics...", end=" ")
                metrics = self._fh_get_metrics(ticker)
                if metrics.get("beta") is not None:
                    data["finnhub_metrics"].append(metrics)
                    print("OK")
                else:
                    print("SKIP")
                time.sleep(delay)

                # Recommendations
                print("    - Recommendations...", end=" ")
                fh_recs = self._fh_get_recommendations(ticker)
                if fh_recs:
                    for rec in fh_recs:
                        data["analyst_recommendations"].append(pd.DataFrame([rec]))
                    print(f"OK ({len(fh_recs)})")
                else:
                    print("SKIP")
                time.sleep(delay)

                # Insider transactions
                print("    - Insider transactions...", end=" ")
                insider = self._fh_get_insider(ticker)
                data["insider_trading"].extend(insider)
                print(f"OK ({len(insider)})")
                time.sleep(delay)

                # News
                print("    - News...", end=" ")
                news = self._fh_get_news(ticker)
                data["news"].extend(news)
                print(f"OK ({len(news)})")
                time.sleep(delay)
        
        # Disconnect from database
        self.db_inserter.disconnect()
        
        return data
    
    def extract_all(self, tickers: List[str], delay: float = 0.5) -> Dict[str, List]:
        """
        Legacy method - extracts data without database insertion (CSV only).
        Use extract_and_insert() for database integration.
        """
        return self.extract_and_insert(tickers, add_to_watchlist=False, delay=delay)
will be:")
    print("  ✓ Inserted into MySQL database (market_data table)")
    print("  ✓ Added to watchlist (assets table with status=WATCHLIST)")
    print("  ✓ Saved to CSV files for backup")
    print("\nData types (matching Portfolio Management schema):")
    print("  [Market Data]  Inserted into market_data table")
    print("  [Assets]       Added to watchlist by default")
    print("  [Benchmarks]   S&P 500, NASDAQ, Dow Jones, Russell 2000")
    print("  [Performance]  Price history with daily returns")
    print("  [Risk]         Volatility, Beta, VaR, metrics")
    print("  [Dividends]    History + Calendar (for alerts)")
    print("  [Analyst]      Recommendations + Price Targets")
    print("  [Financials]   Income, Balance Sheet, Cash Flow")
    print("  [Trading]      Insider transactions, Institutional holders")
    print("  [Alerts]       News articles")

    # Extract and insert into database
    all_data = extractor.extract_and_insert(tickers, add_to_watchlist=True, delay=0.5)

    # Calculate volatility from price history (Risk Management module)
    if all_data.get("price_history"):
        print("\nCalculating volatility metrics (Risk Management)...")
        price_df = pd.concat(all_data["price_history"], ignore_index=True)
        volatility_df = extractor.calculate_volatility(price_df)
        if not volatility_df.empty:
            all_data["volatility_metrics"] = [volatility_df]
            print(f"  Calculated for {len(volatility_df)} tickers")

    # Save to CSV for backup
    extractor.save_to_csv(all_data)

    print("\n" + "=" * 70)
    print("EXTRACTION AND DATABASE INSERTION COMPLETE!")
    print("=" * 70)
    print("\nDatabase Operations:")
    print("  ✓ market_data table updated with latest prices")
    print("  ✓ Stocks added to watchlist (view in assets table)")
    print("\nCSV Backup Files:")
    print("  - market_data.csv      -> Market data backup")
    print("  - benchmark_indices.csv -> Performance tracking")
    print("  - price_targets.csv    -> Analyst targets")
    print("  - dividend_calendar.csv -> Upcoming dividends")
    print("  - volatility_metrics.csv -> Risk metrics")
    
    print("\n" + "=" * 70)
    print("EXAMPLE USAGE FOR PORTFOLIO MANAGEMENT:")
    print("=" * 70)
    print("\n# To record a purchase:")
    print("extractor.db_inserter.connect()")
    print("extractor.db_inserter.record_purchase('AAPL', 100, 150.50)")
    print("extractor.db_inserter.disconnect()")
    
    print("\n# To add to watchlist with target price:")
    print("extractor.db_inserter.connect()")
    print("extractor.db_inserter.add_to_watchlist('GOOGL', target_price=130.00)")
    print("extractor.db_inserter.disconnect(
    extractor = UnifiedFinanceExtractor(output_dir="new_financial_data")

    # Select companies (change slice for more/fewer)
    tickers = TOP_COMPANIES[:10]  # Start with 10 for testing

    print(f"\nCompanies to extract: {', '.join(tickers)}")
    print("\nData types (matching Portfolio Management schema):")
    print("  [Assets]       ticker, name, type, currentPrice, beta")
    print("  [Benchmarks]   S&P 500, NASDAQ, Dow Jones, Russell 2000")
    print("  [Performance]  Price history with daily returns")
    print("  [Risk]         Volatility, Beta, VaR, metrics")
    print("  [Dividends]    History + Calendar (for alerts)")
    print("  [Analyst]      Recommendations + Price Targets")
    print("  [Financials]   Income, Balance Sheet, Cash Flow")
    print("  [Trading]      Insider transactions, Institutional holders")
    print("  [Alerts]       News articles")

    # Extract all data
    all_data = extractor.extract_all(tickers, delay=0.5)

    # Calculate volatility from price history (Risk Management module)
    if all_data["price_history"]:
        print("\nCalculating volatility metrics (Risk Management)...")
        price_df = pd.concat(all_data["price_history"], ignore_index=True)
        volatility_df = extractor.calculate_volatility(price_df)
        if not volatility_df.empty:
            all_data["volatility_metrics"] = [volatility_df]
            print(f"  Calculated for {len(volatility_df)} tickers")

    # Save to CSV
    extractor.save_to_csv(all_data)

    print("\n" + "=" * 70)
    print("EXTRACTION COMPLETE!")
    print("=" * 70)
    print("\nData files match Portfolio Management Application schema:")
    print("  - assets.csv           -> Assets table (ticker, name, type, currentPrice)")
    print("  - benchmark_indices.csv -> Performance Tracking (portfolio vs S&P 500)")
    print("  - price_targets.csv    -> Analyst price targets")
    print("  - dividend_calendar.csv -> Upcoming dividends (for Alerts)")
    print("  - volatility_metrics.csv -> Risk Management (volatility, VaR)")
