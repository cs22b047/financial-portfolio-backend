"""
Explanation Generator module for the Financial Portfolio Chatbot Service.
Uses Groq LLM to generate insightful explanations for financial queries.
"""

import json
from groq import Groq
from typing import Dict, List, Optional, Tuple

from config import GROQ_CONFIG
from prompts import get_explanation_prompt
from data_fetcher import DataFetcher


class ExplanationGenerator:
    """Generates explanations and analysis for financial queries."""
    
    def __init__(self):
        """Initialize the explanation generator."""
        self.client = None
        if GROQ_CONFIG['api_key']:
            self.client = Groq(api_key=GROQ_CONFIG['api_key'])
        self.data_fetcher = DataFetcher()
    
    def generate(self, query: str, symbols: List[str] = None) -> Tuple[str, Dict, Optional[str]]:
        """
        Generate an explanation for a user query.
        
        Args:
            query: The user's natural language query
            symbols: List of stock symbols mentioned in the query
            
        Returns:
            Tuple of (explanation text, supporting data dict, error message or None)
        """
        if not self.client:
            return "", {}, "LLM client not initialized"
        
        if not symbols:
            symbols = []
        
        try:
            # Gather relevant data
            data = self._gather_data(symbols, query)
            
            # Format data for the prompt
            data_str = self._format_data_for_prompt(data)
            
            # Generate explanation
            prompt = get_explanation_prompt(query, data_str)
            
            response = self.client.chat.completions.create(
                model=GROQ_CONFIG['model'],
                messages=[
                    {
                        "role": "system",
                        "content": "You are a financial analyst providing insightful market analysis. Be objective, data-driven, and avoid speculation. Format your response in clear paragraphs."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=GROQ_CONFIG['temperature'],
                max_tokens=GROQ_CONFIG['max_tokens'],
                top_p=0.9
            )
            
            explanation = response.choices[0].message.content.strip()
            
            return explanation, data, None
            
        except Exception as e:
            error_msg = f"Explanation generation error: {str(e)}"
            print(error_msg)
            return "", {}, error_msg
    
    def _gather_data(self, symbols: List[str], query: str) -> Dict:
        """
        Gather relevant data for generating an explanation.
        
        Args:
            symbols: List of stock symbols
            query: The user's query (used to determine what data to fetch)
            
        Returns:
            Dictionary containing gathered data
        """
        data = {
            'stocks': {},
            'news': [],
            'indicators': {},
            'summary': {}
        }
        
        for symbol in symbols[:3]:  # Limit to 3 symbols
            symbol = symbol.upper()
            
            # Get stock info
            stock_info, _ = self.data_fetcher.get_stock_info(symbol)
            if stock_info:
                data['stocks'][symbol] = stock_info
            
            # Get technical indicators
            indicators, _ = self.data_fetcher.get_technical_indicators(symbol)
            if indicators:
                data['indicators'][symbol] = indicators
            
            # Get recent news
            news, _ = self.data_fetcher.get_recent_news(symbol, limit=3)
            if news:
                data['news'].extend(news)
            
            # Get performance summary
            summary, _ = self.data_fetcher.get_stock_summary(symbol)
            if summary:
                data['summary'][symbol] = summary
        
        return data
    
    def _format_data_for_prompt(self, data: Dict) -> str:
        """
        Format gathered data into a string for the LLM prompt.
        
        Args:
            data: Dictionary containing gathered data
            
        Returns:
            Formatted string representation of the data
        """
        parts = []
        
        # Stock information
        if data.get('stocks'):
            parts.append("CURRENT STOCK DATA:")
            for symbol, info in data['stocks'].items():
                parts.append(f"\n{symbol}:")
                parts.append(f"  Price: ${info.get('current_price', 'N/A')}")
                parts.append(f"  Change: {info.get('day_change_percent', 'N/A')}%")
                parts.append(f"  Volume: {info.get('volume', 'N/A')}")
                parts.append(f"  P/E Ratio: {info.get('pe_ratio', 'N/A')}")
                parts.append(f"  Market Cap: ${info.get('market_cap', 'N/A')}")
        
        # Technical indicators
        if data.get('indicators'):
            parts.append("\nTECHNICAL INDICATORS:")
            for symbol, indicators in data['indicators'].items():
                parts.append(f"\n{symbol}:")
                parts.append(f"  RSI: {indicators.get('rsi', 'N/A')}")
                parts.append(f"  MACD: {indicators.get('macd', 'N/A')}")
                parts.append(f"  SMA 20: {indicators.get('sma_20', 'N/A')}")
                parts.append(f"  SMA 50: {indicators.get('sma_50', 'N/A')}")
        
        # Performance summary
        if data.get('summary'):
            parts.append("\nPERFORMANCE SUMMARY:")
            for symbol, summary in data['summary'].items():
                parts.append(f"\n{symbol} ({summary.get('period', '1y')}):")
                parts.append(f"  Total Return: {summary.get('total_return', 'N/A')}%")
                parts.append(f"  Volatility: {summary.get('annualized_volatility', 'N/A')}%")
                parts.append(f"  Sharpe Ratio: {summary.get('sharpe_ratio', 'N/A')}")
                parts.append(f"  Max Drawdown: {summary.get('max_drawdown', 'N/A')}%")
        
        # Recent news
        if data.get('news'):
            parts.append("\nRECENT NEWS:")
            for article in data['news'][:5]:
                parts.append(f"  - {article.get('title', 'N/A')}")
                if article.get('sentiment'):
                    parts.append(f"    Sentiment: {article.get('sentiment')}")
        
        return '\n'.join(parts) if parts else "No relevant data available"
    
    def generate_quick_insight(self, symbol: str) -> Tuple[str, Optional[str]]:
        """
        Generate a quick insight for a single stock.
        
        Args:
            symbol: Stock ticker symbol
            
        Returns:
            Tuple of (insight text, error message or None)
        """
        explanation, _, error = self.generate(
            f"Provide a brief analysis of {symbol}'s current position",
            [symbol]
        )
        return explanation, error


# Singleton instance
_generator = None

def get_explanation_generator() -> ExplanationGenerator:
    """Get or create the ExplanationGenerator singleton instance."""
    global _generator
    if _generator is None:
        _generator = ExplanationGenerator()
    return _generator


def generate_explanation(query: str, symbols: List[str] = None) -> Tuple[str, Dict, Optional[str]]:
    """
    Convenience function to generate an explanation.
    
    Args:
        query: The user's natural language query
        symbols: List of stock symbols mentioned in the query
        
    Returns:
        Tuple of (explanation text, supporting data dict, error message or None)
    """
    return get_explanation_generator().generate(query, symbols)


if __name__ == "__main__":
    # Test the explanation generator
    generator = ExplanationGenerator()
    
    print("Explanation Generation Test")
    print("=" * 60)
    
    test_query = "Why did AAPL's stock change recently?"
    print(f"\nQuery: {test_query}")
    print("-" * 50)
    
    explanation, data, error = generator.generate(test_query, ["AAPL"])
    
    if error:
        print(f"Error: {error}")
    else:
        print(f"\nExplanation:\n{explanation}")
        print(f"\nData gathered for: {list(data.get('stocks', {}).keys())}")
