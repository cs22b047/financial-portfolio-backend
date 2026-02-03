# Financial Portfolio Management System
**The Silicon Cartel** - Tilak, Rudra, Vaishnavi, Deekshitha

---

## Architecture Overview
![Architecture Diagram](<architecture.png>)

## Frontend

### Framework
**React**

### Responsibilities
- Rendering the user interface
- Handling user interactions and events
- Making REST API calls to the backend
- Consuming and displaying JSON responses

### Why React Over Vanilla JavaScript?
We chose React for several key advantages:
- **Modular Development**: Facilitates component-based architecture, making it ideal for team collaboration
- **Rich Ecosystem**: Extensive availability of predefined libraries and plugins
- **Well Documented**: Comprehensive documentation and strong community support compared to vanilla JavaScript
- **Performance**: Virtual DOM for efficient rendering and updates

---

## Backend

### Framework
**Spring Boot**

### Responsibilities
- Handle HTTP/HTTPS requests from the frontend
- Retrieve and manage data from the database
- Execute scheduled jobs to fetch real-time financial data from external APIs
- Store financial data persistently in the database

### Why Spring Boot?
- Part of problem statement

### Key Design Decisions

#### API Data Caching
We cache data from external financial APIs in our database for the following reasons:
- **Rate Limiting**: Open-source APIs impose rate limits; caching prevents exceeding quotas during multiple frontend reloads
- **Performance**: Reduces latency by serving cached data instead of making external API calls
- **Reliability**: Protects against external API downtime or changes
- **Decoupling**: Separating external API logic from frontend and database allows for easier maintenance and updates

---

## External APIs

### APIs Used
- *(To be documented)*

### Integration Strategy
- Scheduled data fetching via background jobs
- Data validation and transformation before storage
- Error handling and retry mechanisms
- API response caching with appropriate TTL (Time To Live)

---

## Database Design


## Project Structure

```
portfolioapp/
├── src/main/java/com/example/portfolioapp/
│   ├── controller/     # REST API endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Database access
│   └── entity/         # Domain models
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL/PostgreSQL database

### Running the Application
```bash
# Clone the repository
git clone <repository-url>

# Navigate to project directory
cd portfolioapp

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

---

## Team
**The Silicon Cartel**
- Tilak
- Rudra
- Vaishnavi
- Deekshitha


