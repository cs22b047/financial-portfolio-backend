"""
Main Chatbot Service module for the Financial Portfolio Chatbot.
Orchestrates query processing, classification, SQL generation, and response generation.
"""

import json
import time
from typing import Dict, List, Optional, Tuple
from groq import Groq

from config import GROQ_CONFIG
from prompts import get_response_prompt, get_general_prompt, get_error_response
from query_classifier import classify_query
from sql_generator import generate_sql
from explanation_generator import generate_explanation
from context_manager import get_context_manager
from data_fetcher import DataFetcher


class ChatbotService:
    """Main chatbot service that orchestrates all components."""
    
    def __init__(self):
        """Initialize the chatbot service."""
        self.client = None
        if GROQ_CONFIG['api_key']:
            self.client = Groq(api_key=GROQ_CONFIG['api_key'])
        
        self.context_manager = get_context_manager()
        self.data_fetcher = DataFetcher()
    
    def process_message(
        self, 
        session_id: str, 
        message: str, 
        history: List[Dict] = None
    ) -> Dict:
        """
        Process a user message and generate a response.
        
        Args:
            session_id: Unique session identifier
            message: The user's message
            history: Previous conversation history
            
        Returns:
            Response dictionary with result and metadata
        """
        start_time = time.time()
        
        # Initialize response
        response = {
            'response': '',
            'query_type': 'ERROR',
            'sql_query': None,
            'data': None,
            'tables_used': [],
            'tokens_used': 0,
            'processing_time_ms': 0,
            'error': None
        }
        
        try:
            # Update context with new message
            context = self.context_manager.update_context(session_id, message)
            context_str = self.context_manager.get_context_string(session_id)
            
            # Classify the query
            query_type = classify_query(message)
            response['query_type'] = query_type
            
            # Process based on query type
            if query_type == 'DATA':
                result = self._process_data_query(message, context_str, context)
            elif query_type == 'EXPLANATION':
                result = self._process_explanation_query(message, context)
            else:  # GENERAL
                result = self._process_general_query(message)
            
            # Update response with result
            response.update(result)
            
        except Exception as e:
            response['error'] = str(e)
            response['response'] = get_error_response('general_error')
        
        # Calculate processing time
        response['processing_time_ms'] = int((time.time() - start_time) * 1000)
        
        return response
    
    def _process_data_query(
        self, 
        message: str, 
        context_str: str,
        context
    ) -> Dict:
        """
        Process a DATA type query.
        
        Args:
            message: User's message
            context_str: Context string for SQL generation
            context: ConversationContext object
            
        Returns:
            Result dictionary
        """
        result = {
            'response': '',
            'sql_query': None,
            'data': None,
            'tables_used': [],
            'tokens_used': 0
        }
        
        # Generate SQL
        sql, tables, error = generate_sql(message, context_str)
        
        if error:
            result['response'] = get_error_response('sql_error')
            result['error'] = error
            return result
        
        result['sql_query'] = sql
        result['tables_used'] = tables
        
        # Execute SQL
        data, error = self.data_fetcher.execute_query(sql)
        
        if error:
            result['response'] = get_error_response('sql_error')
            result['error'] = error
            return result
        
        if not data:
            # Try to identify what wasn't found
            symbols = self.context_manager.extract_symbols(message)
            if symbols:
                result['response'] = get_error_response('no_data', topic=symbols[0])
            else:
                result['response'] = get_error_response('no_data', topic='the requested data')
            return result
        
        result['data'] = data
        
        # Generate natural language response
        response_text = self._generate_data_response(message, data)
        result['response'] = response_text
        
        return result
    
    def _process_explanation_query(self, message: str, context) -> Dict:
        """
        Process an EXPLANATION type query.
        
        Args:
            message: User's message
            context: ConversationContext object
            
        Returns:
            Result dictionary
        """
        result = {
            'response': '',
            'sql_query': None,
            'data': None,
            'tables_used': [],
            'tokens_used': 0
        }
        
        # Extract symbols from message or context
        symbols = self.context_manager.extract_symbols(message)
        if not symbols and context.last_symbol:
            symbols = [context.last_symbol]
        
        # Generate explanation
        explanation, data, error = generate_explanation(message, symbols)
        
        if error:
            result['response'] = get_error_response('llm_error')
            result['error'] = error
            return result
        
        result['response'] = explanation
        result['data'] = data
        
        # Extract tables used from gathered data
        if data:
            tables = set()
            if data.get('stocks'):
                tables.add('market_data')
            if data.get('indicators'):
                tables.update(['price_history', 'technical_indicators'])
            if data.get('news'):
                tables.add('news')
            if data.get('summary'):
                tables.add('stock_summary')
            result['tables_used'] = list(tables)
        
        return result
    
    def _process_general_query(self, message: str) -> Dict:
        """
        Process a GENERAL type query.
        
        Args:
            message: User's message
            
        Returns:
            Result dictionary
        """
        result = {
            'response': '',
            'sql_query': None,
            'data': None,
            'tables_used': [],
            'tokens_used': 0
        }
        
        if not self.client:
            result['response'] = "Hello! I'm your financial portfolio assistant. I can help you with stock prices, technical analysis, ESG ratings, news, and portfolio tracking. What would you like to know?"
            return result
        
        try:
            prompt = get_general_prompt(message)
            
            response = self.client.chat.completions.create(
                model=GROQ_CONFIG['model'],
                messages=[
                    {
                        "role": "system",
                        "content": "You are a helpful financial portfolio assistant. Be friendly, concise, and informative."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=GROQ_CONFIG['temperature'],
                max_tokens=300,
                top_p=0.9
            )
            
            result['response'] = response.choices[0].message.content.strip()
            result['tokens_used'] = response.usage.total_tokens if response.usage else 0
            
        except Exception as e:
            result['response'] = "Hello! I'm your financial portfolio assistant. I can help you with stock prices, technical analysis, ESG ratings, news, and portfolio tracking. What would you like to know?"
        
        return result
    
    def _generate_data_response(self, query: str, data: List[Dict]) -> str:
        """
        Generate a natural language response for data query results.
        
        Args:
            query: Original user query
            data: Query results
            
        Returns:
            Natural language response
        """
        if not self.client:
            return self._format_data_as_text(data)
        
        try:
            # Format data for the prompt
            data_str = json.dumps(data[:10], indent=2)  # Limit to 10 rows for prompt
            
            prompt = get_response_prompt(query, data_str)
            
            response = self.client.chat.completions.create(
                model=GROQ_CONFIG['model'],
                messages=[
                    {
                        "role": "system",
                        "content": "You are a financial assistant. Generate a clear, concise response based on the data. Use proper formatting for numbers (commas, $, %)."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.5,
                max_tokens=500,
                top_p=0.9
            )
            
            return response.choices[0].message.content.strip()
            
        except Exception as e:
            print(f"Response generation error: {e}")
            return self._format_data_as_text(data)
    
    def _format_data_as_text(self, data: List[Dict]) -> str:
        """
        Format data as plain text (fallback when LLM is unavailable).
        
        Args:
            data: Query results
            
        Returns:
            Formatted text
        """
        if not data:
            return "No data found."
        
        if len(data) == 1:
            # Single result - format as key-value pairs
            row = data[0]
            parts = []
            for key, value in row.items():
                if value is not None:
                    # Format numbers
                    if isinstance(value, float):
                        if 'price' in key.lower() or 'cap' in key.lower():
                            parts.append(f"{key}: ${value:,.2f}")
                        elif 'percent' in key.lower() or 'return' in key.lower():
                            parts.append(f"{key}: {value:.2f}%")
                        else:
                            parts.append(f"{key}: {value:.2f}")
                    else:
                        parts.append(f"{key}: {value}")
            return '\n'.join(parts)
        else:
            # Multiple results - format as summary
            return f"Found {len(data)} results. Here are the key details:\n" + \
                   '\n'.join([f"- {row.get('symbol', row.get('title', 'Item'))}: {list(row.values())[1] if len(row) > 1 else 'N/A'}" 
                             for row in data[:5]])
    
    def health_check(self) -> Dict:
        """
        Check the health of the chatbot service.
        
        Returns:
            Health status dictionary
        """
        status = {
            'status': 'healthy',
            'components': {
                'llm': 'ok' if self.client else 'not configured',
                'database': 'unknown'
            }
        }
        
        # Check database connection
        try:
            if self.data_fetcher.connect():
                status['components']['database'] = 'ok'
                self.data_fetcher.disconnect()
            else:
                status['components']['database'] = 'error'
                status['status'] = 'degraded'
        except Exception as e:
            status['components']['database'] = f'error: {str(e)}'
            status['status'] = 'degraded'
        
        return status


# Singleton instance
_service = None

def get_chatbot_service() -> ChatbotService:
    """Get or create the ChatbotService singleton instance."""
    global _service
    if _service is None:
        _service = ChatbotService()
    return _service


def process_chat_message(session_id: str, message: str, history: List[Dict] = None) -> Dict:
    """
    Convenience function to process a chat message.
    
    Args:
        session_id: Session identifier
        message: User's message
        history: Conversation history
        
    Returns:
        Response dictionary
    """
    return get_chatbot_service().process_message(session_id, message, history)


if __name__ == "__main__":
    # Test the chatbot service
    service = ChatbotService()
    
    print("Chatbot Service Test")
    print("=" * 60)
    
    # Health check
    health = service.health_check()
    print(f"\nHealth Status: {health}")
    
    # Test queries
    test_queries = [
        "Hello!",
        "What's AAPL's current price?",
        "Why is TSLA volatile?",
        "Compare MSFT and GOOGL",
    ]
    
    session_id = "test_session"
    
    for query in test_queries:
        print(f"\n{'='*60}")
        print(f"User: {query}")
        print("-" * 40)
        
        response = service.process_message(session_id, query)
        
        print(f"Type: {response['query_type']}")
        if response.get('sql_query'):
            print(f"SQL: {response['sql_query'][:100]}...")
        print(f"Response: {response['response'][:200]}...")
        print(f"Time: {response['processing_time_ms']}ms")
