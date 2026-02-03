"""
Context Manager module for the Financial Portfolio Chatbot Service.
Manages conversation context including mentioned symbols and time periods.
"""

import re
from typing import Dict, List, Optional, Set
from datetime import datetime

from config import CONTEXT_CONFIG


class ConversationContext:
    """Represents the context of a single conversation."""
    
    def __init__(self, session_id: str):
        """
        Initialize conversation context.
        
        Args:
            session_id: Unique identifier for the conversation
        """
        self.session_id = session_id
        self.mentioned_symbols: List[str] = []
        self.last_symbol: Optional[str] = None
        self.time_period: Optional[str] = None
        self.query_history: List[str] = []
        self.created_at = datetime.now()
        self.last_updated = datetime.now()
    
    def add_symbol(self, symbol: str):
        """Add a symbol to the context."""
        symbol = symbol.upper()
        if symbol not in self.mentioned_symbols:
            self.mentioned_symbols.append(symbol)
        self.last_symbol = symbol
        self.last_updated = datetime.now()
    
    def add_query(self, query: str):
        """Add a query to the history."""
        self.query_history.append(query)
        # Keep only last N queries
        max_history = CONTEXT_CONFIG.get('max_history_messages', 10)
        if len(self.query_history) > max_history:
            self.query_history = self.query_history[-max_history:]
        self.last_updated = datetime.now()
    
    def to_dict(self) -> Dict:
        """Convert context to dictionary."""
        return {
            'session_id': self.session_id,
            'mentioned_symbols': self.mentioned_symbols,
            'last_symbol': self.last_symbol,
            'time_period': self.time_period,
            'created_at': self.created_at.isoformat(),
            'last_updated': self.last_updated.isoformat()
        }


