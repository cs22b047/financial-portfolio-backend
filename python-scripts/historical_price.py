"""
Historical Stock Price Extractor
=================================
Extracts comprehensive historical price data for all stocks from the unified finance extractor.
Supports multiple time periods and calculates key metrics.

Features:
- Configurable time periods (1y, 2y, 5y, 10y, max)
- Daily OHLCV data (Open, High, Low, Close, Volume)
- Calculated metrics: Daily returns, moving averages, RSI, Bollinger Bands
- Support for batch processing with rate limiting
- Export to CSV with timestamps

Setup:
    1. Install dependencies:
       pip install yfinance pandas numpy ta-lib

Usage:
    # Get 5 years of history for all stocks
    python historical_price_extractor.py

    # Or use in your code:
    from historical_price_extractor import HistoricalPriceExtractor
    extractor = HistoricalPriceExtractor()
    data = extractor.extract_all(period="5y")
"""

import yfinance as yf
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import os
import time
from typing import List, Dict, Optional
import warnings
import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv
warnings.filterwarnings('ignore')

# Load environment variables
load_dotenv()


# =============================================================================
# STOCK LIST (from unified finance extractor)
# =============================================================================

TOP_COMPANIES = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
    "V", "XOM", "JPM", "WMT", "MA", "PG", "HD", "CVX", "MRK", "ABBV",
    "LLY", "PEP", "KO", "COST", "AVGO", "PFE", "TMO", "MCD", "CSCO", "ACN",
    "CRM", "ABT", "DHR", "NKE", "ORCL", "TXN", "NFLX", "AMD", "INTC", "DIS",
    "VZ", "ADBE", "PM", "NEE", "WFC", "BAC", "RTX", "UPS", "COP", "QCOM"
]

BENCHMARK_INDICES = {
    "^GSPC": "S&P 500",
    "^IXIC": "NASDAQ Composite",
    "^DJI": "Dow Jones Industrial Average",
    "^RUT": "Russell 2000"
}


# =============================================================================
# HISTORICAL PRICE EXTRACTOR
# =============================================================================

