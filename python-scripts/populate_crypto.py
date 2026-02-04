"""
Cryptocurrency Data Populator for Portfolio Database
=====================================================
Unified script that fetches both real and mock cryptocurrency data:
1. Real data from CoinGecko API (rate-limited)
2. Mock data generation (no API calls, for additional cryptos)
3. market_data table with latest crypto prices
4. price_history table with historical price data

CoinGecko Free API - No API key required
Rate Limit: 10-30 calls/minute (free tier)
"""

import requests
import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime, timedelta
from typing import Optional, List
import time
import random

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

# API configuration
COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3"
CRYPTO_ASSET_TYPE_ID = 3  # From asset_types table

# Request configuration
REQUEST_TIMEOUT = 15
RATE_LIMIT_DELAY = 2.5  # seconds between API requests (to stay within free tier limits)

# Top cryptocurrencies to populate from API
TOP_CRYPTOS = [
    'bitcoin', 'ethereum', 'tether', 'binancecoin', 'solana',
    'ripple', 'usd-coin', 'cardano', 'dogecoin', 'tron',
    'avalanche-2', 'chainlink', 'polkadot', 'matic-network', 'litecoin',
    'bitcoin-cash', 'stellar', 'uniswap', 'cosmos', 'monero',
    'ethereum-classic', 'filecoin', 'hedera-hashgraph', 'aptos', 'vechain',
    'internet-computer', 'near', 'algorand', 'optimism', 'aave'
]

# Mock cryptocurrencies (Symbol, Name, Base Price, Volatility) - used as fallback
MOCK_CRYPTOS = [
    ('BNB', 'Binance Coin', 310.50, 0.15),
    ('SOL', 'Solana', 98.20, 0.20),
    ('XRP', 'Ripple', 0.52, 0.18),
    ('USDC', 'USD Coin', 1.00, 0.01),
    ('ADA', 'Cardano', 0.38, 0.16),
    ('DOGE', 'Dogecoin', 0.08, 0.25),
    ('TRX', 'TRON', 0.11, 0.14),
    ('AVAX', 'Avalanche', 25.80, 0.22),
    ('LINK', 'Chainlink', 14.50, 0.17),
    ('DOT', 'Polkadot', 5.20, 0.19),
    ('MATIC', 'Polygon', 0.72, 0.21),
    ('LTC', 'Litecoin', 85.30, 0.12),
    ('BCH', 'Bitcoin Cash', 215.40, 0.16),
    ('XLM', 'Stellar', 0.09, 0.18),
    ('UNI', 'Uniswap', 7.80, 0.20),
    ('ATOM', 'Cosmos', 7.45, 0.17),
    ('XMR', 'Monero', 158.20, 0.15),
    ('ETC', 'Ethereum Classic', 22.50, 0.19),
    ('FIL', 'Filecoin', 4.10, 0.23),
    ('HBAR', 'Hedera', 0.06, 0.22),
    ('APT', 'Aptos', 8.90, 0.24),
    ('VET', 'VeChain', 0.03, 0.20),
    ('ICP', 'Internet Computer', 10.20, 0.21),
    ('NEAR', 'NEAR Protocol', 3.80, 0.19),
    ('ALGO', 'Algorand', 0.17, 0.18),
    ('OP', 'Optimism', 2.15, 0.23),
    ('AAVE', 'Aave', 178.50, 0.20)
]