class ContextManager:
    """Manages conversation contexts for multiple sessions."""
    
    # Known stock symbols (common ones for quick matching)
    COMMON_SYMBOLS = {
        'AAPL', 'MSFT', 'GOOGL', 'AMZN', 'NVDA', 'META', 'TSLA', 'BRK', 'UNH', 'JNJ',
        'V', 'XOM', 'JPM', 'WMT', 'MA', 'PG', 'HD', 'CVX', 'MRK', 'ABBV',
        'LLY', 'PEP', 'KO', 'COST', 'AVGO', 'PFE', 'TMO', 'MCD', 'CSCO', 'ACN',
        'CRM', 'ABT', 'DHR', 'NKE', 'ORCL', 'TXN', 'NFLX', 'AMD', 'INTC', 'DIS',
        'BAC', 'ADBE', 'WFC', 'COP', 'RTX', 'NEE', 'PM', 'UPS', 'QCOM', 'VZ'
    }
    
    # Company name to symbol mapping
    COMPANY_TO_SYMBOL = {
        'apple': 'AAPL',
        'microsoft': 'MSFT',
        'google': 'GOOGL',
        'alphabet': 'GOOGL',
        'amazon': 'AMZN',
        'nvidia': 'NVDA',
        'meta': 'META',
        'facebook': 'META',
        'tesla': 'TSLA',
        'berkshire': 'BRK-B',
        'unitedhealth': 'UNH',
        'johnson': 'JNJ',
        'johnson & johnson': 'JNJ',
        'visa': 'V',
        'exxon': 'XOM',
        'jpmorgan': 'JPM',
        'jp morgan': 'JPM',
        'walmart': 'WMT',
        'mastercard': 'MA',
        'procter': 'PG',
        'procter & gamble': 'PG',
        'home depot': 'HD',
        'chevron': 'CVX',
        'merck': 'MRK',
        'abbvie': 'ABBV',
        'eli lilly': 'LLY',
        'lilly': 'LLY',
        'pepsi': 'PEP',
        'pepsico': 'PEP',
        'coca cola': 'KO',
        'coca-cola': 'KO',
        'coke': 'KO',
        'costco': 'COST',
        'broadcom': 'AVGO',
        'pfizer': 'PFE',
        'thermo fisher': 'TMO',
        'mcdonalds': 'MCD',
        "mcdonald's": 'MCD',
        'cisco': 'CSCO',
        'accenture': 'ACN',
        'salesforce': 'CRM',
        'abbott': 'ABT',
        'danaher': 'DHR',
        'nike': 'NKE',
        'oracle': 'ORCL',
        'texas instruments': 'TXN',
        'netflix': 'NFLX',
        'amd': 'AMD',
        'intel': 'INTC',
        'disney': 'DIS',
        'walt disney': 'DIS',
        'bank of america': 'BAC',
        'adobe': 'ADBE',
        'wells fargo': 'WFC',
        'conocophillips': 'COP',
        'raytheon': 'RTX',
        'nextera': 'NEE',
        'nextera energy': 'NEE',
        'philip morris': 'PM',
        'ups': 'UPS',
        'qualcomm': 'QCOM',
        'verizon': 'VZ'
    }
    
    # Time period keywords
    TIME_PERIODS = {
        'today': '1d',
        'yesterday': '1d',
        'this week': '1w',
        'last week': '1w',
        'week': '1w',
        'this month': '1m',
        'last month': '1m',
        'month': '1m',
        'this quarter': '3m',
        'quarter': '3m',
        'this year': '1y',
        'last year': '1y',
        'year': '1y',
        'ytd': '1y',
        'all time': 'all'
    }
    
    def __init__(self):
        """Initialize the context manager."""
        self.contexts: Dict[str, ConversationContext] = {}
        self.symbol_pattern = re.compile(CONTEXT_CONFIG.get('symbol_pattern', r'\b[A-Z]{1,5}\b'))
    
    def get_context(self, session_id: str) -> ConversationContext:
        """
        Get or create a conversation context.
        
        Args:
            session_id: Unique identifier for the conversation
            
        Returns:
            ConversationContext for the session
        """
        if session_id not in self.contexts:
            self.contexts[session_id] = ConversationContext(session_id)
        return self.contexts[session_id]
    
    def extract_symbols(self, text: str) -> List[str]:
        """
        Extract stock symbols from text.
        
        Args:
            text: Text to extract symbols from
            
        Returns:
            List of stock symbols found
        """
        symbols = []
        text_lower = text.lower()
        
        # First, check for company names
        for company_name, symbol in self.COMPANY_TO_SYMBOL.items():
            if company_name in text_lower:
                if symbol not in symbols:
                    symbols.append(symbol)
        
        # Then find all potential symbols (uppercase 1-5 letter words)
        potential_symbols = self.symbol_pattern.findall(text)
        
        # Filter to only known symbols or likely stock symbols
        for symbol in potential_symbols:
            symbol = symbol.upper()
            if symbol in symbols:
                continue
            # Accept if it's a known symbol
            if symbol in self.COMMON_SYMBOLS:
                symbols.append(symbol)
            # Accept if it looks like a stock symbol (2-5 chars, not common word)
            elif len(symbol) >= 2 and symbol not in self._get_common_words():
                symbols.append(symbol)
        
        return list(dict.fromkeys(symbols))  # Remove duplicates, preserve order
    
    def extract_time_period(self, text: str) -> Optional[str]:
        """
        Extract time period from text.
        
        Args:
            text: Text to extract time period from
            
        Returns:
            Time period code or None
        """
        text_lower = text.lower()
        
        for keyword, period in self.TIME_PERIODS.items():
            if keyword in text_lower:
                return period
        
        return None
    
    def update_context(self, session_id: str, query: str) -> ConversationContext:
        """
        Update conversation context with a new query.
        
        Args:
            session_id: Session identifier
            query: The user's query
            
        Returns:
            Updated ConversationContext
        """
        context = self.get_context(session_id)
        
        # Add query to history
        context.add_query(query)
        
        # Extract and add symbols
        symbols = self.extract_symbols(query)
        for symbol in symbols:
            context.add_symbol(symbol)
        
        # Extract time period
        time_period = self.extract_time_period(query)
        if time_period:
            context.time_period = time_period
        
        return context
    
    def get_relevant_symbol(self, session_id: str, query: str) -> Optional[str]:
        """
        Get the most relevant symbol for a query, considering context.
        
        Args:
            session_id: Session identifier
            query: The user's query
            
        Returns:
            Most relevant stock symbol or None
        """
        # First, check if query mentions a specific symbol
        symbols = self.extract_symbols(query)
        if symbols:
            return symbols[0]
        
        # If no symbol in query, check for implicit references
        query_lower = query.lower()
        implicit_refs = ['it', 'its', 'the stock', 'this stock', 'that stock', 'the company']
        
        for ref in implicit_refs:
            if ref in query_lower:
                context = self.get_context(session_id)
                if context.last_symbol:
                    return context.last_symbol
        
        return None
    
    def get_context_string(self, session_id: str) -> str:
        """
        Get a string representation of the current context for LLM prompts.
        
        Args:
            session_id: Session identifier
            
        Returns:
            Context string for prompts
        """
        context = self.get_context(session_id)
        
        parts = []
        
        if context.mentioned_symbols:
            parts.append(f"Previously mentioned stocks: {', '.join(context.mentioned_symbols)}")
        
        if context.last_symbol:
            parts.append(f"Most recently discussed: {context.last_symbol}")
        
        if context.time_period:
            parts.append(f"Time period context: {context.time_period}")
        
        return '; '.join(parts) if parts else "No previous context"
    
    def clear_context(self, session_id: str):
        """Clear context for a session."""
        if session_id in self.contexts:
            del self.contexts[session_id]
    
    def _get_common_words(self) -> Set[str]:
        """Get a set of common words to filter out from symbol detection."""
        return {
            'A', 'I', 'AN', 'AS', 'AT', 'BE', 'BY', 'DO', 'GO', 'HE', 'IF',
            'IN', 'IS', 'IT', 'ME', 'MY', 'NO', 'OF', 'ON', 'OR', 'SO', 'TO',
            'UP', 'US', 'WE', 'THE', 'AND', 'FOR', 'ARE', 'BUT', 'NOT', 'YOU',
            'ALL', 'CAN', 'HER', 'WAS', 'ONE', 'OUR', 'OUT', 'DAY', 'HAD',
            'HAS', 'HIS', 'HOW', 'ITS', 'MAY', 'NEW', 'NOW', 'OLD', 'SEE',
            'WAY', 'WHO', 'DID', 'GET', 'HIM', 'LET', 'PUT', 'SAY', 'SHE',
            'TOO', 'USE', 'RSI', 'PE', 'EPS', 'CEO', 'CFO', 'IPO', 'ETF', 'NYSE',
            'TOP', 'LOW', 'HIGH', 'BUY', 'SELL', 'HOLD', 'LONG', 'SHORT'
        }


# Singleton instance
_manager = None

def get_context_manager() -> ContextManager:
    """Get or create the ContextManager singleton instance."""
    global _manager
    if _manager is None:
        _manager = ContextManager()
    return _manager


if __name__ == "__main__":
    # Test the context manager
    manager = ContextManager()
    
    print("Context Manager Tests")
    print("=" * 60)
    
    session_id = "test_session_123"
    
    # Test symbol extraction
    test_texts = [
        "What's AAPL's current price?",
        "Compare MSFT and GOOGL",
        "How did it perform this week?",  # Should use context
        "Show me TSLA news from last month",
    ]
    
    for text in test_texts:
        print(f"\nQuery: {text}")
        
        # Extract symbols
        symbols = manager.extract_symbols(text)
        print(f"  Symbols found: {symbols}")
        
        # Extract time period
        period = manager.extract_time_period(text)
        print(f"  Time period: {period}")
        
        # Update context
        context = manager.update_context(session_id, text)
        print(f"  Last symbol: {context.last_symbol}")
        print(f"  Context: {manager.get_context_string(session_id)}")
