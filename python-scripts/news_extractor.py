"""
Company News Extractor with AI Summaries
========================================
Standalone script to extract company news from multiple sources with AI-generated summaries.

Data Sources:
- Yahoo Finance (free, no API key needed)
- Finnhub (optional, API key required for more news)
- Groq LLM (meta-llama/llama-4-scout-17b-16e-instruct for AI summaries)

Output:
- CSV file with company news articles including title, AI summary, source, date

Setup:
    1. Install dependencies:
       pip install yfinance requests pandas python-dotenv groq

    2. Optional - Create .env file with:
       FINNHUB_API_KEY=your_api_key_here
       GROQ_API_KEY=your_groq_key_here

Usage:
    python news_extractor.py
"""

import yfinance as yf
import requests
import pandas as pd
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

# Companies to extract news for
COMPANIES = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS"
]

# Output directory
OUTPUT_DIR = "company_news"

# Finnhub API key (optional)
FINNHUB_API_KEY = os.getenv("FINNHUB_API_KEY")

# Groq API key (for AI summaries)
GROQ_API_KEY = os.getenv("GROQ_API_KEY")

# LLM Model for summaries
GROQ_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"


# =============================================================================
# NEWS EXTRACTOR CLASS
# =============================================================================

class NewsExtractor:
    """Extract news articles for companies from multiple sources."""
    
    def __init__(self, tickers: List[str], output_dir: str = OUTPUT_DIR):
        self.tickers = tickers
        self.output_dir = output_dir
        self.finnhub_key = FINNHUB_API_KEY
        self.groq_key = GROQ_API_KEY
        self.timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        # Initialize Groq client for AI summaries
        self.groq_client = None
        if GROQ_AVAILABLE and self.groq_key:
            try:
                self.groq_client = Groq(api_key=self.groq_key)
            except Exception as e:
                print(f"Warning: Failed to initialize Groq client: {e}")
        
        # Create output directory
        os.makedirs(output_dir, exist_ok=True)
        
        print("\n" + "=" * 70)
        print("COMPANY NEWS EXTRACTOR WITH AI SUMMARIES")
        print("=" * 70)
        print(f"\nConfiguration:")
        print(f"  Companies to track: {len(self.tickers)}")
        print(f"  Output directory:   {self.output_dir}")
        print(f"  Yahoo Finance:      ✓ Available")
        print(f"  Finnhub API:        {'✓ Key found' if self.finnhub_key else '✗ No key (optional)'}")
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
    # YAHOO FINANCE NEWS
    # =========================================================================
    
    def extract_yahoo_news(self, ticker: str) -> List[Dict]:
        """
        Extract news from Yahoo Finance.
        Returns list of news articles with metadata and AI-generated summaries.
        """
        try:
            stock = yf.Ticker(ticker)
            news = stock.news
            
            if not news:
                return []
            
            articles = []
            for article in news:
                title = article.get("title", "")
                
                # Generate AI summary for the article
                ai_summary = ""
                if self.groq_client and title:
                    ai_summary = self.generate_ai_summary(title, ticker)
                    time.sleep(0.2)  # Rate limit for Groq API
                
                articles.append({
                    "ticker": ticker,
                    "source": "Yahoo Finance",
                    "title": title,
                    "publisher": article.get("publisher", ""),
                    "link": article.get("link", ""),
                    "published_date": self._format_timestamp(article.get("providerPublishTime")),
                    "type": article.get("type", ""),
                    "summary": ai_summary,
                    "thumbnail": article.get("thumbnail", {}).get("resolutions", [{}])[0].get("url", "") if article.get("thumbnail") else "",
                    "related_tickers": ",".join(article.get("relatedTickers", [])),
                })
            
            return articles
            
        except Exception as e:
            print(f"  ⚠ Yahoo Finance error for {ticker}: {str(e)}")
            return []
    
    # =========================================================================
    # FINNHUB NEWS
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
                    "source": "Finnhub",
                    "title": title,
                    "publisher": article.get("source", ""),
                    "link": article.get("url", ""),
                    "published_date": self._format_timestamp(article.get("datetime")),
                    "summary": summary,
                    "category": article.get("category", ""),
                    "image": article.get("image", ""),
                    "related_symbols": article.get("related", ""),
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
            return ""
        try:
            if isinstance(timestamp, (int, float)):
                return datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M:%S")
            return str(timestamp)
        except:
            return ""
    
    # =========================================================================
    # MAIN EXTRACTION
    # =========================================================================
    
    def extract_all_news(self):
        """Extract news from all sources for all tickers."""
        all_yahoo_news = []
        all_finnhub_news = []
        
        print(f"Starting news extraction for {len(self.tickers)} companies...")
        print("-" * 70)
        
        for i, ticker in enumerate(self.tickers, 1):
            print(f"[{i}/{len(self.tickers)}] Processing {ticker}...", end=" ")
            
            # Yahoo Finance news (with AI summaries)
            yahoo_articles = self.extract_yahoo_news(ticker)
            all_yahoo_news.extend(yahoo_articles)
            
            # Finnhub news (with rate limiting)
            if self.finnhub_key:
                finnhub_articles = self.extract_finnhub_news(ticker)
                all_finnhub_news.extend(finnhub_articles)
                time.sleep(1)  # Rate limit: 1 request per second for free tier
            else:
                finnhub_articles = []
            
            print(f"✓ ({len(yahoo_articles)} Yahoo, {len(finnhub_articles)} Finnhub)")
        
        print("-" * 70)
        
        # Save to CSV files
        self._save_news_data(all_yahoo_news, all_finnhub_news)
        
        return all_yahoo_news, all_finnhub_news
    
    def _save_news_data(self, yahoo_news: List[Dict], finnhub_news: List[Dict]):
        """Save news data to CSV files."""
        
        # Combined news (all sources)
        all_news = []
        
        # Standardize Yahoo news
        for article in yahoo_news:
            all_news.append({
                "ticker": article["ticker"],
                "title": article["title"],
                "publisher": article["publisher"],
                "published_date": article["published_date"],
                "link": article["link"],
                "source_api": "Yahoo Finance",
                "summary": article.get("summary", ""),
                "image_url": article.get("thumbnail", ""),
            })
        
        # Standardize Finnhub news
        for article in finnhub_news:
            all_news.append({
                "ticker": article["ticker"],
                "title": article["title"],
                "publisher": article["publisher"],
                "published_date": article["published_date"],
                "link": article["link"],
                "source_api": "Finnhub",
                "summary": article.get("summary", ""),
                "image_url": article.get("image", ""),
            })
        
        if all_news:
            df_combined = pd.DataFrame(all_news)
            
            # Remove duplicates based on article link (same article shouldn't appear multiple times)
            # Keep first occurrence for each unique article
            df_combined = df_combined.drop_duplicates(subset=['link'], keep='first')
            
            # Sort by date (most recent first)
            df_combined = df_combined.sort_values("published_date", ascending=False)
            
            combined_file = f"{self.output_dir}/combined_news_{self.timestamp}.csv"
            df_combined.to_csv(combined_file, index=False)
            print(f"\n✓ Combined news saved: {combined_file}")
            print(f"  Total articles (after deduplication): {len(df_combined)}")
            print(f"  With summaries: {sum(1 for _, row in df_combined.iterrows() if row.get('summary'))}")
            print(f"  Date range: {df_combined['published_date'].min()} to {df_combined['published_date'].max()}")
        
        print("\n" + "=" * 70)
        print("News extraction complete!")
        print("=" * 70)


# =============================================================================
# MAIN EXECUTION
# =============================================================================

def main():
    """Main execution function."""
    
    # Initialize extractor
    extractor = NewsExtractor(tickers=COMPANIES)
    
    # Extract all news
    yahoo_news, finnhub_news = extractor.extract_all_news()
    
    # Summary statistics
    print(f"\nSummary:")
    print(f"  Total Yahoo articles:   {len(yahoo_news)}")
    print(f"  Total Finnhub articles: {len(finnhub_news)}")
    print(f"  Grand total:            {len(yahoo_news) + len(finnhub_news)}")
    print(f"\nFiles saved to: {OUTPUT_DIR}/")
    print()


if __name__ == "__main__":
    main()
