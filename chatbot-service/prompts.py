"""
LLM Prompts for the Financial Portfolio Chatbot Service.
Contains all prompts used for query classification, SQL generation, and response generation.
"""

# =============================================================================
# DATABASE SCHEMA INFORMATION
# =============================================================================

SCHEMA_INFO = """
DATABASE SCHEMA - portfolio_db (MySQL)

1. market_data - Current stock information and fundamentals
   Columns: id, symbol (VARCHAR, unique ticker like 'AAPL'), name (full company name like 'Apple Inc.'), 
            sector, industry, current_price, market_cap, volume, day_change, day_change_percent,
            day_high, day_low, week_52_high, week_52_low, pe_ratio, eps,
            dividend_yield, beta, previous_close, ask_price, bid_price,
            exchange, currency, market_status, data_source, last_updated,
            asset_type_id, created_date, updated_date

2. technical_indicators - Technical analysis data
   Columns: id, price_history_id, indicator_date, rsi, macd, macd_signal,
            macd_histogram, sma_20, sma_50, sma_200, ema_12, ema_26,
            bb_upper (Bollinger upper), bb_middle, bb_lower, bb_width,
            atr, stochastic_k, stochastic_d, momentum_10, roc_10,
            daily_return, log_return, cumulative_return, volume_sma_20,
            volume_ratio, created_date, updated_date

3. stock_summary - Performance summary and risk metrics (only period='5y' available)
   Columns: id, market_data_id, period (currently only '5y' available),
            start_date, end_date, start_price, end_price, total_return,
            annualized_return, avg_daily_return, daily_volatility,
            annualized_volatility, sharpe_ratio, max_drawdown, var_95, var_99,
            max_daily_gain, max_daily_loss, max_price, min_price,
            avg_price, max_volume, min_volume, avg_volume, trading_days,
            created_date, updated_date

4. esg_ratings - ESG (Environmental, Social, Governance) ratings
   Columns: id, market_data_id, symbol, total_score (overall ESG score),
            total_grade (A-F grade), environment_score, environment_grade,
            social_score, social_grade, governance_score, governance_grade,
            controversy_level, risk_level, data_source, last_updated, created_date

5. news - News articles with sentiment (may be empty)
   Columns: id, market_data_id, symbol, title, summary, publisher, link,
            published_date, source, sentiment (POSITIVE/NEGATIVE/NEUTRAL),
            image_url, is_read, created_date

6. price_history - Historical OHLCV data
   Columns: id, market_data_id, price_date, open_price, high_price, low_price,
            close_price, adjusted_close, volume, data_source, created_date

7. assets - User portfolio holdings
   Columns: id, symbol, name, quantity, purchase_price, current_price,
            purchase_date, notes, asset_type_id, user_id, created_date

8. transactions - Buy/sell transaction history
   Columns: id, asset_id, transaction_type, quantity, price, transaction_date,
            fees, notes, created_date

9. dividends - Dividend payment records
   Columns: id, asset_id, amount, payment_date, ex_dividend_date, record_date,
            created_date

10. asset_types - Asset type reference table
    Columns: id, name, description, created_date

KEY RELATIONSHIPS:
- market_data.asset_type_id -> asset_types.id
- price_history.market_data_id -> market_data.id
- technical_indicators.price_history_id -> price_history.id
- stock_summary.market_data_id -> market_data.id
- esg_ratings.market_data_id -> market_data.id (also has symbol column for direct lookup)
- news.market_data_id -> market_data.id (also has symbol column for direct lookup)

COMPANY NAME TO SYMBOL MAPPINGS (use these for name lookups):
- Apple, Apple Inc. -> AAPL
- Microsoft, Microsoft Corporation -> MSFT
- Google, Alphabet, Alphabet Inc. -> GOOGL
- Amazon, Amazon.com -> AMZN
- NVIDIA, Nvidia Corporation -> NVDA
- Meta, Meta Platforms, Facebook -> META
- Tesla, Tesla Inc. -> TSLA
- JPMorgan, JPMorgan Chase -> JPM
- Visa, Visa Inc. -> V
- Johnson & Johnson -> JNJ

IMPORTANT: When user mentions a company NAME (not symbol):
1. Use: WHERE md.name LIKE '%CompanyName%' OR md.symbol = 'SYMBOL'
2. Or find symbol first via subquery: WHERE symbol = (SELECT symbol FROM market_data WHERE name LIKE '%CompanyName%' LIMIT 1)
"""

# =============================================================================
# QUERY CLASSIFICATION PROMPT
# =============================================================================