class HistoricalPriceExtractor:
    """
    Extracts historical price data for stocks with technical indicators.
    """

    def __init__(self, output_dir: str = "historical_price_data"):
        """
        Initialize the historical price extractor.

        Args:
            output_dir: Directory to save CSV files
        """
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        print("\n" + "=" * 80)
        print("HISTORICAL STOCK PRICE EXTRACTOR")
        print("=" * 80)
        print(f"\nOutput directory: {output_dir}")

    def get_price_history(
        self,
        ticker: str,
        period: str = "5y",
        interval: str = "1d",
        include_indicators: bool = True
    ) -> pd.DataFrame:
        """
        Get historical price data for a single ticker.

        Args:
            ticker: Stock ticker symbol
            period: Time period - valid values: 1d, 5d, 1mo, 3mo, 6mo, 1y, 2y, 5y, 10y, ytd, max
            interval: Data interval - valid values: 1m, 2m, 5m, 15m, 30m, 60m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo
            include_indicators: Whether to calculate technical indicators

        Returns:
            DataFrame with historical price data and indicators
        """
        try:
            stock = yf.Ticker(ticker)
            hist = stock.history(period=period, interval=interval)

            if hist.empty:
                return pd.DataFrame()

            # Reset index to make Date a column
            hist = hist.reset_index()
            hist["ticker"] = ticker

            # Calculate returns
            hist["dailyReturn"] = hist["Close"].pct_change()
            hist["cumulativeReturn"] = (1 + hist["dailyReturn"]).cumprod() - 1

            # Calculate log returns (for statistical analysis)
            hist["logReturn"] = np.log(hist["Close"] / hist["Close"].shift(1))

            if include_indicators and len(hist) > 50:
                # Moving averages
                hist["SMA_20"] = hist["Close"].rolling(window=20).mean()
                hist["SMA_50"] = hist["Close"].rolling(window=50).mean()
                hist["SMA_200"] = hist["Close"].rolling(window=200).mean()

                # Exponential moving averages
                hist["EMA_12"] = hist["Close"].ewm(span=12, adjust=False).mean()
                hist["EMA_26"] = hist["Close"].ewm(span=26, adjust=False).mean()

                # MACD (Moving Average Convergence Divergence)
                hist["MACD"] = hist["EMA_12"] - hist["EMA_26"]
                hist["MACD_Signal"] = hist["MACD"].ewm(span=9, adjust=False).mean()
                hist["MACD_Histogram"] = hist["MACD"] - hist["MACD_Signal"]

                # Bollinger Bands
                hist["BB_Middle"] = hist["Close"].rolling(window=20).mean()
                bb_std = hist["Close"].rolling(window=20).std()
                hist["BB_Upper"] = hist["BB_Middle"] + (bb_std * 2)
                hist["BB_Lower"] = hist["BB_Middle"] - (bb_std * 2)
                hist["BB_Width"] = hist["BB_Upper"] - hist["BB_Lower"]

                # RSI (Relative Strength Index)
                delta = hist["Close"].diff()
                gain = (delta.where(delta > 0, 0)).rolling(window=14).mean()
                loss = (-delta.where(delta < 0, 0)).rolling(window=14).mean()
                rs = gain / loss
                hist["RSI"] = 100 - (100 / (1 + rs))

                # ATR (Average True Range) - Volatility indicator
                high_low = hist["High"] - hist["Low"]
                high_close = np.abs(hist["High"] - hist["Close"].shift())
                low_close = np.abs(hist["Low"] - hist["Close"].shift())
                ranges = pd.concat([high_low, high_close, low_close], axis=1)
                true_range = np.max(ranges, axis=1)
                hist["ATR"] = true_range.rolling(14).mean()

                # Volume indicators
                hist["Volume_SMA_20"] = hist["Volume"].rolling(window=20).mean()
                hist["Volume_Ratio"] = hist["Volume"] / hist["Volume_SMA_20"]

                # Price momentum
                hist["Momentum_10"] = hist["Close"] - hist["Close"].shift(10)
                hist["ROC_10"] = ((hist["Close"] - hist["Close"].shift(10)) / hist["Close"].shift(10)) * 100

                # Stochastic Oscillator
                low_14 = hist["Low"].rolling(window=14).min()
                high_14 = hist["High"].rolling(window=14).max()
                hist["Stochastic_K"] = 100 * ((hist["Close"] - low_14) / (high_14 - low_14))
                hist["Stochastic_D"] = hist["Stochastic_K"].rolling(window=3).mean()

            # Reorder columns for better readability
            base_cols = ["Date", "ticker", "Open", "High", "Low", "Close", "Volume"]
            return_cols = ["dailyReturn", "cumulativeReturn", "logReturn"]

            other_cols = [col for col in hist.columns if col not in base_cols + return_cols]
            final_cols = base_cols + return_cols + other_cols

            return hist[final_cols]

        except Exception as e:
            print(f"    ERROR: {str(e)}")
            return pd.DataFrame()

    def get_price_summary(self, ticker: str, period: str = "5y") -> dict:
        """
        Get summary statistics for price history.

        Args:
            ticker: Stock ticker symbol
            period: Time period

        Returns:
            Dictionary with summary statistics
        """
        try:
            hist = self.get_price_history(ticker, period, include_indicators=False)

            if hist.empty or len(hist) < 2:
                return {"ticker": ticker, "error": "Insufficient data"}

            returns = hist["dailyReturn"].dropna()

            summary = {
                "ticker": ticker,
                "startDate": hist["Date"].iloc[0].strftime("%Y-%m-%d") if hasattr(hist["Date"].iloc[0], 'strftime') else str(hist["Date"].iloc[0]),
                "endDate": hist["Date"].iloc[-1].strftime("%Y-%m-%d") if hasattr(hist["Date"].iloc[-1], 'strftime') else str(hist["Date"].iloc[-1]),
                "tradingDays": len(hist),

                # Price metrics
                "startPrice": round(hist["Close"].iloc[0], 2),
                "endPrice": round(hist["Close"].iloc[-1], 2),
                "minPrice": round(hist["Low"].min(), 2),
                "maxPrice": round(hist["High"].max(), 2),
                "avgPrice": round(hist["Close"].mean(), 2),

                # Return metrics
                "totalReturn": round(hist["cumulativeReturn"].iloc[-1] * 100, 2),
                "avgDailyReturn": round(returns.mean() * 100, 4),
                "annualizedReturn": round(returns.mean() * 252 * 100, 2),

                # Risk metrics
                "dailyVolatility": round(returns.std() * 100, 4),
                "annualizedVolatility": round(returns.std() * np.sqrt(252) * 100, 2),
                "sharpeRatio": round((returns.mean() / returns.std()) * np.sqrt(252), 2) if returns.std() > 0 else 0,

                # Extreme values
                "maxDailyGain": round(returns.max() * 100, 2),
                "maxDailyLoss": round(returns.min() * 100, 2),

                # Volume
                "avgVolume": int(hist["Volume"].mean()),
                "maxVolume": int(hist["Volume"].max()),
                "minVolume": int(hist["Volume"].min()),
            }

            # Value at Risk (VaR) at 95% and 99% confidence levels
            summary["VaR_95"] = round(returns.quantile(0.05) * 100, 2)
            summary["VaR_99"] = round(returns.quantile(0.01) * 100, 2)

            # Drawdown analysis
            cumulative = (1 + returns).cumprod()
            running_max = cumulative.expanding().max()
            drawdown = (cumulative - running_max) / running_max
            summary["maxDrawdown"] = round(drawdown.min() * 100, 2)

            return summary

        except Exception as e:
            return {"ticker": ticker, "error": str(e)}

    def extract_all(
        self,
        tickers: List[str] = None,
        period: str = "5y",
        interval: str = "1d",
        include_indicators: bool = True,
        include_benchmarks: bool = True,
        delay: float = 0.3
    ) -> Dict[str, pd.DataFrame]:
        """
        Extract historical price data for all tickers.

        Args:
            tickers: List of ticker symbols (default: TOP_COMPANIES)
            period: Time period for historical data
            interval: Data interval
            include_indicators: Whether to calculate technical indicators
            include_benchmarks: Whether to include benchmark indices
            delay: Delay between API calls in seconds

        Returns:
            Dictionary with DataFrames for prices and summaries
        """
        if tickers is None:
            tickers = TOP_COMPANIES

        print(f"\n{'='*80}")
        print(f"EXTRACTING HISTORICAL PRICE DATA")
        print(f"{'='*80}")
        print(f"\nConfiguration:")
        print(f"  Tickers: {len(tickers)} stocks")
        print(f"  Period: {period}")
        print(f"  Interval: {interval}")
        print(f"  Technical Indicators: {'Yes' if include_indicators else 'No'}")
        print(f"  Benchmark Indices: {'Yes' if include_benchmarks else 'No'}")

        all_prices = []
        all_summaries = []
        failed_tickers = []

        # Extract stock data
        print(f"\n{'='*80}")
        print("EXTRACTING STOCK DATA")
        print(f"{'='*80}")

        for i, ticker in enumerate(tickers):
            print(f"\n[{i+1}/{len(tickers)}] {ticker}")
            print("-" * 40)

            # Get price history
            print("  Getting price history...", end=" ")
            prices = self.get_price_history(ticker, period, interval, include_indicators)

            if not prices.empty:
                all_prices.append(prices)
                print(f"OK ({len(prices)} days)")

                # Get summary
                print("  Calculating summary...", end=" ")
                summary = self.get_price_summary(ticker, period)
                if "error" not in summary:
                    all_summaries.append(summary)
                    print("OK")
                else:
                    print("SKIP")
            else:
                print("FAILED")
                failed_tickers.append(ticker)

            time.sleep(delay)

        # Extract benchmark data
        if include_benchmarks:
            print(f"\n{'='*80}")
            print("EXTRACTING BENCHMARK INDICES")
            print(f"{'='*80}")

            for symbol, name in BENCHMARK_INDICES.items():
                print(f"\n{name} ({symbol})")
                print("-" * 40)

                print("  Getting price history...", end=" ")
                prices = self.get_price_history(symbol, period, interval, include_indicators)

                if not prices.empty:
                    all_prices.append(prices)
                    print(f"OK ({len(prices)} days)")

                    print("  Calculating summary...", end=" ")
                    summary = self.get_price_summary(symbol, period)
                    if "error" not in summary:
                        summary["name"] = name
                        all_summaries.append(summary)
                        print("OK")
                    else:
                        print("SKIP")
                else:
                    print("FAILED")

                time.sleep(delay)

        # Combine results
        result = {}

        if all_prices:
            result["prices"] = pd.concat(all_prices, ignore_index=True)
            print(f"\n  Total price records: {len(result['prices'])}")

        if all_summaries:
            result["summaries"] = pd.DataFrame(all_summaries)
            print(f"  Total summaries: {len(result['summaries'])}")

        if failed_tickers:
            print(f"\n  Failed tickers: {', '.join(failed_tickers)}")

        return result

    def save_to_csv(self, data: Dict[str, pd.DataFrame], prefix: str = None):
        """
        Save extracted data to CSV files.

        Args:
            data: Dictionary with DataFrames
            prefix: Optional prefix for filenames (e.g., "5y", "10y")
        """
        print(f"\n{'='*80}")
        print("SAVING DATA TO CSV FILES")
        print(f"{'='*80}")

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

        for name, df in data.items():
            if df.empty:
                continue

            try:
                if prefix:
                    filename = f"{prefix}_{name}_{timestamp}.csv"
                else:
                    filename = f"{name}_{timestamp}.csv"

                filepath = os.path.join(self.output_dir, filename)
                df.to_csv(filepath, index=False)

                print(f"  {name}: {filepath}")
                print(f"    Rows: {len(df)}")
                if "ticker" in df.columns:
                    print(f"    Tickers: {df['ticker'].nunique()}")

            except Exception as e:
                print(f"  {name}: ERROR - {e}")

        print(f"\n  All files saved to: {self.output_dir}/")

    def quick_stats(self, data: Dict[str, pd.DataFrame]):
        """
        Print quick statistics about the extracted data.

        Args:
            data: Dictionary with DataFrames
        """
        if "summaries" not in data or data["summaries"].empty:
            return

        summaries = data["summaries"]

        print(f"\n{'='*80}")
        print("QUICK STATISTICS")
        print(f"{'='*80}")

        print(f"\nTop 10 Performers (Total Return):")
        top_performers = summaries.nlargest(10, "totalReturn")[["ticker", "totalReturn", "annualizedReturn"]]
        for idx, row in top_performers.iterrows():
            print(f"  {row['ticker']:8s} {row['totalReturn']:8.2f}%  (Annualized: {row['annualizedReturn']:6.2f}%)")

        print(f"\nWorst 10 Performers (Total Return):")
        worst_performers = summaries.nsmallest(10, "totalReturn")[["ticker", "totalReturn", "annualizedReturn"]]
        for idx, row in worst_performers.iterrows():
            print(f"  {row['ticker']:8s} {row['totalReturn']:8.2f}%  (Annualized: {row['annualizedReturn']:6.2f}%)")

        print(f"\nHighest Volatility:")
        high_vol = summaries.nlargest(10, "annualizedVolatility")[["ticker", "annualizedVolatility", "sharpeRatio"]]
        for idx, row in high_vol.iterrows():
            print(f"  {row['ticker']:8s} {row['annualizedVolatility']:6.2f}%  (Sharpe: {row['sharpeRatio']:6.2f})")

        print(f"\nLowest Volatility:")
        low_vol = summaries.nsmallest(10, "annualizedVolatility")[["ticker", "annualizedVolatility", "sharpeRatio"]]
        for idx, row in low_vol.iterrows():
            print(f"  {row['ticker']:8s} {row['annualizedVolatility']:6.2f}%  (Sharpe: {row['sharpeRatio']:6.2f})")

        print(f"\nBest Risk-Adjusted Returns (Sharpe Ratio):")
        best_sharpe = summaries.nlargest(10, "sharpeRatio")[["ticker", "sharpeRatio", "annualizedReturn", "annualizedVolatility"]]
        for idx, row in best_sharpe.iterrows():
            print(f"  {row['ticker']:8s} Sharpe: {row['sharpeRatio']:6.2f}  (Return: {row['annualizedReturn']:6.2f}%, Vol: {row['annualizedVolatility']:6.2f}%)")

    def get_db_connection(self):
        """
        Create a database connection using credentials from .env file.
        """
        try:
            connection = mysql.connector.connect(
                host=os.getenv('DB_HOST', 'localhost'),
                user=os.getenv('DB_USER', 'root'),
                password=os.getenv('DB_PASSWORD', ''),
                database=os.getenv('DB_NAME', 'portfolio_db')
            )
            if connection.is_connected():
                print(f"\n  Successfully connected to database: {os.getenv('DB_NAME')}")
                return connection
        except Error as e:
            print(f"\n  ERROR: Failed to connect to database: {e}")
            return None

    def push_to_database(self, data: Dict[str, pd.DataFrame], period: str):
        """
        Push extracted data to the database.

        Args:
            data: Dictionary with DataFrames (prices and summaries)
            period: The period string (e.g., "5y", "10y")
        """
        print(f"\n{'='*80}")
        print("PUSHING DATA TO DATABASE")
        print(f"{'='*80}")

        connection = self.get_db_connection()
        if not connection:
            print("  FAILED: Could not connect to database")
            return

        try:
            cursor = connection.cursor()

            # Push price history and technical indicators
            if "prices" in data and not data["prices"].empty:
                print(f"\n  Pushing price history and technical indicators...")
                self._push_price_history(cursor, data["prices"])

            # Push stock summaries
            if "summaries" in data and not data["summaries"].empty:
                print(f"\n  Pushing stock summaries...")
                self._push_stock_summaries(cursor, data["summaries"], period)

            connection.commit()
            print(f"\n  SUCCESS: All data pushed to database!")

        except Error as e:
            print(f"\n  ERROR: Failed to push data to database: {e}")
            connection.rollback()
        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()
                print(f"  Database connection closed")

    def _push_price_history(self, cursor, prices_df: pd.DataFrame):
        """
        Push price history and technical indicators to database.
        """
        tickers = prices_df['ticker'].unique()
        total_records = 0

        for ticker in tickers:
            # Get or create market_data entry
            cursor.execute("SELECT id FROM market_data WHERE symbol = %s", (ticker,))
            result = cursor.fetchone()

            if not result:
                print(f"    WARNING: Market data not found for {ticker}, skipping...")
                continue

            market_data_id = result[0]
            ticker_data = prices_df[prices_df['ticker'] == ticker]

            for _, row in ticker_data.iterrows():
                # Convert date to proper format
                price_date = pd.to_datetime(row['Date']).date()

                # Insert or update price_history
                price_history_sql = """
                    INSERT INTO price_history
                    (market_data_id, price_date, open_price, high_price, low_price, close_price, volume, data_source, created_date)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
                    ON DUPLICATE KEY UPDATE
                    open_price = VALUES(open_price),
                    high_price = VALUES(high_price),
                    low_price = VALUES(low_price),
                    close_price = VALUES(close_price),
                    volume = VALUES(volume)
                """

                cursor.execute(price_history_sql, (
                    market_data_id,
                    price_date,
                    self._safe_float(row.get('Open')),
                    self._safe_float(row.get('High')),
                    self._safe_float(row.get('Low')),
                    self._safe_float(row.get('Close')),
                    self._safe_int(row.get('Volume')),
                    'yfinance'
                ))

                # Get the price_history_id
                cursor.execute(
                    "SELECT id FROM price_history WHERE market_data_id = %s AND price_date = %s",
                    (market_data_id, price_date)
                )
                price_history_id = cursor.fetchone()[0]

                # Insert or update technical_indicators
                technical_indicators_sql = """
                    INSERT INTO technical_indicators
                    (price_history_id, indicator_date, daily_return, cumulative_return, log_return,
                     sma_20, sma_50, sma_200, ema_12, ema_26,
                     macd, macd_signal, macd_histogram,
                     bb_middle, bb_upper, bb_lower, bb_width,
                     rsi, atr, volume_sma_20, volume_ratio,
                     momentum_10, roc_10, stochastic_k, stochastic_d,
                     created_date, updated_date)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                    ON DUPLICATE KEY UPDATE
                    daily_return = VALUES(daily_return),
                    cumulative_return = VALUES(cumulative_return),
                    log_return = VALUES(log_return),
                    sma_20 = VALUES(sma_20),
                    sma_50 = VALUES(sma_50),
                    sma_200 = VALUES(sma_200),
                    ema_12 = VALUES(ema_12),
                    ema_26 = VALUES(ema_26),
                    macd = VALUES(macd),
                    macd_signal = VALUES(macd_signal),
                    macd_histogram = VALUES(macd_histogram),
                    bb_middle = VALUES(bb_middle),
                    bb_upper = VALUES(bb_upper),
                    bb_lower = VALUES(bb_lower),
                    bb_width = VALUES(bb_width),
                    rsi = VALUES(rsi),
                    atr = VALUES(atr),
                    volume_sma_20 = VALUES(volume_sma_20),
                    volume_ratio = VALUES(volume_ratio),
                    momentum_10 = VALUES(momentum_10),
                    roc_10 = VALUES(roc_10),
                    stochastic_k = VALUES(stochastic_k),
                    stochastic_d = VALUES(stochastic_d),
                    updated_date = NOW()
                """

                cursor.execute(technical_indicators_sql, (
                    price_history_id,
                    price_date,
                    self._safe_float(row.get('dailyReturn')),
                    self._safe_float(row.get('cumulativeReturn')),
                    self._safe_float(row.get('logReturn')),
                    self._safe_float(row.get('SMA_20')),
                    self._safe_float(row.get('SMA_50')),
                    self._safe_float(row.get('SMA_200')),
                    self._safe_float(row.get('EMA_12')),
                    self._safe_float(row.get('EMA_26')),
                    self._safe_float(row.get('MACD')),
                    self._safe_float(row.get('MACD_Signal')),
                    self._safe_float(row.get('MACD_Histogram')),
                    self._safe_float(row.get('BB_Middle')),
                    self._safe_float(row.get('BB_Upper')),
                    self._safe_float(row.get('BB_Lower')),
                    self._safe_float(row.get('BB_Width')),
                    self._safe_float(row.get('RSI')),
                    self._safe_float(row.get('ATR')),
                    self._safe_int(row.get('Volume_SMA_20')),
                    self._safe_float(row.get('Volume_Ratio')),
                    self._safe_float(row.get('Momentum_10')),
                    self._safe_float(row.get('ROC_10')),
                    self._safe_float(row.get('Stochastic_K')),
                    self._safe_float(row.get('Stochastic_D'))
                ))

                total_records += 1

            print(f"    {ticker}: {len(ticker_data)} records pushed")

        print(f"  Total price/indicator records pushed: {total_records}")

    def _push_stock_summaries(self, cursor, summaries_df: pd.DataFrame, period: str):
        """
        Push stock summaries to database.
        """
        total_summaries = 0

        for _, row in summaries_df.iterrows():
            ticker = row['ticker']

            # Get market_data_id
            cursor.execute("SELECT id FROM market_data WHERE symbol = %s", (ticker,))
            result = cursor.fetchone()

            if not result:
                print(f"    WARNING: Market data not found for {ticker}, skipping...")
                continue

            market_data_id = result[0]

            # Parse dates
            start_date = pd.to_datetime(row['startDate']).date()
            end_date = pd.to_datetime(row['endDate']).date()

            # Insert or update stock_summary
            stock_summary_sql = """
                INSERT INTO stock_summary
                (market_data_id, period, start_date, end_date, trading_days,
                 start_price, end_price, min_price, max_price, avg_price,
                 total_return, avg_daily_return, annualized_return,
                 daily_volatility, annualized_volatility, sharpe_ratio,
                 max_daily_gain, max_daily_loss,
                 avg_volume, max_volume, min_volume,
                 var_95, var_99, max_drawdown,
                 created_date, updated_date)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                trading_days = VALUES(trading_days),
                start_price = VALUES(start_price),
                end_price = VALUES(end_price),
                min_price = VALUES(min_price),
                max_price = VALUES(max_price),
                avg_price = VALUES(avg_price),
                total_return = VALUES(total_return),
                avg_daily_return = VALUES(avg_daily_return),
                annualized_return = VALUES(annualized_return),
                daily_volatility = VALUES(daily_volatility),
                annualized_volatility = VALUES(annualized_volatility),
                sharpe_ratio = VALUES(sharpe_ratio),
                max_daily_gain = VALUES(max_daily_gain),
                max_daily_loss = VALUES(max_daily_loss),
                avg_volume = VALUES(avg_volume),
                max_volume = VALUES(max_volume),
                min_volume = VALUES(min_volume),
                var_95 = VALUES(var_95),
                var_99 = VALUES(var_99),
                max_drawdown = VALUES(max_drawdown),
                updated_date = NOW()
            """

            cursor.execute(stock_summary_sql, (
                market_data_id,
                period,
                start_date,
                end_date,
                self._safe_int(row.get('tradingDays')),
                self._safe_float(row.get('startPrice')),
                self._safe_float(row.get('endPrice')),
                self._safe_float(row.get('minPrice')),
                self._safe_float(row.get('maxPrice')),
                self._safe_float(row.get('avgPrice')),
                self._safe_float(row.get('totalReturn')),
                self._safe_float(row.get('avgDailyReturn')),
                self._safe_float(row.get('annualizedReturn')),
                self._safe_float(row.get('dailyVolatility')),
                self._safe_float(row.get('annualizedVolatility')),
                self._safe_float(row.get('sharpeRatio')),
                self._safe_float(row.get('maxDailyGain')),
                self._safe_float(row.get('maxDailyLoss')),
                self._safe_int(row.get('avgVolume')),
                self._safe_int(row.get('maxVolume')),
                self._safe_int(row.get('minVolume')),
                self._safe_float(row.get('VaR_95')),
                self._safe_float(row.get('VaR_99')),
                self._safe_float(row.get('maxDrawdown'))
            ))

            total_summaries += 1

        print(f"  Total summaries pushed: {total_summaries}")

    def _safe_float(self, value):
        """Convert value to float, handling NaN and None."""
        if pd.isna(value) or value is None:
            return None
        try:
            return float(value)
        except (ValueError, TypeError):
            return None

    def _safe_int(self, value):
        """Convert value to int, handling NaN and None."""
        if pd.isna(value) or value is None:
            return None
        try:
            return int(value)
        except (ValueError, TypeError):
            return None


