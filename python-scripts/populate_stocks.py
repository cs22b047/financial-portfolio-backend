"""
Simple Stock Data Populator for Portfolio Database
===================================================
Fetches real stock data and populates the database with market_data.
Uses Yahoo Finance (free, no API key needed).
"""

import yfinance as yf
import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime
from typing import Optional

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

# Database configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'portfolio_db')
}

# Top 50 US stocks to populate
STOCKS = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS",
    "VZ", "ADBE", "PM", "NEE", "WFC", "BAC", "RTX", "UPS", "COP", "QCOM"
]

# Asset type mapping
ASSET_TYPE_MAP = {
    'EQUITY': 1,
    'ETF': 1,
    'MUTUALFUND': 1,
    'STOCK': 1
}

class StockDataPopulator:
    def __init__(self):
        self.connection = None
        
    def connect(self):
        """Establish database connection."""
        try:
            self.connection = mysql.connector.connect(**DB_CONFIG)
            if self.connection.is_connected():
                print(f"✓ Connected to MySQL database: {DB_CONFIG['database']}")
                return True
        except Error as e:
            print(f"✗ Database connection failed: {e}")
            return False
    
    def close(self):
        """Close database connection."""
        if self.connection and self.connection.is_connected():
            self.connection.close()
            print("✓ Database connection closed")
    
    def ensure_asset_types(self):
        """Ensure asset_type table has required entries."""
        try:
            cursor = self.connection.cursor()
            
            # Insert STOCK asset type if doesn't exist
            cursor.execute("""
                INSERT IGNORE INTO asset_type (id, name, description)
                VALUES (1, 'STOCK', 'Common stocks and equities')
            """)
            
            self.connection.commit()
            cursor.close()
            print("✓ Asset types verified")
            
        except Error as e:
            print(f"✗ Failed to setup asset types: {e}")
    
    def fetch_stock_data(self, ticker: str) -> Optional[dict]:
        """Fetch stock data from Yahoo Finance."""
        try:
            stock = yf.Ticker(ticker)
            info = stock.info
            hist = stock.history(period="5d")
            
            # Get current price
            current_price = info.get("currentPrice") or info.get("regularMarketPrice")
            if current_price is None and not hist.empty:
                current_price = float(hist['Close'].iloc[-1])
            
            # Get previous close
            previous_close = info.get("previousClose")
            if previous_close is None and len(hist) > 1:
                previous_close = float(hist['Close'].iloc[-2])
            
            # Calculate changes
            day_change = None
            day_change_percent = None
            if current_price and previous_close:
                day_change = current_price - previous_close
                day_change_percent = (day_change / previous_close) * 100
            
            # Get high/low
            day_high = info.get("dayHigh")
            day_low = info.get("dayLow")
            if not hist.empty:
                if day_high is None:
                    day_high = float(hist['High'].iloc[-1])
                if day_low is None:
                    day_low = float(hist['Low'].iloc[-1])
            
            return {
                "symbol": ticker,
                "name": info.get("longName") or info.get("shortName") or ticker,
                "asset_type_id": 1,  # STOCK
                "current_price": current_price,
                "previous_close": previous_close,
                "day_change": day_change,
                "day_change_percent": day_change_percent,
                "volume": info.get("volume"),
                "day_high": day_high,
                "day_low": day_low,
                "week_52_high": info.get("fiftyTwoWeekHigh"),
                "week_52_low": info.get("fiftyTwoWeekLow"),
                "market_cap": info.get("marketCap"),
                "pe_ratio": info.get("trailingPE"),
                "dividend_yield": info.get("dividendYield"),
                "currency": info.get("currency", "USD"),
                "exchange": info.get("exchange"),
                "sector": info.get("sector"),
                "industry": info.get("industry")
            }
            
        except Exception as e:
            print(f"  ✗ Failed to fetch {ticker}: {str(e)[:50]}")
            return None
    
    def insert_stock(self, data: dict) -> bool:
        """Insert or update stock in market_data table."""
        try:
            cursor = self.connection.cursor()
            
            # Check if stock already exists
            cursor.execute("SELECT id FROM market_data WHERE symbol = %s", (data['symbol'],))
            existing = cursor.fetchone()
            
            if existing:
                # Update existing record
                sql = """
                    UPDATE market_data SET
                        name = %s,
                        asset_type_id = %s,
                        current_price = %s,
                        previous_close = %s,
                        day_change = %s,
                        day_change_percent = %s,
                        volume = %s,
                        day_high = %s,
                        day_low = %s,
                        week_52_high = %s,
                        week_52_low = %s,
                        market_cap = %s,
                        pe_ratio = %s,
                        dividend_yield = %s,
                        currency = %s,
                        exchange = %s,
                        sector = %s,
                        industry = %s,
                        last_updated = NOW()
                    WHERE symbol = %s
                """
                values = (
                    data['name'], data['asset_type_id'], data['current_price'],
                    data['previous_close'], data['day_change'], data['day_change_percent'],
                    data['volume'], data['day_high'], data['day_low'],
                    data['week_52_high'], data['week_52_low'], data['market_cap'],
                    data['pe_ratio'], data['dividend_yield'], data['currency'],
                    data['exchange'], data['sector'], data['industry'],
                    data['symbol']
                )
                cursor.execute(sql, values)
                action = "Updated"
            else:
                # Insert new record
                sql = """
                    INSERT INTO market_data (
                        symbol, name, asset_type_id, current_price, previous_close,
                        day_change, day_change_percent, volume, day_high, day_low,
                        week_52_high, week_52_low, market_cap, pe_ratio, dividend_yield,
                        currency, exchange, sector, industry, last_updated
                    ) VALUES (
                        %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                        %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW()
                    )
                """
                values = (
                    data['symbol'], data['name'], data['asset_type_id'],
                    data['current_price'], data['previous_close'], data['day_change'],
                    data['day_change_percent'], data['volume'], data['day_high'],
                    data['day_low'], data['week_52_high'], data['week_52_low'],
                    data['market_cap'], data['pe_ratio'], data['dividend_yield'],
                    data['currency'], data['exchange'], data['sector'], data['industry']
                )
                cursor.execute(sql, values)
                action = "Inserted"
            
            self.connection.commit()
            cursor.close()
            
            price_str = f"${data['current_price']:.2f}" if data['current_price'] else "N/A"
            print(f"  ✓ {action} {data['symbol']:6s} - {data['name'][:40]:40s} {price_str}")
            return True
            
        except Error as e:
            print(f"  ✗ Failed to insert {data['symbol']}: {e}")
            return False
    
    def populate_all_stocks(self):
        """Main function to populate all stocks."""
        print("\n" + "=" * 80)
        print("STOCK DATA POPULATOR - Portfolio Management System")
        print("=" * 80)
        print(f"\nFetching data for {len(STOCKS)} stocks from Yahoo Finance...")
        print("-" * 80)
        
        success_count = 0
        fail_count = 0
        
        for i, ticker in enumerate(STOCKS, 1):
            print(f"[{i}/{len(STOCKS)}] {ticker}...", end=" ")
            
            # Fetch data
            data = self.fetch_stock_data(ticker)
            if not data:
                fail_count += 1
                continue
            
            # Insert to database
            if self.insert_stock(data):
                success_count += 1
            else:
                fail_count += 1
        
        print("-" * 80)
        print(f"\n✓ Population complete!")
        print(f"  Success: {success_count}/{len(STOCKS)}")
        print(f"  Failed:  {fail_count}/{len(STOCKS)}")
        print("=" * 80 + "\n")

def main():
    """Main execution."""
    populator = StockDataPopulator()
    
    if not populator.connect():
        print("Failed to connect to database. Check your configuration.")
        return
    
    try:
        # Ensure asset_type table has entries
        populator.ensure_asset_types()
        
        # Populate stocks
        populator.populate_all_stocks()
        
    finally:
        populator.close()

if __name__ == "__main__":
    main()