CLASSIFICATION_PROMPT = """You are a query classifier for a financial portfolio chatbot.

Classify the user's query into exactly ONE of these categories:
- DATA: Questions asking for specific data, numbers, prices, statistics, or comparisons
- EXPLANATION: Questions asking WHY something happened, analysis, or reasoning
- GENERAL: Greetings, help requests, or general conversation

Examples:
- "What's AAPL's price?" -> DATA
- "Show me MSFT's PE ratio" -> DATA
- "Compare AAPL and MSFT returns" -> DATA
- "List my portfolio holdings" -> DATA
- "What stocks have RSI above 70?" -> DATA
- "Why did AAPL drop yesterday?" -> EXPLANATION
- "Explain TSLA's recent performance" -> EXPLANATION
- "What caused the market decline?" -> EXPLANATION
- "Should I buy NVDA?" -> EXPLANATION
- "Hello" -> GENERAL
- "What can you do?" -> GENERAL
- "Help" -> GENERAL
- "Thanks" -> GENERAL

User Query: {query}

Respond with ONLY one word: DATA, EXPLANATION, or GENERAL"""

# =============================================================================
# SQL GENERATION PROMPT
# =============================================================================

SQL_GENERATION_PROMPT = """You are a SQL query generator for a financial portfolio database.

{schema}

RULES:
1. Generate ONLY MySQL SELECT queries
2. NEVER use INSERT, UPDATE, DELETE, DROP, ALTER, CREATE
3. Always include appropriate JOINs when data spans multiple tables
4. Use table aliases for readability
5. Include LIMIT clause (max 100 rows) unless user specifies otherwise
6. For stock symbols, use uppercase (e.g., 'AAPL', 'MSFT')
7. Use appropriate date functions for time-based queries
8. Return ONLY the SQL query, no explanation
9. USE EXACT COLUMN NAMES from schema - do not guess or make up column names
10. When user mentions company NAME (Apple, Microsoft, etc.), use LIKE on the name column OR map to known symbol

CRITICAL COLUMN MAPPINGS (use these exact names):
- esg_ratings: total_score (NOT overall_score), total_grade, environment_score (NOT environmental_score), 
  environment_grade, social_score, social_grade, governance_score, governance_grade, controversy_level, risk_level
- market_data: name (NOT company_name), day_high, day_low
- price_history: adjusted_close (NOT adj_close)
- technical_indicators: bb_upper, bb_middle, bb_lower (NOT bollinger_*)
- stock_summary: period is '5y' (NOT '1y', '1m', etc.)

COMPANY NAME HANDLING:
- "Apple" or "Apple Inc." -> WHERE symbol = 'AAPL' OR name LIKE '%Apple%'
- "Microsoft" -> WHERE symbol = 'MSFT' OR name LIKE '%Microsoft%'
- "Tesla" -> WHERE symbol = 'TSLA' OR name LIKE '%Tesla%'
- "Google" or "Alphabet" -> WHERE symbol = 'GOOGL' OR name LIKE '%Alphabet%'
- "Amazon" -> WHERE symbol = 'AMZN' OR name LIKE '%Amazon%'
- "NVIDIA" -> WHERE symbol = 'NVDA' OR name LIKE '%NVIDIA%'
- "Meta" or "Facebook" -> WHERE symbol = 'META' OR name LIKE '%Meta%'

COMMON QUERY EXAMPLES:
- ESG by symbol/name: SELECT total_score, total_grade, environment_score, social_score, governance_score FROM esg_ratings WHERE symbol = 'AAPL'
- ESG by company name: SELECT er.* FROM esg_ratings er JOIN market_data md ON er.symbol = md.symbol WHERE md.name LIKE '%Apple%' OR md.symbol = 'AAPL'
- Current price: SELECT symbol, name, current_price, day_change, day_change_percent FROM market_data WHERE symbol = 'AAPL' OR name LIKE '%Apple%'
- Compare stocks: SELECT symbol, name, current_price, day_change_percent FROM market_data WHERE symbol IN ('AAPL', 'MSFT') ORDER BY current_price DESC
- Technical indicators: SELECT ti.rsi, ti.macd, ti.sma_20, ti.sma_50, ti.bb_upper, ti.bb_lower FROM technical_indicators ti JOIN price_history ph ON ti.price_history_id = ph.id JOIN market_data md ON ph.market_data_id = md.id WHERE md.symbol = 'AAPL' ORDER BY ti.indicator_date DESC LIMIT 1
- Performance summary: SELECT ss.total_return, ss.annualized_return, ss.sharpe_ratio, ss.max_drawdown FROM stock_summary ss JOIN market_data md ON ss.market_data_id = md.id WHERE md.symbol = 'AAPL'
- Price history: SELECT price_date, open_price, close_price, high_price, low_price, volume FROM price_history ph JOIN market_data md ON ph.market_data_id = md.id WHERE md.symbol = 'AAPL' ORDER BY price_date DESC LIMIT 30
- Top stocks by metric: SELECT symbol, name, current_price, pe_ratio, market_cap FROM market_data ORDER BY market_cap DESC LIMIT 10
- Sector analysis: SELECT symbol, name, current_price, sector FROM market_data WHERE sector = 'Technology' ORDER BY market_cap DESC

User Query: {query}

Context (if any): {context}

Generate the SQL query:"""

