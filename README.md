# Microservices Architecture with JWT Authentication

A complete microservices architecture following industry standards with Spring Boot, Spring Cloud, Eureka Service Registry, API Gateway, and JWT-based authentication.

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  API Gateway    │  (Port 8080)
│  - Routes       │
│  - JWT Validate │
└────────┬────────┘
         │
         ├──────────────┬──────────────┐
         │              │              │
    ┌────▼────┐   ┌────▼────┐   ┌────▼────┐
    │ Service │   │  Auth   │   │ Product │
    │Registry │   │ Service │   │ Service │
    │(Eureka) │   │(Port    │   │(Port    │
    │Port 8761│   │ 8081)   │   │ 8082)   │
    └─────────┘   └────┬────┘   └────┬────┘
                       │              │
                  ┌────▼────┐    ┌───▼─────┐
                  │   H2    │    │   H2    │
                  │Auth DB  │    │Product  │
                  │         │    │   DB    │
                  └─────────┘    └─────────┘
```

## Services

### 1. Service Registry (Eureka Server) - Port 8761
- Service discovery for all microservices
- All services register themselves with this registry
- Enables dynamic service discovery and load balancing

### 2. API Gateway - Port 8080
- Single entry point for all client requests
- Routes requests to appropriate microservices
- JWT token validation (except for auth endpoints)
- Extracts user information from JWT and forwards as headers
- **Industry Practice**: Centralized security, rate limiting, and routing

### 3. Auth Service - Port 8081
- User registration and login
- JWT token generation
- Password encryption with BCrypt
- Separate H2 database for user data
- **Endpoints**:
  - `POST /auth/register` - Register new user
  - `POST /auth/login` - Login and get JWT token

### 4. Product Service - Port 8082
- Example business service
- Validates authentication via headers from API Gateway
- CRUD operations for products
- Separate H2 database for product data
- **Endpoints**:
  - `POST /products` - Create product (requires auth)
  - `GET /products` - Get all products (requires auth)
  - `GET /products/{id}` - Get product by ID (requires auth)
  - `PUT /products/{id}` - Update product (requires auth)
  - `DELETE /products/{id}` - Delete product (requires auth)

## Key Features

### ✅ Industry Standard Practices

1. **Microservices Architecture**: Each service is independent and can be deployed separately
2. **Service Discovery**: Automatic service registration and discovery using Eureka
3. **API Gateway Pattern**: Single entry point with centralized routing and security
4. **JWT Authentication**: Stateless authentication using industry-standard JWT tokens
5. **Separate Databases**: Each service has its own database (Database per Service pattern)
6. **Security Best Practices**: 
   - Password encryption with BCrypt
   - JWT tokens with expiration
   - Token validation at gateway level
7. **Clean Architecture**: Separation of concerns (Controller → Service → Repository)
8. **DTOs**: Data Transfer Objects for API requests/responses
9. **Validation**: Input validation using Jakarta Validation
10. **Error Handling**: Proper exception handling and error responses

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build All Services

```bash
# From the project root directory
mvnw clean install
```

### Running the Services

**IMPORTANT**: Start services in this order:

1. **Start Service Registry (Eureka)**
```bash
mvnw spring-boot:run -pl service-registry
```
Wait until you see "Started Eureka Server" in the logs.
Access Eureka Dashboard: http://localhost:8761

2. **Start Auth Service**
```bash
mvnw spring-boot:run -pl auth-service
```
Access H2 Console: http://localhost:8081/h2-console

3. **Start Product Service**
```bash
mvnw spring-boot:run -pl product-service
```
Access H2 Console: http://localhost:8082/h2-console

4. **Start API Gateway**
```bash
mvnw spring-boot:run -pl api-gateway
```
Gateway is ready: http://localhost:8080

## API Usage Examples

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "user@example.com",
  "fullName": "John Doe",
  "roles": "ROLE_USER",
  "message": "User registered successfully"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "user@example.com",
  "fullName": "John Doe",
  "roles": "ROLE_USER",
  "message": "Login successful"
}
```

### 3. Create a Product (Authenticated)

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stock": 50,
    "category": "Electronics"
  }'
```

### 4. Get All Products (Authenticated)

```bash
curl -X GET http://localhost:8080/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 5. Get User Info from Token

```bash
curl -X GET http://localhost:8080/products/user-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## How Authentication Works

### Flow Diagram:

```
1. Client → POST /auth/login → Auth Service
   Auth Service validates credentials and returns JWT

2. Client → GET /products (with JWT) → API Gateway
   - Gateway validates JWT
   - Extracts user info (email, id, roles)
   - Forwards request with X-User-* headers → Product Service
   
