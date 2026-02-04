"""
User Asset Data Populator for Portfolio Database
=================================================
Simulates realistic user portfolio data by creating assets entries.
This represents what assets users own, watch, or are researching.

The assets table links to market_data and represents user's holdings.
"""

import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime, timedelta
import random
from typing import List, Optional

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

# Asset status distribution (more realistic)
STATUS_DISTRIBUTION = {
    'OWNED': 0.50,      # 50% owned
    'WATCHLIST': 0.30,  # 30% on watchlist
    'RESEARCH': 0.15,   # 15% being researched
    'SOLD': 0.05        # 5% sold
}


class UserAssetPopulator:
    def __init__(self, clear_existing: bool = False):
        """
        Initialize the populator.
        
        Args:
            clear_existing: Whether to clear existing assets before populating
        """
        self.connection = None
        self.clear_existing = clear_existing
        self.stats = {
            'total_created': 0,
            'stocks_created': 0,
            'cryptos_created': 0,
            'mutual_funds_created': 0,
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
    
    def clear_assets(self):
        """Clear all existing assets."""
        try:
            cursor = self.connection.cursor()
            cursor.execute("DELETE FROM assets")
            self.connection.commit()
            deleted = cursor.rowcount
            cursor.close()
            print(f"✓ Cleared {deleted} existing assets")
        except Error as e:
            print(f"✗ Failed to clear assets: {e}")
            if self.connection:
                self.connection.rollback()
    
    def get_market_data(self, asset_type_id: Optional[int] = None) -> List[dict]:
        """
        Fetch available market data.
        
        Args:
            asset_type_id: Filter by asset type (None = all)
            
        Returns:
            List of market data records
        """
        try:
            cursor = self.connection.cursor(dictionary=True)
            
            if asset_type_id:
                query = """
                    SELECT id, symbol, name, current_price, asset_type_id, sector
                    FROM market_data 
                    WHERE asset_type_id = %s AND current_price IS NOT NULL
                    ORDER BY RAND()
                """
                cursor.execute(query, (asset_type_id,))
            else:
                query = """
                    SELECT id, symbol, name, current_price, asset_type_id, sector
                    FROM market_data 
                    WHERE current_price IS NOT NULL
                    ORDER BY RAND()
                """
                cursor.execute(query)
            
            results = cursor.fetchall()
            cursor.close()
            return results
            
        except Error as e:
            print(f"✗ Failed to fetch market data: {e}")
            return []
    
    def generate_random_status(self) -> str:
        """Generate random status based on distribution."""
        rand = random.random()
        cumulative = 0
        
        for status, probability in STATUS_DISTRIBUTION.items():
            cumulative += probability
            if rand < cumulative:
                return status
        
        return 'WATCHLIST'
    
    def generate_purchase_date(self, status: str) -> Optional[datetime]:
        """Generate realistic purchase date."""
        if status not in ['OWNED', 'SOLD']:
            return None
        
        # Random date in last 1-3 years
        days_ago = random.randint(30, 1095)
        return datetime.now() - timedelta(days=days_ago)
    
    def generate_quantity(self, current_price: float, asset_type_id: int, status: str) -> Optional[float]:
        """Generate realistic quantity based on asset type and price."""
        if status not in ['OWNED', 'SOLD']:
            return None
        
        if current_price is None or current_price <= 0:
            return None
        
        # Convert Decimal to float if needed
        current_price = float(current_price)
        
        # Different investment ranges by asset type
        if asset_type_id == 1:  # Stocks
            # Invest between $500 - $50,000
            investment = random.uniform(500, 50000)
            # Add some whole number bias for stocks
            qty = investment / current_price
            if current_price > 50:  # Expensive stocks, keep fractional
                return round(qty, 4)
            else:  # Cheaper stocks, round to whole numbers
                return max(1, round(qty))
        
        elif asset_type_id == 3:  # Crypto
            # Crypto investments: $100 - $20,000
            investment = random.uniform(100, 20000)
            qty = investment / current_price
            # Crypto can have many decimals
            return round(qty, 8)
        
        elif asset_type_id == 6:  # Mutual Funds
            # Mutual fund investments: $1,000 - $100,000
            investment = random.uniform(1000, 100000)
            qty = investment / current_price
            return round(qty, 4)
        
        else:
            # Default
            investment = random.uniform(1000, 10000)
            return round(investment / current_price, 4)
    
    def generate_purchase_price(self, current_price: float, purchase_date: Optional[datetime]) -> Optional[float]:
        """Generate purchase price with realistic variance."""
        if not purchase_date or current_price is None:
            return None
        
        # Convert Decimal to float if needed
        current_price = float(current_price)
        
        # Calculate days held
        days_held = (datetime.now() - purchase_date).days
        
        # More variance for longer holding periods
        # Assume average annual return of -20% to +40%
        annual_variance = random.uniform(-0.20, 0.40)
        years_held = days_held / 365
        
        # Calculate historical price (inverse calculation)
        purchase_price = current_price / (1 + (annual_variance * years_held))
        
        return round(purchase_price, 4)
    
    def generate_target_price(self, current_price: float, status: str) -> Optional[float]:
        """Generate realistic target price."""
        if status not in ['OWNED', 'WATCHLIST', 'RESEARCH'] or current_price is None:
            return None
        
        # Convert Decimal to float if needed
        current_price = float(current_price)
        
        # Target 5-30% above current price
        target_multiplier = random.uniform(1.05, 1.30)
        return round(current_price * target_multiplier, 2)
    
    def generate_priority_rank(self, status: str) -> Optional[int]:
        """Generate priority rank for watchlist/research items."""
        if status in ['WATCHLIST', 'RESEARCH']:
            return random.randint(1, 5)
        return None
    
    def create_asset(self, market_data: dict) -> bool:
        """
        Create an asset entry.
        
        Args:
            market_data: Market data record dict
            
        Returns:
            True if successful
        """
        try:
            cursor = self.connection.cursor()
            
            # Check if asset already exists for this market_data_id
            cursor.execute("SELECT id FROM assets WHERE market_data_id = %s", (market_data['id'],))
            if cursor.fetchone():
                cursor.close()
                return False  # Skip duplicates
            
            status = self.generate_random_status()
            purchase_date = self.generate_purchase_date(status)
            quantity = self.generate_quantity(market_data['current_price'], market_data['asset_type_id'], status)
            purchase_price = self.generate_purchase_price(market_data['current_price'], purchase_date)
            target_price = self.generate_target_price(market_data['current_price'], status)
            priority_rank = self.generate_priority_rank(status)
            
            # Watchlist date
            watchlist_date = None
            if status == 'WATCHLIST':
                days_ago = random.randint(1, 180)
                watchlist_date = (datetime.now() - timedelta(days=days_ago)).date()
            
            now = datetime.now()
            
            insert_query = """
                INSERT INTO assets (
                    symbol, name, asset_type_id, market_data_id, status,
                    quantity, purchase_price, purchase_date, current_price,
                    target_price, sector, priority_rank, added_to_watchlist_date,
                    price_alerts_enabled, notes, created_date, updated_date
                ) VALUES (
                    %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
                )
            """
            
            # Generate notes based on status
            notes = None
            if status == 'RESEARCH':
                notes_options = [
                    "Evaluating for long-term investment",
                    "Monitoring price trends",
                    "Waiting for better entry point",
                    "Researching fundamentals",
                    "Comparing with competitors"
                ]
                notes = random.choice(notes_options)
            elif status == 'WATCHLIST':
                notes_options = [
                    "High potential growth",
                    "Good dividend yield",
                    "Strong fundamentals",
                    "Recommended by analyst",
                    "Sector diversification"
                ]
                notes = random.choice(notes_options)
            
            cursor.execute(insert_query, (
                market_data['symbol'],
                market_data['name'],
                market_data['asset_type_id'],
                market_data['id'],
                status,
                quantity,
                purchase_price,
                purchase_date.date() if purchase_date else None,
                market_data['current_price'],
                target_price,
                market_data.get('sector'),
                priority_rank,
                watchlist_date,
                random.choice([True, False]) if status == 'OWNED' else False,
                notes,
                now,
                now
            ))
            
            self.connection.commit()
            cursor.close()
            
            # Track stats
            self.stats['total_created'] += 1
            if market_data['asset_type_id'] == 1:
                self.stats['stocks_created'] += 1
            elif market_data['asset_type_id'] == 3:
                self.stats['cryptos_created'] += 1
            elif market_data['asset_type_id'] == 6:
                self.stats['mutual_funds_created'] += 1
            
            return True
            
        except Error as e:
            print(f"  ✗ Failed to create asset for {market_data.get('symbol')}: {e}")
            self.stats['errors'] += 1
            if self.connection:
                self.connection.rollback()
            return False
    
    def populate(self, num_assets_per_type: dict = None):
        """
        Populate user assets.
        
        Args:
            num_assets_per_type: Dict of {asset_type_id: count}
                                 Default: {1: 20, 3: 15, 6: 5} (stocks, crypto, mutual funds)
        """
        print("\n" + "="*70)
        print("USER ASSET DATA POPULATION")
        print("="*70)
        
        if self.clear_existing:
            print("\n⚠️  Clearing existing assets...")
            self.clear_assets()
        
        if num_assets_per_type is None:
            num_assets_per_type = {
                1: 25,  # 25 stock assets
                3: 15,  # 15 crypto assets
                6: 5    # 5 mutual fund assets
            }
        
        print(f"\n📊 Creating user assets:")
        for asset_type_id, count in num_assets_per_type.items():
            asset_type_name = {1: 'Stocks', 3: 'Crypto', 6: 'Mutual Funds'}.get(asset_type_id, f'Type {asset_type_id}')
            print(f"   → {count} {asset_type_name}")
        
        print(f"\n📈 Status distribution:")
        for status, prob in STATUS_DISTRIBUTION.items():
            print(f"   → {status}: {prob*100:.0f}%")
        
        print()
        
        for asset_type_id, target_count in num_assets_per_type.items():
            asset_type_name = {1: 'Stock', 3: 'Crypto', 6: 'Mutual Fund'}.get(asset_type_id, f'Type {asset_type_id}')
            print(f"\n📦 Processing {asset_type_name} assets...")
            
            # Get available market data
            market_data_list = self.get_market_data(asset_type_id)
            
            if not market_data_list:
                print(f"  ⚠️  No market data found for asset type {asset_type_id}")
                continue
            
            print(f"  ✓ Found {len(market_data_list)} available {asset_type_name.lower()} records")
            
            # Create assets
            created = 0
            for market_data in market_data_list[:target_count]:
                if self.create_asset(market_data):
                    created += 1
                    status_emoji = {'OWNED': '💰', 'WATCHLIST': '👀', 'RESEARCH': '🔍', 'SOLD': '📤'}
                    print(f"  {status_emoji.get(self.generate_random_status(), '✓')} Created asset: {market_data['symbol']} ({market_data['name'][:30]})")
                
                if created >= target_count:
                    break
        
        print("\n" + "="*70)
        print("POPULATION SUMMARY")
        print("="*70)
        print(f"✓ Total assets created:     {self.stats['total_created']}")
        print(f"  → Stock assets:           {self.stats['stocks_created']}")
        print(f"  → Crypto assets:          {self.stats['cryptos_created']}")
        print(f"  → Mutual fund assets:     {self.stats['mutual_funds_created']}")
        print(f"✗ Errors:                   {self.stats['errors']}")
        print("="*70)
        
        # Show status breakdown
        self.show_status_breakdown()
    
    def show_status_breakdown(self):
        """Show breakdown of created assets by status."""
        try:
            cursor = self.connection.cursor()
            
            print("\n" + "="*70)
            print("ASSET STATUS BREAKDOWN")
            print("="*70)
            
            cursor.execute("""
                SELECT status, COUNT(*) as count 
                FROM assets 
                GROUP BY status 
                ORDER BY count DESC
            """)
            
            for row in cursor.fetchall():
                status, count = row
                print(f"  {status:12s}: {count:3d}")
            
            cursor.close()
            
        except Error as e:
            print(f"✗ Failed to show breakdown: {e}")


def main():
    """Main entry point."""
    print("\n🚀 Starting User Asset Population...")
    
    # Option to clear existing assets
    clear = input("\n⚠️  Clear existing assets? (y/N): ").strip().lower() == 'y'
    
    populator = UserAssetPopulator(clear_existing=clear)
    
    if not populator.connect():
        return
    
    try:
        # Customize these numbers as needed
        populator.populate(num_assets_per_type={
            1: 30,  # Stocks
            3: 20,  # Crypto
            6: 5    # Mutual Funds
        })
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