# =============================================================================
# MAIN EXECUTION
# =============================================================================

if __name__ == "__main__":
    # Initialize extractor
    extractor = HistoricalPriceExtractor(output_dir="historical_price_data")

    # Configuration
    PERIOD = "5y"  # Options: 1d, 5d, 1mo, 3mo, 6mo, 1y, 2y, 5y, 10y, ytd, max
    INTERVAL = "1d"  # Options: 1m, 2m, 5m, 15m, 30m, 60m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo
    INCLUDE_INDICATORS = True
    INCLUDE_BENCHMARKS = True

    # Use all companies from the list
    tickers = TOP_COMPANIES  # All 50 companies

    print(f"\nExtraction Configuration:")
    print(f"  Period: {PERIOD}")
    print(f"  Interval: {INTERVAL}")
    print(f"  Number of stocks: {len(tickers)}")
    print(f"  Stocks: {', '.join(tickers[:10])}..." if len(tickers) > 10 else f"  Stocks: {', '.join(tickers)}")

    # Extract data
    data = extractor.extract_all(
        tickers=tickers,
        period=PERIOD,
        interval=INTERVAL,
        include_indicators=INCLUDE_INDICATORS,
        include_benchmarks=INCLUDE_BENCHMARKS,
        delay=0.3
    )

    # Save to CSV
    extractor.save_to_csv(data, prefix=PERIOD)

    # Print quick statistics
    extractor.quick_stats(data)

    # Push to database
    extractor.push_to_database(data, PERIOD)

    print("\n" + "=" * 80)
    print("EXTRACTION COMPLETE!")
    print("=" * 80)
    print("\nFiles created:")
    print(f"  1. {PERIOD}_prices_[timestamp].csv")
    print(f"     - Complete historical price data with technical indicators")
    print(f"     - Columns: Date, ticker, OHLCV, Returns, SMA, EMA, MACD, RSI, Bollinger Bands, etc.")
    print(f"\n  2. {PERIOD}_summaries_[timestamp].csv")
    print(f"     - Summary statistics for each ticker")
    print(f"     - Columns: Returns, Volatility, Sharpe Ratio, VaR, Max Drawdown, etc.")
    print("\nData has been pushed to database!")
    print("\nUsage in code:")
    print("  from historical_price_extractor import HistoricalPriceExtractor")
    print("  extractor = HistoricalPriceExtractor()")
    print("  data = extractor.extract_all(period='10y')  # Get 10 years of data")
    print("  extractor.push_to_database(data, '10y')  # Push to database")