3. Product Service receives request with headers:
   - X-User-Id: user@example.com
   - X-User-Email: user@example.com
   - X-User-Roles: ROLE_USER
   - Uses this info for authorization and audit
```

### Security Features:

1. **JWT Secret**: Shared secret across Gateway and Auth Service (in production, use environment variables)
2. **Token Expiration**: 24 hours (configurable)
3. **Stateless**: No session state stored on server
4. **Header Propagation**: Gateway extracts user info and passes to downstream services
5. **Public Endpoints**: Auth endpoints (/register, /login) bypass JWT validation

## Database Configuration

### Development (Current Setup)
- **H2 In-Memory Database** for each service
- Data is reset on service restart
- Perfect for development and testing
- Access H2 console for each service:
  - Auth Service: http://localhost:8081/h2-console
  - Product Service: http://localhost:8082/h2-console

### Production Configuration
To use PostgreSQL in production, update application.yml:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: your_username
    password: your_password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Configuration Files

### Important Configuration Properties

**JWT Secret** (MUST be the same across API Gateway and Auth Service):
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  expiration: 86400000  # 24 hours
```

**Service Ports:**
- Service Registry: 8761
- API Gateway: 8080
- Auth Service: 8081
- Product Service: 8082

**Eureka Configuration:**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Testing the Setup

### 1. Check Service Registry
Visit http://localhost:8761 - You should see all services registered

### 2. Test Auth Flow
```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","fullName":"Test User"}'

# Save the token from response

# Test protected endpoint
curl -X GET http://localhost:8080/products \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 3. Test Without Token (Should Fail)
```bash
curl -X GET http://localhost:8080/products
# Expected: 401 Unauthorized
```

## Adding New Microservices

To add a new microservice following this pattern:

1. Create new module in parent POM
2. Add dependency on Eureka client
3. Include JWT validation dependencies
4. Configure separate database
5. Add route in API Gateway
6. Validate authentication via X-User-* headers

Example for a new "Order Service":
```yaml
# In api-gateway/application.yml
- id: order-service
  uri: lb://order-service
  predicates:
    - Path=/orders/**
  filters:
    - JwtAuthenticationFilter
    - RewritePath=/orders/(?<segment>.*), /${segment}
```

## Project Structure

```
microservice-parent-project/
├── service-registry/          # Eureka Server
├── api-gateway/              # API Gateway with JWT validation
├── auth-service/             # Authentication service
│   ├── controller/          # REST Controllers
│   ├── service/             # Business Logic
│   ├── repository/          # Data Access
│   ├── entity/              # JPA Entities
│   ├── dto/                 # Data Transfer Objects
│   ├── security/            # JWT Utilities
│   └── config/              # Security Config
├── product-service/          # Example business service
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── security/
└── pom.xml                   # Parent POM
```

## Troubleshooting

### Services not registering with Eureka
- Ensure Eureka is started first
- Check `eureka.client.service-url.defaultZone` is correct
- Wait 30 seconds for registration

### JWT Token Invalid
- Ensure JWT secret is the same in both Gateway and Auth Service
- Check token expiration
- Verify "Bearer " prefix in Authorization header

### Cannot connect to services
- Check all services are running: `netstat -an | findstr "8080 8081 8082 8761"`
- Verify no port conflicts

## Best Practices Implemented

✅ Each service has its own database (Database per Service)  
✅ Centralized authentication and authorization  
✅ Stateless JWT-based security  
✅ Service discovery and load balancing  
✅ Clean separation of concerns  
✅ Input validation  
✅ DTO pattern for API contracts  
✅ Exception handling  
✅ Lombok for reduced boilerplate  
✅ H2 for development, easy to switch to PostgreSQL/MySQL  

## Next Steps for Production

1. **Externalize Configuration**: Use Spring Cloud Config Server
2. **Distributed Tracing**: Add Spring Cloud Sleuth + Zipkin
3. **Centralized Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
4. **Circuit Breaker**: Add Resilience4j
5. **API Documentation**: Add Swagger/OpenAPI
6. **Containerization**: Create Dockerfiles and docker-compose
7. **CI/CD**: Set up Jenkins/GitHub Actions
8. **Monitoring**: Add Prometheus + Grafana
9. **Rate Limiting**: Add Redis-based rate limiting in Gateway
10. **HTTPS**: Enable SSL/TLS certificates

## License

This is a demo project for educational purposes.

---

**Author**: Microservices Team  
**Date**: February 2026  
**Version**: 1.0.0