class CryptoDataPopulator:
    def __init__(self, max_cryptos: Optional[int] = None, history_days: int = 365, use_mock_fallback: bool = True):
        """
        Initialize the populator.
        
        Args:
            max_cryptos: Maximum number of cryptos to process. None = all.
            history_days: Number of days of historical data to fetch (max 365 for free tier)
            use_mock_fallback: If True, generate mock data when API fails or rate-limited
        """
        self.connection = None
        self.max_cryptos = max_cryptos
        self.history_days = min(history_days, 365)  # Free tier limit
        self.use_mock_fallback = use_mock_fallback
        self.stats = {
            'total_fetched': 0,
            'market_data_inserted': 0,
            'market_data_updated': 0,
            'mock_data_created': 0,
            'price_history_inserted': 0,
            'errors': 0
        }
        
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
    
    def fetch_crypto_data(self, crypto_id: str) -> Optional[dict]:
        """
        Fetch current market data for a cryptocurrency.
        
        Args:
            crypto_id: CoinGecko ID (e.g., 'bitcoin', 'ethereum')
            
        Returns:
            Dict with current market data, or None if failed
        """
        try:
            url = f"{COINGECKO_BASE_URL}/coins/{crypto_id}"
            params = {
                'localization': 'false',
                'tickers': 'false',
                'community_data': 'false',
                'developer_data': 'false',
                'sparkline': 'false'
            }
            
            response = requests.get(url, params=params, timeout=REQUEST_TIMEOUT)
            response.raise_for_status()
            data = response.json()
            
            market_data = data.get('market_data', {})
            
            # Extract current price and other metrics
            current_price_usd = market_data.get('current_price', {}).get('usd')
            if not current_price_usd:
                return None
            
            # Calculate 24h change
            price_change_24h = market_data.get('price_change_24h')
            price_change_percent_24h = market_data.get('price_change_percentage_24h')
            
            # Get highs and lows
            high_24h = market_data.get('high_24h', {}).get('usd')
            low_24h = market_data.get('low_24h', {}).get('usd')
            ath = market_data.get('ath', {}).get('usd')  # All-time high
            atl = market_data.get('atl', {}).get('usd')  # All-time low
            
            return {
                'id': crypto_id,
                'symbol': data.get('symbol', '').upper(),
                'name': data.get('name', ''),
                'current_price': current_price_usd,
                'market_cap': market_data.get('market_cap', {}).get('usd'),
                'total_volume': market_data.get('total_volume', {}).get('usd'),
                'price_change_24h': price_change_24h,
                'price_change_percent_24h': price_change_percent_24h,
                'high_24h': high_24h,
                'low_24h': low_24h,
                'ath': ath,
                'atl': atl,
                'circulating_supply': market_data.get('circulating_supply'),
                'total_supply': market_data.get('total_supply'),
                'max_supply': market_data.get('max_supply'),
                'market_cap_rank': data.get('market_cap_rank')
            }
            
        except requests.RequestException as e:
            print(f"  ✗ Failed to fetch {crypto_id}: {e}")
            self.stats['errors'] += 1
            return None
    
    def fetch_price_history(self, crypto_id: str, days: int = 365) -> List[dict]:
        """
        Fetch historical price data for a cryptocurrency.
        
        Args:
            crypto_id: CoinGecko ID
            days: Number of days of history (1-365 for free tier)
            
        Returns:
            List of dicts with date and price data
        """
        try:
            url = f"{COINGECKO_BASE_URL}/coins/{crypto_id}/market_chart"
            params = {
                'vs_currency': 'usd',
                'days': days,
                'interval': 'daily'
            }
            
            response = requests.get(url, params=params, timeout=REQUEST_TIMEOUT)
            response.raise_for_status()
            data = response.json()
            
            prices = data.get('prices', [])
            volumes = data.get('total_volumes', [])
            
            # Convert to daily records
            history = []
            volume_dict = {int(v[0]): v[1] for v in volumes}
            
            for price_point in prices:
                timestamp = int(price_point[0])
                price = price_point[1]
                
                price_date = datetime.fromtimestamp(timestamp / 1000)
                volume = volume_dict.get(timestamp, 0)
                
                history.append({
                    'date': price_date.date(),
                    'price': price,
                    'volume': volume
                })
            
            return history
            
        except requests.RequestException as e:
            print(f"  ✗ Failed to fetch history for {crypto_id}: {e}")
            return []
    
    def insert_or_update_crypto(self, data: dict) -> Optional[int]:
        """
        Insert or update cryptocurrency in market_data table.
        
        Args:
            data: Crypto data dict
            
        Returns:
            market_data_id if successful, None otherwise
        """
        try:
            cursor = self.connection.cursor()
            
            # Check if crypto already exists
            cursor.execute("SELECT id FROM market_data WHERE symbol = %s AND asset_type_id = %s", 
                         (data['symbol'], CRYPTO_ASSET_TYPE_ID))
            result = cursor.fetchone()
            
            now = datetime.now()
            
            if result:
                # Update existing
                market_data_id = result[0]
                update_query = """
                    UPDATE market_data SET
                        name = %s,
                        current_price = %s,
                        day_change = %s,
                        day_change_percent = %s,
                        day_high = %s,
                        day_low = %s,
                        week_52_high = %s,
                        week_52_low = %s,
                        market_cap = %s,
                        volume = %s,
                        currency = 'USD',
                        exchange = 'CRYPTO',
                        data_source = 'CoinGecko',
                        last_updated = %s,
                        updated_date = %s
                    WHERE id = %s
                """
                cursor.execute(update_query, (
                    data['name'],
                    data['current_price'],
                    data['price_change_24h'],
                    data['price_change_percent_24h'],
                    data['high_24h'],
                    data['low_24h'],
                    data['ath'],
                    data['atl'],
                    data['market_cap'],
                    data['total_volume'],
                    now,
                    now,
                    market_data_id
                ))
                self.stats['market_data_updated'] += 1
                print(f"  ✓ Updated: {data['symbol']} ({data['name']}) - ${data['current_price']:,.2f}")
            else:
                # Insert new
                insert_query = """
                    INSERT INTO market_data (
                        symbol, name, asset_type_id, current_price,
                        day_change, day_change_percent, day_high, day_low,
                        week_52_high, week_52_low, market_cap, volume,
                        currency, exchange, data_source, last_updated,
                        created_date, updated_date
                    ) VALUES (
                        %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                        'USD', 'CRYPTO', 'CoinGecko', %s, %s, %s
                    )
                """
                cursor.execute(insert_query, (
                    data['symbol'],
                    data['name'],
                    CRYPTO_ASSET_TYPE_ID,
                    data['current_price'],
                    data['price_change_24h'],
                    data['price_change_percent_24h'],
                    data['high_24h'],
                    data['low_24h'],
                    data['ath'],
                    data['atl'],
                    data['market_cap'],
                    data['total_volume'],
                    now,
                    now,
                    now
                ))
                market_data_id = cursor.lastrowid
                self.stats['market_data_inserted'] += 1
                print(f"  ✓ Inserted: {data['symbol']} ({data['name']}) - ${data['current_price']:,.2f}")
            
            self.connection.commit()
            cursor.close()
            return market_data_id
            
        except Error as e:
            print(f"  ✗ Database error for {data.get('symbol')}: {e}")
            self.stats['errors'] += 1
            if self.connection:
                self.connection.rollback()
            return None
    
    def insert_price_history(self, market_data_id: int, history: List[dict]) -> int:
        """
        Insert historical price data for a cryptocurrency.
        
        Args:
            market_data_id: The market_data ID
            history: List of price history dicts
            
        Returns:
            Number of records inserted
        """
        if not history:
            return 0
        
        try:
            cursor = self.connection.cursor()
            inserted = 0
            
            for record in history:
                # Check if record already exists
                cursor.execute("""
                    SELECT id FROM price_history 
                    WHERE market_data_id = %s AND price_date = %s
                """, (market_data_id, record['date']))
                
                if cursor.fetchone():
                    continue  # Skip existing records
                
                # Insert new record
                insert_query = """
                    INSERT INTO price_history (
                        market_data_id, price_date, open_price, high_price,
                        low_price, close_price, adjusted_close, volume,
                        data_source, created_date
                    ) VALUES (
                        %s, %s, %s, %s, %s, %s, %s, %s, 'CoinGecko', %s
                    )
                """
                # For crypto, we only have close price, so use it for all price fields
                cursor.execute(insert_query, (
                    market_data_id,
                    record['date'],
                    record['price'],  # open
                    record['price'],  # high (approximation)
                    record['price'],  # low (approximation)
                    record['price'],  # close
                    record['price'],  # adjusted close
                    int(record['volume']) if record['volume'] else None,
                    datetime.now()
                ))
                inserted += 1
            
            self.connection.commit()
            cursor.close()
            self.stats['price_history_inserted'] += inserted
            
            if inserted > 0:
                print(f"    → Added {inserted} historical price records")
            
            return inserted
            
        except Error as e:
            print(f"  ✗ Failed to insert price history: {e}")
            if self.connection:
                self.connection.rollback()
            return 0
    
    def generate_mock_price_history(self, base_price: float, volatility: float, days: int) -> List[dict]:
        """Generate realistic mock price history using random walk."""
        history = []
        current_price = base_price
        
        for i in range(days, 0, -1):
            daily_change = random.gauss(0, volatility)
            current_price = current_price * (1 + daily_change)
            current_price = max(current_price, base_price * 0.01)
            
            open_price = current_price * random.uniform(0.98, 1.02)
            close_price = current_price
            high_price = max(open_price, close_price) * random.uniform(1.0, 1.03)
            low_price = min(open_price, close_price) * random.uniform(0.97, 1.0)
            
            base_volume = int(base_price * random.uniform(1e6, 1e8))
            volume = base_volume * random.uniform(0.5, 2.0)
            
            date = datetime.now() - timedelta(days=i)
            
            history.append({
                'date': date.date(),
                'price': close_price,
                'volume': int(volume)
            })
        
        return history
    
    def insert_mock_crypto(self, symbol: str, name: str, base_price: float, volatility: float) -> Optional[int]:
        """Insert mock cryptocurrency data."""
        try:
            cursor = self.connection.cursor()
            
            # Check if already exists
            cursor.execute("SELECT id FROM market_data WHERE symbol = %s AND asset_type_id = %s",
                         (symbol, CRYPTO_ASSET_TYPE_ID))
            result = cursor.fetchone()
            
            if result:
                cursor.close()
                return result[0]
            
            current_price = base_price * random.uniform(0.9, 1.1)
            previous_close = current_price * random.uniform(0.95, 1.05)
            day_change = current_price - previous_close
            day_change_percent = (day_change / previous_close) * 100
            
            high_24h = current_price * random.uniform(1.01, 1.08)
            low_24h = current_price * random.uniform(0.92, 0.99)
            
            circulating_supply = random.uniform(1e6, 1e9)
            market_cap = int(current_price * circulating_supply)
            volume_24h = int(market_cap * random.uniform(0.05, 0.30))
            
            now = datetime.now()
            
            insert_query = """
                INSERT INTO market_data (
                    symbol, name, asset_type_id, current_price,
                    day_change, day_change_percent, day_high, day_low,
                    week_52_high, week_52_low, market_cap, volume,
                    currency, exchange, data_source, last_updated,
                    created_date, updated_date
                ) VALUES (
                    %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                    'USD', 'CRYPTO', 'Mock Data', %s, %s, %s
                )
            """
            
            cursor.execute(insert_query, (
                symbol, name, CRYPTO_ASSET_TYPE_ID, current_price,
                day_change, day_change_percent, high_24h, low_24h,
                base_price * 1.5, base_price * 0.3, market_cap, volume_24h,
                now, now, now
            ))
            
            market_data_id = cursor.lastrowid
            self.connection.commit()
            cursor.close()
            
            self.stats['mock_data_created'] += 1
            print(f"  ✓ Mock inserted: {symbol} ({name}) - ${current_price:,.4f}")
            
            return market_data_id
            
        except Error as e:
            print(f"  ✗ Failed to insert mock {symbol}: {e}")
            self.stats['errors'] += 1
            if self.connection:
                self.connection.rollback()
            return None
    
    def populate(self):
        """Main population process."""
        print("\n" + "="*70)
        print("CRYPTOCURRENCY DATA POPULATION (Real + Mock)")
        print("="*70)
        
        cryptos_to_process = TOP_CRYPTOS[:self.max_cryptos] if self.max_cryptos else TOP_CRYPTOS
        
        print(f"\n📊 Processing {len(cryptos_to_process)} cryptocurrencies from API")
        print(f"📅 Fetching {self.history_days} days of historical data")
        print(f"⏱️  Rate limit: {RATE_LIMIT_DELAY}s delay between requests")
        if self.use_mock_fallback:
            print(f"🎲 Mock fallback: Enabled (will generate mock data if API fails)")
        print()
        
        # Try to fetch real data from API
        api_success_count = 0
        for i, crypto_id in enumerate(cryptos_to_process, 1):
            print(f"[{i}/{len(cryptos_to_process)}] Processing: {crypto_id}")
            
            # Fetch current market data
            crypto_data = self.fetch_crypto_data(crypto_id)
            if not crypto_data:
                if i > 3 and self.use_mock_fallback:
                    print(f"  ⚠ API failed, will use mock data later")
                continue
            
            self.stats['total_fetched'] += 1
            api_success_count += 1
            
            # Insert/update in database
            market_data_id = self.insert_or_update_crypto(crypto_data)
            if not market_data_id:
                continue
            
            # Rate limiting
            time.sleep(RATE_LIMIT_DELAY)
            
            # Fetch and insert historical data
            print(f"    Fetching historical data...")
            history = self.fetch_price_history(crypto_id, self.history_days)
            if history:
                self.insert_price_history(market_data_id, history)
            
            # Rate limiting
            time.sleep(RATE_LIMIT_DELAY)
        
        # If we got rate-limited or errors and mock fallback is enabled, add mock data
        if self.use_mock_fallback and api_success_count < 10:
            print("\n" + "="*70)
            print(f"📊 Generating mock data for additional cryptocurrencies")
            print("="*70 + "\n")
            
            for i, (symbol, name, base_price, volatility) in enumerate(MOCK_CRYPTOS, 1):
                print(f"[{i}/{len(MOCK_CRYPTOS)}] Mock: {symbol}")
                
                market_data_id = self.insert_mock_crypto(symbol, name, base_price, volatility)
                if not market_data_id:
                    continue
                
                print(f"    Generating historical data...")
                history = self.generate_mock_price_history(base_price, volatility, self.history_days)
                if history:
                    self.insert_price_history(market_data_id, history)
        
        print("\n" + "="*70)
        print("POPULATION SUMMARY")
        print("="*70)
        print(f"✓ Real API data fetched:   {self.stats['total_fetched']}")
        print(f"✓ Mock data created:       {self.stats['mock_data_created']}")
        print(f"✓ Market data inserted:    {self.stats['market_data_inserted']}")
        print(f"✓ Market data updated:     {self.stats['market_data_updated']}")
        print(f"✓ Price history inserted:  {self.stats['price_history_inserted']}")
        print(f"✗ Errors:                  {self.stats['errors']}")
        print("="*70)


def main():
    """Main entry point."""
    print("\n🚀 Starting Cryptocurrency Data Population...")
    
    # Configuration
    # For testing: max_cryptos=10, history_days=90
    # For full run: max_cryptos=None, history_days=365
    populator = CryptoDataPopulator(
        max_cryptos=None,
        history_days=365,
        use_mock_fallback=True  # Will generate mock data if API fails
    )
    
    if not populator.connect():
        return
    
    try:
        populator.populate()
    except KeyboardInterrupt:
        print("\n\n⚠️  Process interrupted by user")
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
    finally:
        populator.close()
    
    print("\n✓ Process completed!\n")


if __name__ == "__main__":
    main()
