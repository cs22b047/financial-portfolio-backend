"""
Flask Application for the Financial Portfolio Chatbot Service.
Provides REST API endpoints for the chatbot functionality.
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import time

from config import FLASK_CONFIG, print_config, validate_config
from chatbot_service import get_chatbot_service


# Initialize Flask app
app = Flask(__name__)
CORS(app)  # Enable CORS for all routes


# =============================================================================
# API ENDPOINTS
# =============================================================================

@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check endpoint.
    
    Returns:
        JSON response with service health status
    """
    service = get_chatbot_service()
    health = service.health_check()
    
    status_code = 200 if health['status'] == 'healthy' else 503
    
    return jsonify(health), status_code


@app.route('/api/chat', methods=['POST'])
def chat():
    """
    Main chat endpoint.
    
    Request Body:
        {
            "session_id": "string",
            "message": "string",
            "history": [{"role": "user/assistant", "content": "string"}, ...]
        }
    
    Returns:
        JSON response with chatbot reply and metadata
    """
    start_time = time.time()
    
    try:
        # Parse request
        data = request.get_json()
        
        if not data:
            return jsonify({
                'error': 'Request body is required',
                'response': 'Please provide a message.',
                'query_type': 'ERROR'
            }), 400
        
        session_id = data.get('session_id', 'default')
        message = data.get('message', '').strip()
        history = data.get('history', [])
        
        if not message:
            return jsonify({
                'error': 'Message is required',
                'response': 'Please provide a message.',
                'query_type': 'ERROR'
            }), 400
        
        # Process the message
        service = get_chatbot_service()
        response = service.process_message(session_id, message, history)
        
        return jsonify(response), 200
        
    except Exception as e:
        error_msg = str(e)
        print(f"Chat endpoint error: {error_msg}")
        
        return jsonify({
            'error': error_msg,
            'response': 'Sorry, something went wrong. Please try again.',
            'query_type': 'ERROR',
            'processing_time_ms': int((time.time() - start_time) * 1000)
        }), 500


@app.route('/api/classify', methods=['POST'])
def classify():
    """
    Query classification endpoint (for testing/debugging).
    
    Request Body:
        {
            "message": "string"
        }
    
    Returns:
        JSON response with query classification
    """
    try:
        data = request.get_json()
        message = data.get('message', '').strip()
        
        if not message:
            return jsonify({'error': 'Message is required'}), 400
        
        from query_classifier import classify_query
        query_type = classify_query(message)
        
        return jsonify({
            'message': message,
            'query_type': query_type
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/sql', methods=['POST'])
def generate_sql_endpoint():
    """
    SQL generation endpoint (for testing/debugging).
    
    Request Body:
        {
            "message": "string",
            "context": "string" (optional)
        }
    
    Returns:
        JSON response with generated SQL
    """
    try:
        data = request.get_json()
        message = data.get('message', '').strip()
        context = data.get('context', '')
        
        if not message:
            return jsonify({'error': 'Message is required'}), 400
        
        from sql_generator import generate_sql
        sql, tables, error = generate_sql(message, context)
        
        if error:
            return jsonify({
                'error': error,
                'sql': None,
                'tables': []
            }), 400
        
        return jsonify({
            'message': message,
            'sql': sql,
            'tables': tables
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/symbols', methods=['GET'])
def get_symbols():
    """
    Get available stock symbols endpoint.
    
    Returns:
        JSON response with list of available symbols
    """
    try:
        from data_fetcher import DataFetcher
        fetcher = DataFetcher()
        symbols, error = fetcher.get_all_symbols()
        
        if error:
            return jsonify({'error': error, 'symbols': []}), 500
        
        return jsonify({
            'symbols': symbols,
            'count': len(symbols)
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/stock/<symbol>', methods=['GET'])
def get_stock_info(symbol: str):
    """
    Get stock information endpoint.
    
    Args:
        symbol: Stock ticker symbol
    
    Returns:
        JSON response with stock data
    """
    try:
        from data_fetcher import DataFetcher
        fetcher = DataFetcher()
        
        info, error = fetcher.get_stock_info(symbol)
        
        if error:
            return jsonify({'error': error}), 404
        
        return jsonify(info), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500


# =============================================================================
# ERROR HANDLERS
# =============================================================================

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors."""
    return jsonify({
        'error': 'Endpoint not found',
        'message': 'The requested endpoint does not exist.'
    }), 404


@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors."""
    return jsonify({
        'error': 'Internal server error',
        'message': 'Something went wrong on our end. Please try again.'
    }), 500


# =============================================================================
# MAIN
# =============================================================================

def main():
    """Main entry point for the Flask application."""
    print("\n" + "=" * 60)
    print("FINANCIAL PORTFOLIO CHATBOT SERVICE")
    print("=" * 60)
    
    # Print and validate configuration
    print_config()
    
    if not validate_config():
        print("\n⚠️  Warning: Configuration validation failed!")
        print("   The service may not function correctly.\n")
    
    print(f"\n🚀 Starting Flask server on http://{FLASK_CONFIG['host']}:{FLASK_CONFIG['port']}")
    print("   Press Ctrl+C to stop\n")
    
    # Run Flask app
    app.run(
        host=FLASK_CONFIG['host'],
        port=FLASK_CONFIG['port'],
        debug=FLASK_CONFIG['debug']
    )


if __name__ == '__main__':
    main()
