"""
ESG Data Populator for Portfolio Database
==========================================
Creates ESG table and populates it with data for stocks in market_data.
"""

import mysql.connector
from mysql.connector import Error
import os
from typing import Optional
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

# Sample ESG data for common stocks
ESG_DATA = {
    "AAPL": {"env": 85, "social": 82, "gov": 88, "controversy": 1},
    "MSFT": {"env": 92, "social": 88, "gov": 90, "controversy": 0},
    "GOOGL": {"env": 88, "social": 78, "gov": 85, "controversy": 2},
    "AMZN": {"env": 75, "social": 72, "gov": 80, "controversy": 2},
    "NVDA": {"env": 82, "social": 85, "gov": 87, "controversy": 0},
    "META": {"env": 80, "social": 70, "gov": 75, "controversy": 3},
    "TSLA": {"env": 90, "social": 65, "gov": 68, "controversy": 4},
    "BRK-B": {"env": 70, "social": 75, "gov": 92, "controversy": 1},
    "UNH": {"env": 78, "social": 80, "gov": 85, "controversy": 2},
    "JNJ": {"env": 85, "social": 88, "gov": 90, "controversy": 1},
    "V": {"env": 82, "social": 85, "gov": 88, "controversy": 0},
    "XOM": {"env": 55, "social": 68, "gov": 75, "controversy": 4},
    "JPM": {"env": 80, "social": 82, "gov": 88, "controversy": 2},
    "WMT": {"env": 75, "social": 78, "gov": 82, "controversy": 2},
    "MA": {"env": 83, "social": 86, "gov": 89, "controversy": 0},
    "PG": {"env": 88, "social": 85, "gov": 90, "controversy": 1},
    "HD": {"env": 80, "social": 82, "gov": 85, "controversy": 1},
    "CVX": {"env": 58, "social": 70, "gov": 78, "controversy": 3},
    "MRK": {"env": 86, "social": 87, "gov": 89, "controversy": 1},
    "ABBV": {"env": 84, "social": 86, "gov": 88, "controversy": 1},
    "LLY": {"env": 87, "social": 88, "gov": 90, "controversy": 0},
    "PEP": {"env": 85, "social": 84, "gov": 87, "controversy": 1},
    "KO": {"env": 82, "social": 83, "gov": 86, "controversy": 2},
    "COST": {"env": 81, "social": 85, "gov": 88, "controversy": 0},
    "AVGO": {"env": 79, "social": 80, "gov": 83, "controversy": 1},
    "PFE": {"env": 85, "social": 87, "gov": 89, "controversy": 1},
    "TMO": {"env": 83, "social": 84, "gov": 87, "controversy": 0},
    "MCD": {"env": 78, "social": 80, "gov": 84, "controversy": 2},
    "CSCO": {"env": 84, "social": 83, "gov": 86, "controversy": 1},
    "ACN": {"env": 86, "social": 87, "gov": 90, "controversy": 0},
    "CRM": {"env": 90, "social": 85, "gov": 88, "controversy": 1},
    "ABT": {"env": 84, "social": 85, "gov": 88, "controversy": 1},
    "DHR": {"env": 85, "social": 86, "gov": 89, "controversy": 0},
    "NKE": {"env": 80, "social": 75, "gov": 82, "controversy": 2},
    "ORCL": {"env": 77, "social": 78, "gov": 81, "controversy": 2},
    "TXN": {"env": 82, "social": 83, "gov": 86, "controversy": 1},
    "NFLX": {"env": 79, "social": 77, "gov": 80, "controversy": 1},
    "AMD": {"env": 81, "social": 82, "gov": 85, "controversy": 1},
    "INTC": {"env": 83, "social": 84, "gov": 87, "controversy": 1},
    "DIS": {"env": 78, "social": 80, "gov": 83, "controversy": 2},
    "VZ": {"env": 76, "social": 79, "gov": 82, "controversy": 2},
    "ADBE": {"env": 88, "social": 85, "gov": 88, "controversy": 0},
    "PM": {"env": 65, "social": 68, "gov": 75, "controversy": 4},
    "NEE": {"env": 92, "social": 82, "gov": 86, "controversy": 1},
    "WFC": {"env": 74, "social": 76, "gov": 79, "controversy": 3},
    "BAC": {"env": 78, "social": 80, "gov": 83, "controversy": 2},
    "RTX": {"env": 72, "social": 75, "gov": 80, "controversy": 2},
    "UPS": {"env": 80, "social": 83, "gov": 86, "controversy": 1},
    "COP": {"env": 60, "social": 72, "gov": 77, "controversy": 3},
    "QCOM": {"env": 82, "social": 83, "gov": 86, "controversy": 1},
}

