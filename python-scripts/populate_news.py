"""
Company News Populator - Finnhub API
====================================
Standalone script to extract company news from Finnhub API and insert into MySQL database.

Data Sources:
- Finnhub API (API key required)
- Groq LLM (optional, for AI-generated summaries when Finnhub summary is empty)

Output:
- Direct insertion into MySQL portfolio_db.news table

Setup:
    1. Install dependencies:
       pip install requests mysql-connector-python groq

    2. Configure Finnhub API key in this script or .env file:
       FINNHUB_API_KEY=your_api_key_here

Usage:
    python populate_news.py
"""

import requests
import mysql.connector
from datetime import datetime, timedelta
import os
import time
from typing import List, Dict

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
    print("Warning: groq library not installed. AI summaries will be disabled.")
    print("Install with: pip install groq")


# =============================================================================
# CONFIGURATION
# =============================================================================

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root351973',
    'database': 'portfolio_db',
    'port': 3306
}

# Companies to extract news for
COMPANIES = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS"
]

# Finnhub API key (required - set in .env file)
FINNHUB_API_KEY = os.getenv("FINNHUB_API_KEY")

# Groq API key (for AI summaries - set in .env file)
GROQ_API_KEY = os.getenv("GROQ_API_KEY")

# LLM Model for summaries
GROQ_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"


# =============================================================================
# NEWS EXTRACTOR CLASS
# =============================================================================

