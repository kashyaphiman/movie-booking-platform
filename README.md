# Movie Ticket Booking Platform

A comprehensive, scalable movie ticket booking platform built with Spring Boot, supporting B2B and B2C operations with theatre management, payment processing, and multi-channel notifications.

## 🚀 Features

### Core Functionality
- **Movie Management**: Add, update, and manage movie catalog with metadata
- **Theatre Management**: Manage theatres, screens, and show schedules
- **Booking System**: Real-time seat selection and ticket booking
- **User Management**: Customer registration, authentication, and profiles
- **Payment Integration**: Support for Stripe and Razorpay payment gateways
- **Multi-channel Notifications**: Email, SMS, and WhatsApp notifications

### Advanced Features
- **Rate Limiting**: Distributed rate limiting with Redis
- **Caching**: Redis-based caching for performance optimization
- **Async Processing**: Background processing for notifications and heavy tasks
- **Security**: JWT-based authentication with role-based access control
- **Monitoring**: Comprehensive metrics with Prometheus and Grafana
- **API Documentation**: Swagger/OpenAPI documentation
- **Database Migration**: Flyway for database versioning

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.1.5
- **Database**: PostgreSQL (Production) / H2 (Development)
- **Cache**: Redis
- **Security**: Spring Security with JWT
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven
- **Java Version**: 17

### Project Structure
```
movie-ticket-platform/
├── src/main/java/com/xyz/movieticket/
│   ├── config/           # Configuration classes
│   │   ├── AsyncConfig.java
│   │   ├── CacheConfig.java
│   │   ├── DataSourceConfig.java
│   │   ├── JpaConfig.java
│   │   ├── MonitoringConfig.java
│   │   ├── RateLimitingConfig.java
│   │   ├── RestTemplateConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── WebConfig.java
│   ├── controller/       # REST controllers
│   │   ├── AuthController.java
│   │   ├── BookingController.java
│   │   ├── HealthCheckController.java
│   │   ├── MovieController.java
│   │   ├── PaymentController.java
│   │   ├── ReportController.java
│   │   ├── TheatreController.java
│   │   └── UserController.java
│   ├── dto/             # Data Transfer Objects
│   │   ├── request/     # Request DTOs
│   │   └── response/    # Response DTOs
│   ├── exception/       # Custom exceptions
│   ├── interceptor/     # HTTP interceptors
│   ├── listener/        # Application listeners
│   ├── model/           # JPA entities
│   │   ├── Booking.java
│   │   ├── Movie.java
│   │   ├── Seat.java
│   │   ├── Show.java
│   │   ├── Theatre.java
│   │   └── User.java
│   ├── repository/      # JPA repositories
│   ├── scheduler/       # Scheduled tasks
│   ├── security/        # Security components
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── RateLimiterFilter.java
│   ├── service/         # Business logic
│   │   ├── impl/        # Service implementations
│   │   └── strategy/    # Strategy pattern implementations
│   ├── util/            # Utility classes
│   └── validator/       # Custom validators
├── src/main/resources/
│   ├── application.yaml      # Production configuration
│   ├── application-h2.yml    # Development configuration
│   ├── db/migration/         # Flyway migrations
│   └── static/               # Static resources
├── src/test/                 # Test classes
└── target/                   # Build output
```

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (Production)
- Redis 6+ (Optional, for caching and rate limiting)
- SMTP server (for email notifications)

## 🚀 Quick Start

### Development Setup (H2 Database)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd movie-ticket-platform
   ```

2. **Run with H2 profile**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
   ```

3. **Access the application**
   - API: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Actuator: http://localhost:8080/actuator

### Production Setup (PostgreSQL)

1. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE movie_ticket_db;
   CREATE USER movie_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE movie_ticket_db TO movie_user;
   ```

2. **Configure environment variables**
   ```bash
   export DB_USERNAME=movie_user
   export DB_PASSWORD=your_password
   export JWT_SECRET=your_jwt_secret
   export REDIS_HOST=localhost
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

## 🔧 Configuration

### Application Profiles
- **default**: Production profile with PostgreSQL
- **h2**: Development profile with H2 in-memory database

### Key Configuration Properties

#### Database
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/movie_ticket_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

#### Caching
```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
```

#### Security
```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000
```

#### Rate Limiting
```yaml
app:
  rate-limiting:
    enabled: true
    default-per-minute: 60
    booking-per-minute: 10
    use-redis: true
```

## 📚 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh JWT token

### Movies
- `GET /api/v1/movies` - List all movies
- `GET /api/v1/movies/{id}` - Get movie details
- `POST /api/v1/movies` - Add new movie (Admin)
- `PUT /api/v1/movies/{id}` - Update movie (Admin)

### Theatres
- `GET /api/v1/theatres` - List theatres
- `POST /api/v1/theatres` - Add theatre (Theatre Admin)
- `GET /api/v1/theatres/{id}/shows` - Get theatre shows

### Bookings
- `GET /api/v1/bookings/theatres/by-movie` - Browse theatres by movie
- `POST /api/v1/bookings/book` - Book tickets
- `GET /api/v1/bookings/{id}` - Get booking details
- `DELETE /api/v1/bookings/{id}` - Cancel booking

### Payments
- `POST /api/v1/payments/initiate` - Initiate payment
- `POST /api/v1/payments/webhook` - Payment webhook
- `GET /api/v1/payments/{id}/status` - Check payment status

## 🧪 Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw verify
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/v1/movies

# Using wrk
wrk -t4 -c100 -d30s http://localhost:8080/api/v1/bookings/theatres/by-movie
```

## 📊 Monitoring

### Health Checks
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Application metrics

### Prometheus Metrics
- `GET /actuator/prometheus` - Prometheus metrics endpoint

### Key Metrics
- HTTP request/response metrics
- Database connection pool metrics
- Cache hit/miss ratios
- Rate limiting metrics
- JVM metrics

## 🔒 Security

### Authentication
- JWT-based authentication
- Role-based access control (CUSTOMER, THEATRE_ADMIN, ADMIN)

### Authorization
- Method-level security with `@PreAuthorize`
- Endpoint-level security configuration

### Rate Limiting
- Distributed rate limiting with Redis
- Per-user and per-endpoint limits
- Configurable limits for different user types

## 🐳 Docker Deployment

### Build Docker Image
```dockerfile
FROM openjdk:17-slim
COPY target/movie-ticket-platform-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose (Development)
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=h2
    depends_on:
      - redis

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

## 🚀 Scaling

See [SCALING_GUIDE.md](SCALING_GUIDE.md) for detailed scaling strategies including:
- Vertical scaling
- Horizontal scaling
- Microservices architecture
- Event-driven architecture

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team

## 🔄 Version History

- **v1.0.0**: Initial release with core booking functionality
  - Movie and theatre management
  - User authentication and authorization
  - Ticket booking and payment processing
  - Multi-channel notifications
  - Rate limiting and caching
  - Monitoring and metrics</content>
<parameter name="filePath">C:\movie_ticket_booking\movie-ticket-platform\movie-ticket-platform\README.md