# =============================================================================
# RESPONSE GENERATION PROMPT
# =============================================================================

RESPONSE_GENERATION_PROMPT = """You are a helpful financial assistant. Generate a clear, concise response based on the query and data.

User Query: {query}

Data Retrieved:
{data}

GUIDELINES:
1. Present numbers clearly with appropriate formatting (%, $, commas)
2. Keep response concise but informative (2-4 sentences for simple queries)
3. Highlight key insights from the data
4. If comparing stocks, organize the comparison clearly
5. Use natural language, avoid technical jargon unless asked
6. If data is empty, say "I couldn't find data for [topic]"

Generate a helpful response:"""

# =============================================================================
# EXPLANATION GENERATION PROMPT
# =============================================================================

EXPLANATION_PROMPT = """You are a financial analyst providing insight into market movements.

{schema}

User Question: {query}

Available Data:
{data}

ANALYSIS GUIDELINES:
1. Analyze price movements and trends
2. Consider news sentiment if available
3. Look at technical indicators (RSI, MACD, etc.)
4. Compare with market/sector performance if relevant
5. Provide 2-3 paragraphs of insightful analysis
6. Be objective - present facts, not predictions
7. Mention any relevant risks or considerations

Generate your analysis:"""

# =============================================================================
# GENERAL CONVERSATION PROMPT
# =============================================================================

GENERAL_PROMPT = """You are a helpful financial portfolio assistant. Respond to the user's general query.

User: {query}

You can help users with:
- Stock prices and market data
- Technical analysis indicators
- Portfolio performance tracking
- ESG ratings and sustainability data
- News and sentiment analysis
- Performance comparisons between stocks
- Historical price data

Keep your response friendly, helpful, and concise (2-3 sentences).

Response:"""

# =============================================================================
# ERROR RESPONSE TEMPLATES
# =============================================================================

ERROR_RESPONSES = {
    'no_data': "I couldn't find any data for {topic}. Please check the stock symbol or try a different query.",
    'invalid_symbol': "I don't recognize the stock symbol '{symbol}'. Please use a valid ticker symbol (e.g., AAPL, MSFT, GOOGL).",
    'sql_error': "I encountered an issue processing your request. Please try rephrasing your question.",
    'llm_error': "I'm having trouble generating a response right now. Please try again in a moment.",
    'timeout': "The request took too long to process. Please try a simpler query.",
    'general_error': "Something went wrong. Please try again or rephrase your question."
}

# =============================================================================
# HELPER FUNCTIONS
# =============================================================================

def get_classification_prompt(query: str) -> str:
    """Get the classification prompt with the user query."""
    return CLASSIFICATION_PROMPT.format(query=query)


def get_sql_prompt(query: str, context: str = "") -> str:
    """Get the SQL generation prompt with schema, query, and context."""
    return SQL_GENERATION_PROMPT.format(
        schema=SCHEMA_INFO,
        query=query,
        context=context if context else "No specific context"
    )


def get_response_prompt(query: str, data: str) -> str:
    """Get the response generation prompt with query and data."""
    return RESPONSE_GENERATION_PROMPT.format(
        query=query,
        data=data if data else "No data retrieved"
    )


def get_explanation_prompt(query: str, data: str) -> str:
    """Get the explanation prompt with schema, query, and data."""
    return EXPLANATION_PROMPT.format(
        schema=SCHEMA_INFO,
        query=query,
        data=data if data else "No data available"
    )


def get_general_prompt(query: str) -> str:
    """Get the general conversation prompt."""
    return GENERAL_PROMPT.format(query=query)


def get_error_response(error_type: str, **kwargs) -> str:
    """Get an error response template with formatted values."""
    template = ERROR_RESPONSES.get(error_type, ERROR_RESPONSES['general_error'])
    return template.format(**kwargs) if kwargs else template