class NewsExtractor:
    """Extract news articles for companies from multiple sources."""
    
    def __init__(self, tickers: List[str]):
        self.tickers = tickers
        self.finnhub_key = FINNHUB_API_KEY
        self.groq_key = GROQ_API_KEY
        self.db_connection = None
        self.db_cursor = None
        
        # Initialize Groq client for AI summaries
        self.groq_client = None
        if GROQ_AVAILABLE and self.groq_key:
            try:
                self.groq_client = Groq(api_key=self.groq_key)
            except Exception as e:
                print(f"Warning: Failed to initialize Groq client: {e}")
        
        # Connect to database
        try:
            self.db_connection = mysql.connector.connect(**DB_CONFIG)
            self.db_cursor = self.db_connection.cursor()
            print(f"✓ Connected to MySQL database: {DB_CONFIG['database']}")
        except Exception as e:
            print(f"✗ Database connection failed: {e}")
            raise
        
        print("\n" + "=" * 70)
        print("NEWS DATA POPULATOR - Finnhub API")
        print("=" * 70)
        print(f"\nConfiguration:")
        print(f"  Companies to track: {len(self.tickers)}")
        print(f"  Database:           {DB_CONFIG['database']} @ {DB_CONFIG['host']}")
        print(f"  Finnhub API:        {'✓ Configured' if self.finnhub_key else '✗ Missing API key'}")
        print(f"  Groq LLM:           {'✓ Ready (' + GROQ_MODEL + ')' if self.groq_client else '✗ Disabled'}")
        print()
    
    # =========================================================================
    # AI SUMMARY GENERATION
    # =========================================================================
    
    def generate_ai_summary(self, title: str, ticker: str) -> str:
        """
        Generate a brief summary of a news article using Groq LLM.
        
        Args:
            title: The news article title
            ticker: The stock ticker symbol
            
        Returns:
            AI-generated summary (2-3 sentences)
        """
        if not self.groq_client:
            return ""
        
        try:
            prompt = f"""You are a financial news analyst. Generate a brief, informative summary (2-3 sentences) for this news headline about {ticker}:

Headline: {title}

Summary should:
- Be concise and professional
- Focus on the key financial impact or business development
- Be 2-3 sentences maximum
- Avoid speculation

Summary:"""

            response = self.groq_client.chat.completions.create(
                model=GROQ_MODEL,
                messages=[
                    {"role": "system", "content": "You are a financial news analyst providing brief, accurate summaries."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.3,
                max_tokens=150,
                top_p=0.9
            )
            
            summary = response.choices[0].message.content.strip()
            return summary
            
        except Exception as e:
            print(f"    ⚠ AI summary failed: {str(e)[:50]}")
            return ""
    
    # =========================================================================
    # FINNHUB NEWS EXTRACTION
    # =========================================================================
    
    def extract_finnhub_news(self, ticker: str) -> List[Dict]:
        """
        Extract news from Finnhub API.
        Returns list of news articles with summaries (uses AI if empty).
        """
        if not self.finnhub_key:
            return []
        
        try:
            # Get news from past 7 days
            end_date = datetime.now()
            start_date = end_date - timedelta(days=7)
            
            url = "https://finnhub.io/api/v1/company-news"
            params = {
                "symbol": ticker,
                "from": start_date.strftime("%Y-%m-%d"),
                "to": end_date.strftime("%Y-%m-%d"),
                "token": self.finnhub_key
            }
            
            response = requests.get(url, params=params, timeout=10)
            response.raise_for_status()
            news_data = response.json()
            
            if not news_data:
                return []
            
            articles = []
            for article in news_data[:20]:  # Limit to 20 most recent
                summary = article.get("summary", "")
                title = article.get("headline", "")
                
                # Generate AI summary if Finnhub summary is empty
                if not summary and self.groq_client and title:
                    summary = self.generate_ai_summary(title, ticker)
                    time.sleep(0.2)  # Rate limit for Groq API
                
                articles.append({
                    "ticker": ticker,
                    "source_api": "Finnhub",
                    "title": title,
                    "publisher": article.get("source", ""),
                    "link": article.get("url", ""),
                    "published_date": article.get("datetime", ""),
                    "summary": summary,
                    "image_url": article.get("image", ""),
                })
            
            return articles
            
        except Exception as e:
            print(f"  ⚠ Finnhub error for {ticker}: {str(e)}")
            return []
    
    # =========================================================================
    # HELPER METHODS
    # =========================================================================
    
    def _format_timestamp(self, timestamp) -> str:
        """Convert Unix timestamp to readable date string."""
        if not timestamp:
            return None
        try:
            if isinstance(timestamp, (int, float)):
                return datetime.fromtimestamp(timestamp)
            return datetime.strptime(str(timestamp), "%Y-%m-%d %H:%M:%S")
        except:
            return None
    
    def _insert_article(self, article: Dict) -> bool:
        """Insert a news article into the database."""
        try:
            # Check if article already exists (by link)
            if article.get('link'):
                self.db_cursor.execute(
                    "SELECT id FROM news WHERE link = %s",
                    (article['link'],)
                )
                if self.db_cursor.fetchone():
                    return False  # Duplicate
            
            # Insert new article
            insert_query = """
                INSERT INTO news (symbol, title, summary, publisher, link, 
                                published_date, source, image_url, created_date)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
            """
            
            values = (
                article.get('ticker', ''),
                article.get('title', ''),
                article.get('summary', ''),
                article.get('publisher', ''),
                article.get('link', ''),
                self._format_timestamp(article.get('published_date')),
                article.get('source_api', ''),
                article.get('image_url', '')
            )
            
            self.db_cursor.execute(insert_query, values)
            self.db_connection.commit()
            return True
            
        except Exception as e:
            print(f"    ✗ Insert failed: {str(e)[:50]}")
            return False
    
    # =========================================================================
    # MAIN EXTRACTION
    # =========================================================================
    
    def extract_all_news(self):
        """Extract news from all sources for all tickers."""
        total_inserted = 0
        total_duplicates = 0
        
        print(f"Starting news extraction for {len(self.tickers)} companies...")
        print("-" * 70)
        
        for i, ticker in enumerate(self.tickers, 1):
            print(f"[{i}/{len(self.tickers)}] {ticker:6} ...", end=" ")
            
            ticker_inserted = 0
            ticker_duplicates = 0
            
            # Extract Finnhub news
            if self.finnhub_key:
                finnhub_articles = self.extract_finnhub_news(ticker)
                for article in finnhub_articles:
                    if self._insert_article(article):
                        ticker_inserted += 1
                    else:
                        ticker_duplicates += 1
                time.sleep(1.2)  # Rate limit: 60 calls/minute for free tier
            else:
                print("✗ No API key")
                continue
            
            total_inserted += ticker_inserted
            total_duplicates += ticker_duplicates
            
            if ticker_inserted > 0:
                print(f"✓ {ticker_inserted} inserted")
            else:
                print("No new articles")
        
        print("-" * 70)
        print(f"\n✓ News extraction complete!")
        print(f"  Total articles inserted:  {total_inserted}")
        print(f"  Duplicates skipped:       {total_duplicates}")
        print("=" * 70)
        
        return total_inserted, total_duplicates
    
    def close(self):
        """Close database connection."""
        if self.db_cursor:
            self.db_cursor.close()
        if self.db_connection:
            self.db_connection.close()
            print("✓ Database connection closed")


# =============================================================================
# MAIN EXECUTION
# =============================================================================

def main():
    """Main execution function."""
    
    extractor = None
    try:
        # Initialize extractor
        extractor = NewsExtractor(tickers=COMPANIES)
        
        # Extract and insert all news
        total_inserted, total_duplicates = extractor.extract_all_news()
        
        print(f"\nFinal Summary:")
        print(f"  New articles added:     {total_inserted}")
        print(f"  Duplicates skipped:     {total_duplicates}")
        print(f"  Total processed:        {total_inserted + total_duplicates}")
        print()
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
    finally:
        if extractor:
            extractor.close()


if __name__ == "__main__":
    main()
