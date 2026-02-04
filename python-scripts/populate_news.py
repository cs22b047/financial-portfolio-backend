"""
Company News Populator - Multi-Source API
==========================================
Standalone script to extract news from multiple sources and insert into MySQL database.

Data Sources:
- Finnhub API: Stock and ETF/mutual fund news
- CryptoCompare API: Cryptocurrency news (no API key required)
- Groq LLM: Optional AI-generated summaries when source summary is empty

Output:
- Direct insertion into MySQL portfolio_db.news table

Setup:
    1. Install dependencies:
       pip install requests mysql-connector-python groq

    2. Configure Finnhub API key in this script or .env file:
       FINNHUB_API_KEY=your_api_key_here

Usage:
    python populate_news_v2.py
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

# Companies to extract news for (stocks)
COMPANIES = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS"
]

# Mutual funds/ETFs to extract news for (tickers)
MUTUAL_FUND_TICKERS = [
    "SPY",    # SPDR S&P 500 ETF
    "QQQ",    # Invesco QQQ Trust
    "VOO",    # Vanguard S&P 500 ETF
    "VTI",    # Vanguard Total Stock Market ETF
    "IVV",    # iShares Core S&P 500 ETF
    "VEA",    # Vanguard FTSE Developed Markets ETF
    "IEMG",   # iShares Core MSCI Emerging Markets ETF
    "VWO",    # Vanguard FTSE Emerging Markets ETF
    "BND",    # Vanguard Total Bond Market ETF
    "AGG"     # iShares Core US Aggregate Bond ETF
]

# Cryptocurrencies to extract news for (symbols)
CRYPTOCURRENCIES = [
    "BTC",    # Bitcoin
    "ETH",    # Ethereum
    "USDT",   # Tether
    "BNB",    # Binance Coin
    "SOL",    # Solana
    "XRP",    # Ripple
    "USDC",   # USD Coin
    "ADA",    # Cardano
    "DOGE",   # Dogecoin
    "TRX",    # TRON
    "AVAX",   # Avalanche
    "LINK",   # Chainlink
    "DOT",    # Polkadot
    "MATIC",  # Polygon
    "LTC"     # Litecoin
]

# Finnhub API key (required for stocks/ETFs - set in .env file)
FINNHUB_API_KEY = os.getenv("FINNHUB_API_KEY")

# Groq API key (for AI summaries - set in .env file)
GROQ_API_KEY = os.getenv("GROQ_API_KEY")

# LLM Model for summaries
GROQ_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"


# =============================================================================
# NEWS EXTRACTOR CLASS
# =============================================================================

class NewsExtractor:
    """Extract news articles for stocks, mutual funds/ETFs, and cryptocurrencies."""
    
    def __init__(self, stock_tickers: List[str] = None, mutual_funds: List[str] = None, cryptos: List[str] = None):
        self.stock_tickers = stock_tickers or []
        self.mutual_funds = mutual_funds or []
        self.cryptos = cryptos or []
        self.finnhub_key = FINNHUB_API_KEY
        self.groq_key = GROQ_API_KEY
        self.db_connection = None
        self.db_cursor = None
        
        # Initialize Groq client for AI summaries
        if GROQ_AVAILABLE and self.groq_key:
            try:
                self.groq_client = Groq(api_key=self.groq_key)
                print("✓ Groq LLM initialized for AI summaries")
            except Exception as e:
                self.groq_client = None
                print(f"⚠ Groq initialization failed: {e}")
        else:
            self.groq_client = None
            if not GROQ_AVAILABLE:
                print("⚠ Groq library not available - AI summaries disabled")
    
    # =========================================================================
    # DATABASE CONNECTION
    # =========================================================================
    
    def connect_db(self):
        """Establish database connection."""
        try:
            self.db_connection = mysql.connector.connect(**DB_CONFIG)
            self.db_cursor = self.db_connection.cursor()
            print("✓ Connected to MySQL database")
            return True
        except Exception as e:
            print(f"✗ Database connection failed: {e}")
            return False
    
    # =========================================================================
    # AI SUMMARY GENERATION
    # =========================================================================
    
    def generate_ai_summary(self, title: str, max_length: int = 200) -> str:
        """Generate AI summary from article title using Groq LLM."""
        if not self.groq_client:
            return ""
        
        try:
            prompt = f"""Based on this news headline, write a brief 2-3 sentence summary suitable for investors:

