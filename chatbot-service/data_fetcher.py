"""
Data Fetcher module for the Financial Portfolio Chatbot Service.
Handles database connections and SQL query execution.
"""

import mysql.connector
from mysql.connector import Error
from decimal import Decimal
from datetime import datetime, date
import json
from typing import List, Dict, Any, Optional, Tuple

from config import DB_CONFIG


class DataFetcher:
    """Handles database operations for the chatbot service."""
    
    def __init__(self):
        """Initialize the data fetcher."""
        self.connection = None
        self.cursor = None
    
    def connect(self) -> bool:
        """
        Establish a database connection.
        
        Returns:
            bool: True if connection successful, False otherwise
        """
        try:
            self.connection = mysql.connector.connect(**DB_CONFIG)
            self.cursor = self.connection.cursor(dictionary=True)
            return True
        except Error as e:
            print(f"Database connection error: {e}")
            return False
    
    def disconnect(self):
        """Close the database connection."""
        try:
            if self.cursor:
                self.cursor.close()
            if self.connection and self.connection.is_connected():
                self.connection.close()
        except Error as e:
            print(f"Error closing connection: {e}")
    
    def _convert_to_serializable(self, value: Any) -> Any:
        """
        Convert a value to JSON-serializable format.
        
        Args:
            value: Any value that might need conversion
            
        Returns:
            JSON-serializable value
        """
        if isinstance(value, Decimal):
            return float(value)
        elif isinstance(value, (datetime, date)):
            return value.isoformat()
        elif isinstance(value, bytes):
            return value.decode('utf-8')
        elif value is None:
            return None
        else:
            return value
    
    def _convert_row(self, row: Dict) -> Dict:
        """
        Convert a database row to JSON-serializable format.
        
        Args:
            row: Dictionary representing a database row
            
        Returns:
            Dictionary with all values converted to JSON-serializable types
        """
        return {key: self._convert_to_serializable(value) for key, value in row.items()}
    
    def execute_query(self, sql: str) -> Tuple[List[Dict], Optional[str]]:
        """
        Execute a SQL query and return the results.
        
        Args:
            sql: The SQL query to execute
            
        Returns:
            Tuple of (results list, error message or None)
        """
        try:
            if not self.connection or not self.connection.is_connected():
                if not self.connect():
                    return [], "Failed to connect to database"
            
            # Execute the query
            self.cursor.execute(sql)
            
            # Fetch results
            results = self.cursor.fetchall()
            
            # Convert to JSON-serializable format
            converted_results = [self._convert_row(row) for row in results]
            
            return converted_results, None
            
        except Error as e:
            error_msg = f"SQL execution error: {str(e)}"
            print(error_msg)
            return [], error_msg
        finally:
            self.disconnect()
    
    def get_stock_info(self, symbol: str) -> Tuple[Dict, Optional[str]]:
        """
        Get basic stock information for a symbol.
        
        Args:
            symbol: Stock ticker symbol
            
        Returns:
            Tuple of (stock info dict, error message or None)
        """
        sql = f"""
            SELECT md.symbol, md.name, md.sector, md.industry, md.current_price,
                   md.day_change, md.day_change_percent, md.market_cap,
                   md.pe_ratio, md.eps, md.dividend_yield, md.beta,
                   md.week_52_high, md.week_52_low, md.volume, md.last_updated
            FROM market_data md
            WHERE md.symbol = '{symbol.upper()}'
            LIMIT 1
        """
        results, error = self.execute_query(sql)
        if error:
            return {}, error
        if not results:
            return {}, f"No data found for symbol {symbol}"
        return results[0], None
    
    def get_technical_indicators(self, symbol: str) -> Tuple[Dict, Optional[str]]:
        """
        Get technical indicators for a symbol.
        
        Args:
            symbol: Stock ticker symbol
            
        Returns:
            Tuple of (indicators dict, error message or None)
        """
        sql = f"""
            SELECT md.symbol, ti.indicator_date, ti.rsi, ti.macd, ti.macd_signal, ti.macd_histogram,
                   ti.sma_20, ti.sma_50, ti.sma_200, ti.ema_12, ti.ema_26,
                   ti.bb_upper, ti.bb_middle, ti.bb_lower, ti.bb_width, ti.atr,
                   ti.stochastic_k, ti.stochastic_d, ti.momentum_10, ti.roc_10,
                   ti.daily_return, ti.volume_ratio
            FROM market_data md
            JOIN price_history ph ON ph.market_data_id = md.id
            JOIN technical_indicators ti ON ti.price_history_id = ph.id
            WHERE md.symbol = '{symbol.upper()}'
            ORDER BY ti.indicator_date DESC
            LIMIT 1
        """
        results, error = self.execute_query(sql)
        if error:
            return {}, error
        if not results:
            return {}, f"No technical indicators found for {symbol}"
        return results[0], None
    
    def get_recent_news(self, symbol: str, limit: int = 5) -> Tuple[List[Dict], Optional[str]]:
        """
        Get recent news articles for a symbol.
        
        Args:
            symbol: Stock ticker symbol
            limit: Maximum number of articles to return
            
        Returns:
            Tuple of (news list, error message or None)
        """
        sql = f"""
            SELECT symbol, title, summary, publisher, published_date, sentiment, link
            FROM news
            WHERE symbol = '{symbol.upper()}'
            ORDER BY published_date DESC
            LIMIT {limit}
        """
        return self.execute_query(sql)
    
    def get_stock_summary(self, symbol: str, period: str = '1y') -> Tuple[Dict, Optional[str]]:
        """
        Get stock performance summary.
        
        Args:
            symbol: Stock ticker symbol
            period: Time period ('1w', '1m', '3m', '1y')
            
        Returns:
            Tuple of (summary dict, error message or None)
        """
        sql = f"""
            SELECT md.symbol, md.name, ss.period, ss.start_date, ss.end_date,
                   ss.start_price, ss.end_price, ss.total_return, ss.annualized_return,
                   ss.avg_daily_return, ss.daily_volatility, ss.annualized_volatility,
                   ss.sharpe_ratio, ss.max_drawdown, ss.var_95, ss.var_99,
                   ss.max_daily_gain, ss.max_daily_loss, ss.max_price, ss.min_price,
                   ss.avg_price, ss.trading_days
            FROM market_data md
            JOIN stock_summary ss ON ss.market_data_id = md.id
            WHERE md.symbol = '{symbol.upper()}'
            LIMIT 1
        """
        results, error = self.execute_query(sql)
        if error:
            return {}, error
        if not results:
            return {}, f"No summary data found for {symbol} ({period})"
        return results[0], None
    
    def get_esg_rating(self, symbol: str) -> Tuple[Dict, Optional[str]]:
        """
        Get ESG rating for a symbol.
        
        Args:
            symbol: Stock ticker symbol
            
        Returns:
            Tuple of (ESG rating dict, error message or None)
        """
        sql = f"""
            SELECT md.symbol, md.name, er.total_score, er.total_grade,
                   er.environment_score, er.environment_grade,
                   er.social_score, er.social_grade,
                   er.governance_score, er.governance_grade,
                   er.controversy_level, er.risk_level, er.data_source, er.last_updated
            FROM market_data md
            JOIN esg_ratings er ON er.market_data_id = md.id
            WHERE md.symbol = '{symbol.upper()}'
            ORDER BY er.last_updated DESC
            LIMIT 1
        """
        results, error = self.execute_query(sql)
        if error:
            return {}, error
        if not results:
            return {}, f"No ESG rating found for {symbol}"
        return results[0], None
    
    def get_price_history(self, symbol: str, days: int = 30) -> Tuple[List[Dict], Optional[str]]:
        """
        Get price history for a symbol.
        
        Args:
            symbol: Stock ticker symbol
            days: Number of days of history
            
        Returns:
            Tuple of (price history list, error message or None)
        """
        sql = f"""
            SELECT md.symbol, ph.price_date, ph.open_price, ph.high_price,
                   ph.low_price, ph.close_price, ph.adj_close, ph.volume
            FROM market_data md
            JOIN price_history ph ON ph.market_data_id = md.id
            WHERE md.symbol = '{symbol.upper()}'
                AND ph.price_date >= DATE_SUB(CURDATE(), INTERVAL {days} DAY)
            ORDER BY ph.price_date DESC
            LIMIT 100
        """
        return self.execute_query(sql)
    
    def get_all_symbols(self) -> Tuple[List[str], Optional[str]]:
        """
        Get all available stock symbols.
        
        Returns:
            Tuple of (list of symbols, error message or None)
        """
        sql = "SELECT DISTINCT symbol FROM market_data ORDER BY symbol"
        results, error = self.execute_query(sql)
        if error:
            return [], error
        return [r['symbol'] for r in results], None


# Singleton instance
_data_fetcher = None

def get_data_fetcher() -> DataFetcher:
    """Get or create the DataFetcher singleton instance."""
    global _data_fetcher
    if _data_fetcher is None:
        _data_fetcher = DataFetcher()
    return _data_fetcher


if __name__ == "__main__":
    # Test the data fetcher
    fetcher = DataFetcher()
    
    print("Testing DataFetcher...")
    print("-" * 50)
    
    # Test stock info
    info, err = fetcher.get_stock_info("AAPL")
    if err:
        print(f"Error: {err}")
    else:
        print(f"AAPL Info: {json.dumps(info, indent=2)}")
    
    print("-" * 50)
    
    # Test getting all symbols
    symbols, err = fetcher.get_all_symbols()
    if err:
        print(f"Error: {err}")
    else:
        print(f"Available symbols: {symbols[:10]}...")
