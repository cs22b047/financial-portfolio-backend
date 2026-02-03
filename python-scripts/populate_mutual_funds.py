"""
Mutual Fund Data Populator for Portfolio Database
==================================================
Fetches mutual fund NAV data from https://api.mfapi.in/ and populates:
1. market_data table with latest NAV
2. price_history table with historical NAV data
"""

import requests
import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime
from typing import Optional, List
import time

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
MFAPI_BASE_URL = "https://api.mfapi.in/mf"
MUTUAL_FUND_ASSET_TYPE_ID = 6  # From asset_types table

# Request configuration
REQUEST_TIMEOUT = 10
BATCH_COMMIT_SIZE = 50
RATE_LIMIT_DELAY = 0.5  # seconds between API requests


class MutualFundPopulator:
    def __init__(self, max_funds: Optional[int] = None):
        """
        Initialize the populator.
        
        Args:
            max_funds: Maximum number of funds to process (for testing). None = all funds.
        """
        self.connection = None
        self.max_funds = max_funds
        self.stats = {
            'total_fetched': 0,
            'market_data_inserted': 0,
            'market_data_updated': 0,
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
    
    def fetch_all_schemes(self) -> List[dict]:
        """
        Fetch list of all mutual fund schemes.
        
        Returns:
            List of dicts with schemeCode and schemeName
        """
        try:
            print(f"📡 Fetching list of all mutual fund schemes...")
            response = requests.get(MFAPI_BASE_URL, timeout=REQUEST_TIMEOUT)
            response.raise_for_status()
            
            schemes = response.json()
            total_count = len(schemes)
            
            if self.max_funds:
                schemes = schemes[:self.max_funds]
                print(f"✓ Found {total_count} schemes, processing {len(schemes)} (limited)")
            else:
                print(f"✓ Found {total_count} schemes")
            
            return schemes
            
        except requests.RequestException as e:
            print(f"✗ Failed to fetch scheme list: {e}")
            return []
    
    def fetch_scheme_data(self, scheme_code: str) -> Optional[dict]:
        """
        Fetch detailed data for a specific mutual fund scheme.
        
        Args:
            scheme_code: The scheme code to fetch
            
        Returns:
            Dict with meta and data, or None if failed
        """
        try:
            url = f"{MFAPI_BASE_URL}/{scheme_code}"
            response = requests.get(url, timeout=REQUEST_TIMEOUT)
            response.raise_for_status()
            
            data = response.json()
            
            if data.get('status') != 'SUCCESS':
                return None
                
            return data
            
        except requests.RequestException as e:
            print(f"  ✗ Failed to fetch scheme {scheme_code}: {e}")
            return None
    
    def parse_nav_date(self, date_str: str) -> Optional[datetime]:
        """
        Parse NAV date string (DD-MM-YYYY) to datetime.
        
        Args:
            date_str: Date string in DD-MM-YYYY format
            
        Returns:
            datetime object or None if parsing fails
        """
        try:
            return datetime.strptime(date_str, '%d-%m-%Y')
        except ValueError:
            return None
    
    def get_market_data_id(self, cursor, symbol: str) -> Optional[int]:
        """
        Get market_data_id for a symbol, or None if not exists.
        
        Args:
            cursor: Database cursor
            symbol: The symbol to look up
            
        Returns:
            market_data_id or None
        """
        cursor.execute("SELECT id FROM market_data WHERE symbol = %s", (symbol,))
        result = cursor.fetchone()
        return result[0] if result else None
    
    def insert_market_data(self, cursor, data: dict) -> bool:
        """
        Insert or update market_data for a mutual fund.
        
        Args:
            cursor: Database cursor
            data: Scheme data from API
            
        Returns:
            True if successful, False otherwise
        """
        try:
            meta = data['meta']
            nav_data = data['data']
            
            if not nav_data or len(nav_data) == 0:
                return False
            
            # Extract data
            symbol = str(meta['scheme_code'])
            name = meta['scheme_name']
            
            # Latest NAV is first in array
            current_nav = float(nav_data[0]['nav'])
            previous_nav = float(nav_data[1]['nav']) if len(nav_data) > 1 else None
            
            # Calculate changes
            day_change = (current_nav - previous_nav) if previous_nav else None
            day_change_percent = (day_change / previous_nav * 100) if (day_change and previous_nav) else None
            
            # Get additional metadata
            fund_house = meta.get('fund_house', '')
            scheme_category = meta.get('scheme_category', '')
            
            # Check if exists
            market_data_id = self.get_market_data_id(cursor, symbol)
            
            if market_data_id:
                # UPDATE existing
                sql = """
                    UPDATE market_data 
                    SET name = %s,
                        current_price = %s,
                        previous_close = %s,
                        day_change = %s,
                        day_change_percent = %s,
                        sector = %s,
                        industry = %s,
                        currency = 'INR',
                        data_source = 'mfapi.in',
                        last_updated = NOW()
                    WHERE symbol = %s
                """
                cursor.execute(sql, (
                    name, current_nav, previous_nav, day_change, day_change_percent,
                    fund_house, scheme_category, symbol
                ))
                self.stats['market_data_updated'] += 1
            else:
                # INSERT new
                sql = """
                    INSERT INTO market_data 
                    (asset_type_id, symbol, name, current_price, previous_close,
                     day_change, day_change_percent, sector, industry, 
                     currency, data_source)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(sql, (
                    MUTUAL_FUND_ASSET_TYPE_ID, symbol, name, current_nav, previous_nav,
                    day_change, day_change_percent, fund_house, scheme_category,
                    'INR', 'mfapi.in'
                ))
                self.stats['market_data_inserted'] += 1
            
            return True
            
        except (KeyError, ValueError, Error) as e:
            print(f"  ✗ Failed to insert market_data: {e}")
            return False
    
    def insert_price_history(self, cursor, symbol: str, data: dict) -> int:
        """
        Insert historical NAV data into price_history.
        
        Args:
            cursor: Database cursor
            symbol: The fund symbol
            data: Scheme data from API
            
        Returns:
            Number of records inserted
        """
        try:
            # Get market_data_id
            market_data_id = self.get_market_data_id(cursor, symbol)
            if not market_data_id:
                return 0
            
            nav_data = data['data']
            inserted = 0
            
            # Use INSERT ... ON DUPLICATE KEY UPDATE for upsert
            sql = """
                INSERT INTO price_history
                (market_data_id, price_date, close_price, data_source, created_date)
                VALUES (%s, %s, %s, %s, NOW())
                ON DUPLICATE KEY UPDATE
                    close_price = VALUES(close_price),
                    data_source = VALUES(data_source)
            """
            
            for nav_entry in nav_data:
                nav_date = self.parse_nav_date(nav_entry['date'])
                if not nav_date:
                    continue
                
                nav_value = float(nav_entry['nav'])
                
                cursor.execute(sql, (
                    market_data_id,
                    nav_date.date(),
                    nav_value,
                    'mfapi.in'
                ))
                inserted += 1
            
            self.stats['price_history_inserted'] += inserted
            return inserted
            
        except (KeyError, ValueError, Error) as e:
            print(f"  ✗ Failed to insert price_history: {e}")
            return 0
    
    def populate(self):
        """Main method to populate mutual fund data."""
        if not self.connect():
            return
        
        try:
            # Fetch all schemes
            schemes = self.fetch_all_schemes()
            if not schemes:
                print("✗ No schemes to process")
                return
            
            cursor = self.connection.cursor()
            total_schemes = len(schemes)
            processed = 0
            
            print(f"\n📊 Starting to process {total_schemes} mutual fund schemes...")
            print(f"   Batch commit size: {BATCH_COMMIT_SIZE}")
            print(f"   Rate limit delay: {RATE_LIMIT_DELAY}s\n")
            
            for idx, scheme in enumerate(schemes, 1):
                scheme_code = scheme['schemeCode']
                scheme_name = scheme.get('schemeName', 'Unknown')
                
                # Progress indicator
                if idx % 10 == 0 or idx == 1:
                    print(f"[{idx}/{total_schemes}] Processing: {scheme_name[:50]}...")
                
                # Fetch detailed data
                scheme_data = self.fetch_scheme_data(scheme_code)
                if not scheme_data:
                    self.stats['errors'] += 1
                    continue
                
                self.stats['total_fetched'] += 1
                
                # Insert market_data
                if not self.insert_market_data(cursor, scheme_data):
                    self.stats['errors'] += 1
                    continue
                
                # Insert price_history
                symbol = str(scheme_data['meta']['scheme_code'])
                self.insert_price_history(cursor, symbol, scheme_data)
                
                processed += 1
                
                # Batch commit
                if processed % BATCH_COMMIT_SIZE == 0:
                    self.connection.commit()
                    print(f"  ✓ Committed batch at {processed} schemes")
                
                # Rate limiting
                if idx < total_schemes:  # Don't delay after last item
                    time.sleep(RATE_LIMIT_DELAY)
            
            # Final commit
            self.connection.commit()
            cursor.close()
            
            # Print summary
            print("\n" + "="*60)
            print("📈 POPULATION SUMMARY")
            print("="*60)
            print(f"Total schemes processed:     {self.stats['total_fetched']}")
            print(f"Market data inserted:        {self.stats['market_data_inserted']}")
            print(f"Market data updated:         {self.stats['market_data_updated']}")
            print(f"Price history records:       {self.stats['price_history_inserted']}")
            print(f"Errors:                      {self.stats['errors']}")
            print("="*60)
            
        except Exception as e:
            print(f"\n✗ Fatal error: {e}")
            if self.connection:
                self.connection.rollback()
        finally:
            self.close()


def main():
    """Main entry point."""
    import argparse
    
    parser = argparse.ArgumentParser(description='Populate mutual fund data into database')
    parser.add_argument('--limit', type=int, help='Limit number of funds to process (for testing)')
    
    args = parser.parse_args()
    
    print("="*60)
    print("🏦 MUTUAL FUND DATA POPULATOR")
    print("="*60)
    print(f"Database: {DB_CONFIG['database']}@{DB_CONFIG['host']}")
    print(f"API Source: {MFAPI_BASE_URL}")
    if args.limit:
        print(f"Limit: {args.limit} funds")
    print("="*60 + "\n")
    
    populator = MutualFundPopulator(max_funds=args.limit)
    populator.populate()


if __name__ == "__main__":
    main()