Headline: {title}

Summary (2-3 sentences):"""
            
            response = self.groq_client.chat.completions.create(
                model=GROQ_MODEL,
                messages=[
                    {"role": "system", "content": "You are a financial news summarizer. Create concise, informative summaries for investors."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.5,
                max_tokens=150,
                top_p=0.9
            )
            
            summary = response.choices[0].message.content.strip()
            return summary[:max_length]
            
        except Exception as e:
            print(f"    ✗ AI summary failed: {str(e)[:40]}")
            return ""
    
    # =========================================================================
    # CRYPTOCURRENCY NEWS EXTRACTION
    # =========================================================================
    
    def extract_crypto_news(self, crypto_symbol: str) -> List[Dict]:
        """
        Extract cryptocurrency news from CryptoCompare API.
        Free API, no key required.
        """
        try:
            # Use CryptoCompare News API
            url = "https://min-api.cryptocompare.com/data/v2/news/"
            params = {
                "lang": "EN",
                "categories": crypto_symbol
            }
            
            response = requests.get(url, params=params, timeout=10)
            response.raise_for_status()
            news_data = response.json()
            
            if news_data.get('Response') != 'Success':
                return []
            
            articles = []
            raw_articles = news_data.get('Data', [])
            
            # Filter and process articles
            for article in raw_articles[:15]:  # Get more, filter later
                title = article.get('title', '')
                body = article.get('body', '')
                
                # Check if crypto symbol appears in title or body
                if crypto_symbol.lower() in title.lower() or crypto_symbol.lower() in body.lower():
                    articles.append({
                        "ticker": crypto_symbol,
                        "source_api": "CryptoCompare",
                        "title": title,
                        "publisher": article.get('source', 'Unknown'),
                        "link": article.get('url', ''),
                        "published_date": article.get('published_on', int(datetime.now().timestamp())),
                        "summary": body[:500] if body else title,  # First 500 chars
                        "image_url": article.get('imageurl', ''),
                    })
                    
                    if len(articles) >= 10:  # Limit to 10 articles per crypto
                        break
            
            return articles
            
        except Exception as e:
            print(f"✗ CryptoCompare failed: {str(e)[:40]}")
            return []
    
    # =========================================================================
    # FINNHUB NEWS EXTRACTION (Stocks & ETFs)
    # =========================================================================
    
    def extract_finnhub_news(self, ticker: str) -> List[Dict]:
        """
        Extract news from Finnhub API.
        Works for both stocks and ETF/mutual fund tickers.
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
                    summary = self.generate_ai_summary(title)
                
                articles.append({
                    "ticker": ticker,
                    "source_api": "Finnhub",
                    "title": title,
                    "publisher": article.get("source", "Unknown"),
                    "link": article.get("url", ""),
                    "published_date": article.get("datetime", int(datetime.now().timestamp())),
                    "summary": summary,
                    "image_url": article.get("image", ""),
                })
            
            return articles
            
        except Exception as e:
            print(f"✗ Finnhub failed: {str(e)[:40]}")
            return []
    
    # =========================================================================
    # HELPERS
    # =========================================================================
    
    def _format_timestamp(self, timestamp):
        """Convert Unix timestamp to MySQL datetime."""
        if not timestamp:
            return None
        try:
            return datetime.fromtimestamp(int(timestamp))
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
        """Extract news from all sources for all asset types."""
        total_inserted = 0
        total_duplicates = 0
        
        print("\n" + "="*70)
        print("NEWS EXTRACTION - ALL ASSET TYPES")
        print("="*70)
        
        # 1. Extract stock news
        if self.stock_tickers and self.finnhub_key:
            print(f"\n📈 Extracting news for {len(self.stock_tickers)} stocks...")
            print("-" * 70)
            
            for i, ticker in enumerate(self.stock_tickers, 1):
                print(f"[{i}/{len(self.stock_tickers)}] {ticker:6} ...", end=" ")
                
                finnhub_articles = self.extract_finnhub_news(ticker)
                inserted = 0
                duplicates = 0
                
                for article in finnhub_articles:
                    if self._insert_article(article):
                        inserted += 1
                    else:
                        duplicates += 1
                
                total_inserted += inserted
                total_duplicates += duplicates
                
                if inserted > 0:
                    print(f"✓ {inserted} inserted")
                else:
                    print("No new articles")
                
                time.sleep(1.2)  # Rate limit: 60 calls/minute for free tier
        
        # 2. Extract mutual fund/ETF news
        if self.mutual_funds and self.finnhub_key:
            print(f"\n📊 Extracting news for {len(self.mutual_funds)} mutual funds/ETFs...")
            print("-" * 70)
            
            for i, ticker in enumerate(self.mutual_funds, 1):
                print(f"[{i}/{len(self.mutual_funds)}] {ticker:6} ...", end=" ")
                
                finnhub_articles = self.extract_finnhub_news(ticker)
                inserted = 0
                duplicates = 0
                
                for article in finnhub_articles:
                    if self._insert_article(article):
                        inserted += 1
                    else:
                        duplicates += 1
                
                total_inserted += inserted
                total_duplicates += duplicates
                
                if inserted > 0:
                    print(f"✓ {inserted} inserted")
                else:
                    print("No new articles")
                
                time.sleep(1.2)  # Rate limit
        
        # 3. Extract cryptocurrency news
        if self.cryptos:
            print(f"\n₿ Extracting news for {len(self.cryptos)} cryptocurrencies...")
            print("-" * 70)
            
            for i, crypto in enumerate(self.cryptos, 1):
                print(f"[{i}/{len(self.cryptos)}] {crypto:6} ...", end=" ")
                
                crypto_articles = self.extract_crypto_news(crypto)
                inserted = 0
                duplicates = 0
                
                for article in crypto_articles:
                    if self._insert_article(article):
                        inserted += 1
                    else:
                        duplicates += 1
                
                total_inserted += inserted
                total_duplicates += duplicates
                
                if inserted > 0:
                    print(f"✓ {inserted} inserted")
                else:
                    print("No new articles")
                
                time.sleep(0.5)  # Lighter rate limit for free API
        
        print("\n" + "="*70)
        print("NEWS EXTRACTION SUMMARY")
        print("="*70)
        print(f"✓ Total articles inserted:  {total_inserted}")
        print(f"  - Stocks:                 {len(self.stock_tickers)} tickers")
        print(f"  - Mutual Funds/ETFs:      {len(self.mutual_funds)} tickers")
        print(f"  - Cryptocurrencies:       {len(self.cryptos)} symbols")
        print(f"✗ Duplicates skipped:       {total_duplicates}")
        print("="*70)
        
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
        # Initialize extractor with all asset types
        extractor = NewsExtractor(
            stock_tickers=COMPANIES,
            mutual_funds=MUTUAL_FUND_TICKERS,
            cryptos=CRYPTOCURRENCIES
        )
        
        # Connect to database
        if not extractor.connect_db():
            print("\n✗ Failed to connect to database. Exiting.")
            return
        
        # Extract and insert all news
        total_inserted, total_duplicates = extractor.extract_all_news()
        
        print(f"\n✓ Process completed!")
        print(f"  New articles added:     {total_inserted}")
        print(f"  Duplicates skipped:     {total_duplicates}")
        print(f"  Total processed:        {total_inserted + total_duplicates}\n")
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if extractor:
            extractor.close()


if __name__ == "__main__":
    main()
