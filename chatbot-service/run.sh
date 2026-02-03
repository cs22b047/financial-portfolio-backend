#!/bin/bash
#
# Financial Portfolio Chatbot Service - Startup Script
#

cd "$(dirname "$0")"

echo ""
echo "============================================================"
echo "  Financial Portfolio Chatbot Service"
echo "============================================================"
echo ""

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.9+"
    exit 1
fi

# Check Python version
PYTHON_VERSION=$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')
echo "✓ Python version: $PYTHON_VERSION"

# Check if virtual environment exists
if [ -d "../.venv" ]; then
    echo "✓ Using existing virtual environment: ../.venv"
    source ../.venv/bin/activate
elif [ -d ".venv" ]; then
    echo "✓ Using virtual environment: .venv"
    source .venv/bin/activate
else
    echo "ℹ️  No virtual environment found, using system Python"
fi

# Install/upgrade dependencies
echo ""
echo "📦 Installing dependencies..."
pip install -q -r requirements.txt

# Check if .env file exists
if [ ! -f ".env" ]; then
    if [ -f "../python-scripts/.env" ]; then
        echo "✓ Copying .env from python-scripts"
        cp ../python-scripts/.env .env
    else
        echo "⚠️  Warning: .env file not found"
        echo "   Please create a .env file with:"
        echo "   GROQ_API_KEY=your_api_key"
        echo "   DB_HOST=localhost"
        echo "   DB_USER=root"
        echo "   DB_PASSWORD=your_password"
        echo "   DB_NAME=portfolio_db"
    fi
fi

# Start the Flask server
echo ""
echo "🚀 Starting Chatbot Service..."
echo ""

export FLASK_APP=app.py
export FLASK_ENV=production

python3 app.py
