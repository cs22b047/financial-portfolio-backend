# Starting the Chatbot Server

This guide explains how to start and run the chatbot service for the financial portfolio backend.

## Prerequisites

- Python 3.8 or higher
- MySQL database (portfolio_db) running and accessible
- Database credentials (default: root/root351973)
- Groq API key for AI responses

## Installation

### 1. Navigate to the chatbot service directory

```bash
cd /Users/rudrapanda/Desktop/financial-portfolio-backend/chatbot-service
```

### 2. Install dependencies

Using pip:
```bash
pip3 install flask flask-cors groq mysql-connector-python python-dotenv requests
```

Or using the requirements file:
```bash
pip3 install -r requirements.txt
```

Using the project's virtual environment (recommended):
```bash
/Users/rudrapanda/Desktop/financial-portfolio-backend/.venv/bin/pip install -r requirements.txt
```

## Configuration

### Environment Variables

Create a `.env` file in the `chatbot-service` directory with the following variables:

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=root351973
DB_NAME=portfolio_db
GROQ_API_KEY=your_groq_api_key_here
```

## Running the Server

### Method 1: Direct Python Execution

From the chatbot-service directory:
```bash
python3 app.py
```

Or using the virtual environment:
```bash
/Users/rudrapanda/Desktop/financial-portfolio-backend/.venv/bin/python app.py
```

### Method 2: Using the Shell Script

```bash
./run.sh
```

### Method 3: Background Process

To run in the background:
```bash
/Users/rudrapanda/Desktop/financial-portfolio-backend/.venv/bin/python app.py &
```

## Verification

### 1. Check if the server is running

```bash
lsof -i :5000 2>/dev/null | grep -v "^COMMAND" | head -1
```

### 2. Test the health endpoint

```bash
curl -s http://localhost:5000/health | python3 -m json.tool
```

Expected response:
```json
{
  "status": "healthy",
  "database": "connected"
}
```

### 3. Test the chat endpoint

```bash
curl -s -X POST http://localhost:5000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Why is NVIDIA stock performing well lately?"}' \
  | python3 -m json.tool
```

## API Endpoints

- **Health Check**: `GET http://localhost:5000/health`
- **Chat**: `POST http://localhost:5000/api/chat`
  - Body: `{"message": "your question here"}`

## Troubleshooting

### Port 5000 Already in Use

Kill the existing process:
```bash
lsof -i :5000 | awk 'NR>1 {print $2}' | xargs kill -9 2>/dev/null
```

Then restart the server.

### Database Connection Issues

1. Verify MySQL is running:
   ```bash
   mysql -u root -proot351973 portfolio_db -e "SHOW TABLES;"
   ```

2. Check database credentials in config.py or .env file

3. Ensure the news table exists:
   ```bash
   mysql -u root -proot351973 portfolio_db -e "DESCRIBE news;" 2>/dev/null
   ```

### Import Errors

Ensure all dependencies are installed:
```bash
pip3 install flask flask-cors groq mysql-connector-python python-dotenv requests
```

## Server Information

- **Default Port**: 5000
- **Host**: localhost (0.0.0.0 for external access)
- **CORS**: Enabled for all origins
- **Database**: portfolio_db

## Stopping the Server

If running in foreground, press `Ctrl+C`.

If running in background:
```bash
lsof -i :5000 | awk 'NR>1 {print $2}' | xargs kill -9
```

## Additional Notes

- The chatbot uses Groq AI for generating responses
- It fetches real-time data from the MySQL database
- Query classification determines which data sources to use
- SQL queries are generated dynamically based on user questions
