# Spring Boot Application

## 🧰 Stack
Java 21  
Spring Boot  
Gradle  
PostgreSQL  
Docker Compose  
AWS Cognito  
Swagger UI  

## 🚀 How to Run
1. Clone the repository:
   git clone <repository-url>
   cd <project-folder>

2. Create a .env file in the project root and define the required environment variables:
   DB_NAME=<your_db_name>
   DB_PORT=5432
   DB_HOST=localhost
   DB_USERNAME=postgres  
   DB_PASSWORD=postgres  
   COGNITO_CLIENT_ID=<your-client-id>  

4. Start PostgreSQL with Docker Compose:
   docker-compose up -d

5. Run the Spring Boot application:
   ./gradlew bootRun  
   # or build and run the JAR:  
   ./gradlew build  
   java -jar build/libs/<your-jar-file>.jar

6. Open the application in your browser:
   http://localhost:8080

7. Verify that it works by opening Swagger UI:
   http://localhost:8080/swagger-ui/swagger-ui/index.html

## ⚙️ Configuration
Port: 8080  
Database: PostgreSQL (from docker-compose.yml)  
Environment variables: loaded from .env  
Authentication: AWS Cognito  
Active Profile: default (no profile required)

## 🔒 Authentication
The application uses AWS Cognito for user authentication and authorization.  
All secured endpoints require a valid JWT access token issued by Cognito.  
Cognito configuration values (issuer-uri, client-id) are loaded from the .env file.

## 🧩 Notes
Make sure Docker is running before starting the database.  
To stop containers: docker-compose down  
Logs are printed to the console when running via Gradle.  
Use Swagger UI to test API endpoints with JWT tokens.
