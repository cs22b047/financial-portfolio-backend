"""
Query Classifier module for the Financial Portfolio Chatbot Service.
Uses Groq LLM to classify user queries into DATA, EXPLANATION, or GENERAL.
"""

from groq import Groq
from typing import Optional

from config import GROQ_CONFIG
from prompts import get_classification_prompt


class QueryClassifier:
    """Classifies user queries into predefined categories."""
    
    # Valid query types
    QUERY_TYPES = ['DATA', 'EXPLANATION', 'GENERAL']
    
    def __init__(self):
        """Initialize the query classifier with Groq client."""
        self.client = None
        if GROQ_CONFIG['api_key']:
            self.client = Groq(api_key=GROQ_CONFIG['api_key'])
    
    def classify(self, query: str) -> str:
        """
        Classify a user query into one of the predefined categories.
        
        Args:
            query: The user's natural language query
            
        Returns:
            Classification result: 'DATA', 'EXPLANATION', or 'GENERAL'
        """
        if not self.client:
            return self._fallback_classify(query)
        
        try:
            prompt = get_classification_prompt(query)
            
            response = self.client.chat.completions.create(
                model=GROQ_CONFIG['model'],
                messages=[
                    {
                        "role": "system",
                        "content": "You are a query classifier. Respond with exactly one word: DATA, EXPLANATION, or GENERAL."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=GROQ_CONFIG['classification_temperature'],
                max_tokens=10,
                top_p=0.9
            )
            
            result = response.choices[0].message.content.strip().upper()
            
            # Validate the result
            if result in self.QUERY_TYPES:
                return result
            
            # If LLM returned something unexpected, use fallback
            return self._fallback_classify(query)
            
        except Exception as e:
            print(f"Classification error: {e}")
            return self._fallback_classify(query)
    
    def _fallback_classify(self, query: str) -> str:
        """
        Fallback classification using keyword matching.
        
        Args:
            query: The user's natural language query
            
        Returns:
            Classification result based on keywords
        """
        query_lower = query.lower().strip()
        
        # General greetings and help
        general_keywords = [
            'hello', 'hi', 'hey', 'thanks', 'thank you', 'help',
            'what can you', 'how do i', 'goodbye', 'bye'
        ]
        
        # Explanation keywords
        explanation_keywords = [
            'why', 'explain', 'reason', 'cause', 'analysis',
            'should i', 'recommend', 'what happened', 'understand'
        ]
        
        # Data keywords
        data_keywords = [
            'price', 'what is', 'what\'s', 'show', 'list', 'get',
            'how much', 'compare', 'rsi', 'pe ratio', 'volume',
            'market cap', 'dividend', 'return', 'performance'
        ]
        
        # Check general first (highest priority for greetings)
        for keyword in general_keywords:
            if keyword in query_lower:
                return 'GENERAL'
        
        # Check explanation keywords
        for keyword in explanation_keywords:
            if keyword in query_lower:
                return 'EXPLANATION'
        
        # Check data keywords
        for keyword in data_keywords:
            if keyword in query_lower:
                return 'DATA'
        
        # Default to DATA for queries with stock symbols
        import re
        if re.search(r'\b[A-Z]{1,5}\b', query):
            return 'DATA'
        
        # Default to GENERAL for everything else
        return 'GENERAL'


# Singleton instance
_classifier = None

def get_classifier() -> QueryClassifier:
    """Get or create the QueryClassifier singleton instance."""
    global _classifier
    if _classifier is None:
        _classifier = QueryClassifier()
    return _classifier


def classify_query(query: str) -> str:
    """
    Convenience function to classify a query.
    
    Args:
        query: The user's natural language query
        
    Returns:
        Classification result: 'DATA', 'EXPLANATION', or 'GENERAL'
    """
    return get_classifier().classify(query)


if __name__ == "__main__":
    # Test the classifier
    classifier = QueryClassifier()
    
    test_queries = [
        "What's AAPL's current price?",
        "Why did TSLA drop yesterday?",
        "Hello",
        "Compare AAPL and MSFT returns",
        "Explain NVDA's recent performance",
        "Help me understand the market",
        "Show me stocks with RSI above 70",
        "What can you do?",
        "Thanks!"
    ]
    
    print("Query Classification Tests")
    print("=" * 60)
    
    for query in test_queries:
        result = classifier.classify(query)
        print(f"{result:12} | {query}")
