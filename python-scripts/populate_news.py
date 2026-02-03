"""
News Data Populator for Portfolio Database
==========================================
Fetches company news and populates the database news table.
Uses stocks from market_data table.
"""

import yfinance as yf
import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime, timedelta
import time
from typing import List, Dict, Optional

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

try:
    from groq import Groq
    GROQ_AVAILABLE = True
except ImportError:
    GROQ_AVAILABLE = False
    print("Note: groq library not installed. AI summaries will be disabled.")

# Database configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'portfolio_db')
}

# Groq configuration for AI summaries
GROQ_API_KEY = os.getenv("GROQ_API_KEY")
GROQ_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"

class NewsPopulator:
    def __init__(self):
        self.connection = None
        self.groq_client = None
        
        # Initialize Groq for AI summaries
        if GROQ_AVAILABLE and GROQ_API_KEY:
            try:
                self.groq_client = Groq(api_key=GROQ_API_KEY)
                print("✓ Groq AI enabled for summaries")
            except Exception as e:
                print(f"⚠ Groq initialization failed: {e}")
        
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
    
    def get_stocks_from_db(self) -> List[Dict]:
        """Fetch all stocks from market_data table."""
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute("SELECT id, symbol, name FROM market_data ORDER BY symbol")
            stocks = cursor.fetchall()
            cursor.close()
            return stocks
        except Error as e:
            print(f"✗ Failed to fetch stocks: {e}")
            return []
    
    def generate_ai_summary(self, title: str, ticker: str) -> str:
        """Generate AI summary for news headline."""
        if not self.groq_client:
            return ""
        
        try:
            prompt = f"""You are a financial news analyst. Generate a brief summary (2-3 sentences) for this news headline about {ticker}:

Headline: {title}

Summary should be concise, professional, and focus on key financial impact.

Summary:"""

            response = self.groq_client.chat.completions.create(
                model=GROQ_MODEL,
                messages=[
                    {"role": "system", "content": "You are a financial news analyst."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.3,
                max_tokens=150,
                top_p=0.9
            )
            
            return response.choices[0].message.content.strip()
            
        except Exception as e:
            return ""
    
    def fetch_yahoo_news(self, ticker: str) -> List[Dict]:
        """Fetch news from Yahoo Finance."""
        try:
            stock = yf.Ticker(ticker)
            news = stock.news
            
            if not news:
                return []
            
            articles = []
            for article in news[:10]:  # Limit to 10 most recent
                title = article.get("title", "")
                
                # Generate AI summary
                summary = ""
                if self.groq_client and title:
                    summary = self.generate_ai_summary(title, ticker)
                    time.sleep(0.2)  # Rate limit
                
                # Format timestamp
                published_ts = article.get("providerPublishTime")
                published_date = None
                if published_ts:
                    published_date = datetime.fromtimestamp(published_ts)
                
                articles.append({
                    "ticker": ticker,
                    "title": title,
                    "summary": summary,
                    "link": article.get("link", ""),
                    "image_url": article.get("thumbnail", {}).get("resolutions", [{}])[0].get("url", "") if article.get("thumbnail") else "",
                    "source": "Yahoo Finance",
                    "publisher": article.get("publisher", ""),
                    "published_date": published_date
                })
            
            return articles
            
        except Exception as e:
            print(f"  ⚠ Yahoo error for {ticker}: {str(e)[:50]}")
            return []
    
    def insert_news(self, market_data_id: int, ticker: str, article: Dict) -> bool:
        """Insert news article into database."""
        try:
            cursor = self.connection.cursor()
            
            # Check if article already exists (by link)
            if article['link']:
                cursor.execute("SELECT id FROM news WHERE link = %s", (article['link'],))
                if cursor.fetchone():
                    cursor.close()
                    return False  # Already exists
            
            # Insert new article
            sql = """
                INSERT INTO news (
                    market_data_id, symbol, title, summary, link,
                    image_url, source, publisher, published_date,
                    is_read, created_date
                ) VALUES (
                    %s, %s, %s, %s, %s,
                    %s, %s, %s, %s,
                    0, NOW()
                )
            """
            
            values = (
                market_data_id,
                ticker,
                article['title'][:500],  # Truncate to fit varchar(500)
                article['summary'],
                article['link'][:1000] if article['link'] else None,
                article['image_url'][:1000] if article['image_url'] else None,
                article['source'],
                article['publisher'][:200] if article['publisher'] else None,
                article['published_date']
            )
            
            cursor.execute(sql, values)
            self.connection.commit()
            cursor.close()
            return True
            
        except Error as e:
            # Ignore duplicate key errors
            if e.errno != 1062:  # Duplicate entry
                print(f"  ⚠ Insert failed: {str(e)[:50]}")
            return False
    
    def populate_all_news(self):
        """Main function to populate news for all stocks."""
        print("\n" + "=" * 80)
        print("NEWS DATA POPULATOR - Portfolio Management System")
        print("=" * 80)
        
        # Get stocks from database
        stocks = self.get_stocks_from_db()
        if not stocks:
            print("✗ No stocks found in market_data table")
            return
        
        print(f"\nFetching news for {len(stocks)} stocks from Yahoo Finance...")
        if self.groq_client:
            print("✓ AI summaries enabled")
        print("-" * 80)
        
        total_articles = 0
        total_inserted = 0
        
        for i, stock in enumerate(stocks, 1):
            ticker = stock['symbol']
            market_data_id = stock['id']
            
            print(f"[{i}/{len(stocks)}] {ticker:6s}...", end=" ")
            
            # Fetch news
            articles = self.fetch_yahoo_news(ticker)
            
            if not articles:
                print("No news")
                continue
            
            # Insert articles
            inserted = 0
            for article in articles:
                if self.insert_news(market_data_id, ticker, article):
                    inserted += 1
            
            total_articles += len(articles)
            total_inserted += inserted
            
            print(f"✓ {inserted}/{len(articles)} articles inserted")
            
            # Rate limit
            time.sleep(0.5)
        
        print("-" * 80)
        print(f"\n✓ News population complete!")
        print(f"  Total articles fetched: {total_articles}")
        print(f"  New articles inserted:  {total_inserted}")
        print(f"  Duplicates skipped:     {total_articles - total_inserted}")
        print("=" * 80 + "\n")

def main():
    """Main execution."""
    populator = NewsPopulator()
    
    if not populator.connect():
        print("Failed to connect to database. Check your configuration.")
        return
    
    try:
        populator.populate_all_news()
    finally:
        populator.close()

if __name__ == "__main__":
    main()
