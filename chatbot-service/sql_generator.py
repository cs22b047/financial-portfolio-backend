"""
SQL Generator module for the Financial Portfolio Chatbot Service.
Uses Groq LLM to generate secure SQL queries from natural language.
"""

import re
from groq import Groq
from typing import Tuple, List, Optional

from config import GROQ_CONFIG, SQL_SECURITY
from prompts import get_sql_prompt


class SQLGenerator:
    """Generates and validates SQL queries from natural language."""
    
    def __init__(self):
        """Initialize the SQL generator with Groq client."""
        self.client = None
        if GROQ_CONFIG['api_key']:
            self.client = Groq(api_key=GROQ_CONFIG['api_key'])
    
    def generate(self, query: str, context: str = "") -> Tuple[str, List[str], Optional[str]]:
        """
        Generate a SQL query from natural language.
        
        Args:
            query: The user's natural language query
            context: Optional conversation context
            
        Returns:
            Tuple of (SQL query, list of tables used, error message or None)
        """
        if not self.client:
            return "", [], "LLM client not initialized"
        
        try:
            prompt = get_sql_prompt(query, context)
            
            response = self.client.chat.completions.create(
                model=GROQ_CONFIG['model'],
                messages=[
                    {
                        "role": "system",
                        "content": "You are a SQL query generator. Generate ONLY valid MySQL SELECT queries. No explanations, just the SQL query."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.3,  # Lower temperature for more consistent SQL
                max_tokens=500,
                top_p=0.9
            )
            
            raw_sql = response.choices[0].message.content.strip()
            
            # Clean and validate the SQL
            sql, error = self._clean_and_validate(raw_sql)
            if error:
                return "", [], error
            
            # Extract tables used
            tables = self._extract_tables(sql)
            
            # Add LIMIT if not present
            sql = self._ensure_limit(sql)
            
            return sql, tables, None
            
        except Exception as e:
            error_msg = f"SQL generation error: {str(e)}"
            print(error_msg)
            return "", [], error_msg
    
    def _clean_and_validate(self, raw_sql: str) -> Tuple[str, Optional[str]]:
        """
        Clean and validate the generated SQL.
        
        Args:
            raw_sql: The raw SQL from LLM
            
        Returns:
            Tuple of (cleaned SQL, error message or None)
        """
        # Remove markdown code blocks
        sql = raw_sql.strip()
        sql = re.sub(r'^```sql\s*', '', sql, flags=re.IGNORECASE)
        sql = re.sub(r'^```\s*', '', sql)
        sql = re.sub(r'\s*```$', '', sql)
        sql = sql.strip()
        
        # Remove any leading explanation text
        lines = sql.split('\n')
        sql_lines = []
        started = False
        for line in lines:
            line_upper = line.strip().upper()
            if line_upper.startswith('SELECT') or started:
                started = True
                sql_lines.append(line)
        sql = '\n'.join(sql_lines) if sql_lines else sql
        
        # Validate - check for blocked commands
        sql_upper = sql.upper()
        
        for blocked in SQL_SECURITY['blocked_commands']:
            # Check if blocked command appears as a keyword (not in string)
            pattern = r'\b' + blocked + r'\b'
            if re.search(pattern, sql_upper):
                return "", f"Blocked SQL command detected: {blocked}"
        
        # Validate - must start with SELECT
        if not sql_upper.strip().startswith('SELECT'):
            return "", "Only SELECT queries are allowed"
        
        # Check query length
        if len(sql) > SQL_SECURITY['max_query_length']:
            return "", "Query too long"
        
        return sql, None
    
    def _extract_tables(self, sql: str) -> List[str]:
        """
        Extract table names from a SQL query.
        
        Args:
            sql: The SQL query
            
        Returns:
            List of table names used in the query
        """
        tables = set()
        
        # Known tables in the database
        known_tables = [
            'market_data', 'technical_indicators', 'stock_summary', 'esg_ratings',
            'news', 'price_history', 'assets', 'transactions',
            'asset_types', 'conversations', 'chat_messages', 'chat_context'
        ]
        
        sql_lower = sql.lower()
        
        for table in known_tables:
            # Check if table is used (with word boundaries)
            if re.search(r'\b' + table + r'\b', sql_lower):
                tables.add(table)
        
        return list(tables)
    
    def _ensure_limit(self, sql: str) -> str:
        """
        Ensure the SQL query has a LIMIT clause.
        
        Args:
            sql: The SQL query
            
        Returns:
            SQL query with LIMIT clause
        """
        sql_upper = sql.upper()
        
        # Check if LIMIT already exists
        if 'LIMIT' in sql_upper:
            return sql
        
        # Add default LIMIT
        default_limit = SQL_SECURITY['default_limit']
        
        # Handle queries ending with semicolon
        sql = sql.rstrip(';').rstrip()
        
        return f"{sql} LIMIT {default_limit}"
    
    def validate_sql(self, sql: str) -> Tuple[bool, Optional[str]]:
        """
        Validate a SQL query for security.
        
        Args:
            sql: The SQL query to validate
            
        Returns:
            Tuple of (is_valid, error message or None)
        """
        _, error = self._clean_and_validate(sql)
        return error is None, error


# Singleton instance
_generator = None

def get_sql_generator() -> SQLGenerator:
    """Get or create the SQLGenerator singleton instance."""
    global _generator
    if _generator is None:
        _generator = SQLGenerator()
    return _generator


def generate_sql(query: str, context: str = "") -> Tuple[str, List[str], Optional[str]]:
    """
    Convenience function to generate SQL from natural language.
    
    Args:
        query: The user's natural language query
        context: Optional conversation context
        
    Returns:
        Tuple of (SQL query, list of tables used, error message or None)
    """
    return get_sql_generator().generate(query, context)


if __name__ == "__main__":
    # Test the SQL generator
    generator = SQLGenerator()
    
    test_queries = [
        "What's AAPL's current price?",
        "Show me MSFT's PE ratio and market cap",
        "Compare AAPL and GOOGL returns",
        "List stocks with RSI above 70",
        "Show recent news for TSLA"
    ]
    
    print("SQL Generation Tests")
    print("=" * 70)
    
    for query in test_queries:
        print(f"\nQuery: {query}")
        print("-" * 50)
        sql, tables, error = generator.generate(query)
        if error:
            print(f"Error: {error}")
        else:
            print(f"SQL: {sql}")
            print(f"Tables: {tables}")