class ESGPopulator:
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
    
    def create_esg_table(self):
        """Create ESG ratings table with proper entity relationship to market_data."""
        try:
            cursor = self.connection.cursor()
            
            # Drop existing table if exists
            cursor.execute("DROP TABLE IF EXISTS esg_ratings")
            
            # Create ESG ratings table
            create_table_sql = """
            CREATE TABLE esg_ratings (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                market_data_id BIGINT NOT NULL,
                symbol VARCHAR(20) NOT NULL,
                
                -- Overall ESG Scores (0-100, higher is better)
                total_score DECIMAL(5,2),
                total_grade VARCHAR(20),
                
                -- Pillar Scores (0-100, higher is better)
                environment_score DECIMAL(5,2),
                environment_grade VARCHAR(20),
                social_score DECIMAL(5,2),
                social_grade VARCHAR(20),
                governance_score DECIMAL(5,2),
                governance_grade VARCHAR(20),
                
                -- Risk and Controversy
                controversy_level INT,  -- 0-5, 0=none, 5=severe
                risk_level VARCHAR(20), -- Low, Medium, High, Severe
                
                -- Metadata
                data_source VARCHAR(100),
                last_updated DATETIME(6),
                created_date DATETIME(6),
                
                -- Constraints
                UNIQUE KEY unique_symbol (symbol),
                KEY idx_market_data (market_data_id),
                KEY idx_total_score (total_score),
                KEY idx_controversy (controversy_level),
                
                -- Foreign Key to market_data
                CONSTRAINT fk_esg_market_data 
                    FOREIGN KEY (market_data_id) 
                    REFERENCES market_data(id) 
                    ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """
            
            cursor.execute(create_table_sql)
            self.connection.commit()
            cursor.close()
            
            print("✓ ESG ratings table created with foreign key relationship to market_data")
            
        except Error as e:
            print(f"✗ Failed to create ESG table: {e}")
    
    def score_to_grade(self, score: float) -> str:
        """Convert numeric score to letter grade."""
        if score is None:
            return "N/A"
        elif score >= 90:
            return "A+"
        elif score >= 85:
            return "A"
        elif score >= 80:
            return "A-"
        elif score >= 75:
            return "B+"
        elif score >= 70:
            return "B"
        elif score >= 65:
            return "B-"
        elif score >= 60:
            return "C+"
        elif score >= 55:
            return "C"
        else:
            return "C-"
    
    def controversy_to_risk(self, controversy: int) -> str:
        """Convert controversy level to risk level."""
        if controversy == 0:
            return "Negligible"
        elif controversy == 1:
            return "Low"
        elif controversy == 2:
            return "Medium"
        elif controversy == 3:
            return "High"
        else:
            return "Severe"
    
    def get_stocks_from_db(self):
        """Fetch all stocks from market_data table."""
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute("SELECT id, symbol FROM market_data ORDER BY symbol")
            stocks = cursor.fetchall()
            cursor.close()
            return stocks
        except Error as e:
            print(f"✗ Failed to fetch stocks: {e}")
            return []
    
    def insert_esg_data(self, market_data_id: int, ticker: str, esg_data: dict) -> bool:
        """Insert ESG data for a stock."""
        try:
            cursor = self.connection.cursor()
            
            # Calculate total score (weighted average: env 30%, social 30%, gov 40%)
            total_score = (
                esg_data['env'] * 0.30 +
                esg_data['social'] * 0.30 +
                esg_data['gov'] * 0.40
            )
            
            sql = """
                INSERT INTO esg_ratings (
                    market_data_id, symbol,
                    total_score, total_grade,
                    environment_score, environment_grade,
                    social_score, social_grade,
                    governance_score, governance_grade,
                    controversy_level, risk_level,
                    data_source, last_updated, created_date
                ) VALUES (
                    %s, %s,
                    %s, %s,
                    %s, %s,
                    %s, %s,
                    %s, %s,
                    %s, %s,
                    %s, NOW(), NOW()
                )
            """
            
            values = (
                market_data_id,
                ticker,
                round(total_score, 2),
                self.score_to_grade(total_score),
                esg_data['env'],
                self.score_to_grade(esg_data['env']),
                esg_data['social'],
                self.score_to_grade(esg_data['social']),
                esg_data['gov'],
                self.score_to_grade(esg_data['gov']),
                esg_data['controversy'],
                self.controversy_to_risk(esg_data['controversy']),
                "Sample Dataset"
            )
            
            cursor.execute(sql, values)
            self.connection.commit()
            cursor.close()
            
            return True
            
        except Error as e:
            print(f"  ✗ Failed to insert ESG data for {ticker}: {e}")
            return False
    
    def populate_all_esg(self):
        """Main function to populate ESG data for all stocks."""
        print("\n" + "=" * 80)
        print("ESG DATA POPULATOR - Portfolio Management System")
        print("=" * 80)
        
        # Get stocks from database
        stocks = self.get_stocks_from_db()
        if not stocks:
            print("✗ No stocks found in market_data table")
            return
        
        print(f"\nPopulating ESG data for {len(stocks)} stocks...")
        print("-" * 80)
        
        success_count = 0
        not_found_count = 0
        
        for i, stock in enumerate(stocks, 1):
            ticker = stock['symbol']
            market_data_id = stock['id']
            
            print(f"[{i}/{len(stocks)}] {ticker:6s}...", end=" ")
            
            # Get ESG data
            if ticker in ESG_DATA:
                esg_data = ESG_DATA[ticker]
                if self.insert_esg_data(market_data_id, ticker, esg_data):
                    total = round(esg_data['env']*0.3 + esg_data['social']*0.3 + esg_data['gov']*0.4, 2)
                    print(f"✓ ESG Score: {total:.1f} ({self.score_to_grade(total)})")
                    success_count += 1
            else:
                print(f"⚠ No ESG data (sample data only)")
                not_found_count += 1
        
        print("-" * 80)
        print(f"\n✓ ESG population complete!")
        print(f"  Success:   {success_count}/{len(stocks)}")
        print(f"  No data:   {not_found_count}/{len(stocks)}")
        print("=" * 80 + "\n")

def main():
    """Main execution."""
    populator = ESGPopulator()
    
    if not populator.connect():
        print("Failed to connect to database. Check your configuration.")
        return
    
    try:
        # Create ESG table
        populator.create_esg_table()
        
        # Populate ESG data
        populator.populate_all_esg()
        
    finally:
        populator.close()

if __name__ == "__main__":
    main()
