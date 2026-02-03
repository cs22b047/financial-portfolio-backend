"""
Configuration module for the Financial Portfolio Chatbot Service.
Loads settings from environment variables with sensible defaults.
"""

import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# =============================================================================
# DATABASE CONFIGURATION
# =============================================================================

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', 'root351973'),
    'database': os.getenv('DB_NAME', 'portfolio_db'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'charset': 'utf8mb4',
    'collation': 'utf8mb4_general_ci'
}

# =============================================================================
# GROQ LLM CONFIGURATION
# =============================================================================

GROQ_CONFIG = {
    'api_key': os.getenv('GROQ_API_KEY'),
    'model': os.getenv('GROQ_MODEL', 'meta-llama/llama-4-scout-17b-16e-instruct'),
    'temperature': float(os.getenv('GROQ_TEMPERATURE', '0.7')),
    'max_tokens': int(os.getenv('GROQ_MAX_TOKENS', '1000')),
    'classification_temperature': 0.1,  # Low temperature for deterministic classification
}

# =============================================================================
# FLASK SERVER CONFIGURATION
# =============================================================================

FLASK_CONFIG = {
    'host': os.getenv('FLASK_HOST', '0.0.0.0'),
    'port': int(os.getenv('FLASK_PORT', 5000)),
    'debug': os.getenv('FLASK_DEBUG', 'false').lower() == 'true'
}

# =============================================================================
# SQL SECURITY CONFIGURATION
# =============================================================================

SQL_SECURITY = {
    # Only these SQL commands are allowed
    'allowed_commands': ['SELECT'],
    
    # These commands are blocked
    'blocked_commands': [
        'INSERT', 'UPDATE', 'DELETE', 'DROP', 'ALTER', 'CREATE',
        'TRUNCATE', 'GRANT', 'REVOKE', 'EXECUTE', 'CALL'
    ],
    
    # Maximum results to return
    'default_limit': 100,
    
    # Maximum query length
    'max_query_length': 2000,
}

# =============================================================================
# CONTEXT CONFIGURATION
# =============================================================================

CONTEXT_CONFIG = {
    'max_history_messages': 10,
    'symbol_pattern': r'\b[A-Z]{1,5}\b',  # Pattern to extract stock symbols
}

# =============================================================================
# VALIDATION
# =============================================================================

def validate_config():
    """Validate that required configuration is present."""
    errors = []
    
    if not GROQ_CONFIG['api_key']:
        errors.append("GROQ_API_KEY is not set in environment variables")
    
    if not DB_CONFIG['password']:
        errors.append("DB_PASSWORD is not set in environment variables")
    
    if errors:
        print("Configuration Errors:")
        for error in errors:
            print(f"  - {error}")
        return False
    
    return True


def print_config():
    """Print current configuration (hiding sensitive values)."""
    print("\n" + "=" * 60)
    print("CHATBOT SERVICE CONFIGURATION")
    print("=" * 60)
    print(f"\nDatabase:")
    print(f"  Host:     {DB_CONFIG['host']}:{DB_CONFIG['port']}")
    print(f"  Database: {DB_CONFIG['database']}")
    print(f"  User:     {DB_CONFIG['user']}")
    
    print(f"\nGroq LLM:")
    print(f"  Model:       {GROQ_CONFIG['model']}")
    print(f"  Temperature: {GROQ_CONFIG['temperature']}")
    print(f"  Max Tokens:  {GROQ_CONFIG['max_tokens']}")
    print(f"  API Key:     {'✓ Set' if GROQ_CONFIG['api_key'] else '✗ Missing'}")
    
    print(f"\nFlask Server:")
    print(f"  Host: {FLASK_CONFIG['host']}")
    print(f"  Port: {FLASK_CONFIG['port']}")
    print(f"  Debug: {FLASK_CONFIG['debug']}")
    print("=" * 60 + "\n")


if __name__ == "__main__":
    print_config()
    validate_config()
