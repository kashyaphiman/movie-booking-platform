# Movie Ticket Booking Platform - Scaling Guide

## Phase 1: Single Instance Optimization (Current)
- ✅ Connection pooling (HikariCP)
- ✅ Caching (Redis/Simple cache)
- ✅ Async processing (ThreadPoolTaskExecutor)
- ✅ Rate limiting (In-memory + Redis)
- ✅ Monitoring (Prometheus/Micrometer)

## Phase 2: Vertical Scaling
1. Increase server resources (CPU, RAM)
2. Tune JVM heap size
3. Optimize database queries
4. Increase connection pool sizes

## Phase 3: Horizontal Scaling
1. Deploy multiple instances
2. Add load balancer (Nginx/HAProxy)
3. Use Redis for distributed caching
4. Configure distributed sessions
5. Implement API Gateway

## Phase 4: Microservices Architecture
Split into:
- Booking Service (core business logic)
- Payment Service (payment processing)
- Notification Service (email, SMS, WhatsApp)
- Theatre Service (theatre & show management)
- User Service (authentication & authorization)
- Search Service (movie & theatre search - high read)

## Phase 5: Event-Driven Architecture
Use message queues:
- RabbitMQ / Kafka for async processing
- Event sourcing for booking state changes
- CQRS for read/write optimization

## Key Metrics to Monitor
- Requests per second (RPS)
- Response time (p50, p95, p99)
- Database query time
- Cache hit rate
- Error rate
- Active database connections

## Configuration for Multi-Instance Deployment

### application.yaml (Updated for scaling)
```yaml
spring:
  application:
    name: movie-ticket-platform
  profiles:
    active: prod
  
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase for multi-instance
      minimum-idle: 5
      
  data:
    redis:
      host: redis-cluster.internal
      port: 6379
      lettuce:
        pool:
          max-active: 20
          max-idle: 10

server:
  port: 8080
  servlet:
    context-path: /api
  shutdown: graceful
  
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      instance: ${INSTANCE_ID:default}
      
app:
  rate-limiting:
    use-redis: true  # Enable Redis for distributed rate limiting
    enabled: true
```

## Load Testing Commands
```bash
# Using Apache Bench
ab -n 10000 -c 100 http://localhost:8080/api/v1/movies

# Using wrk
wrk -t12 -c400 -d30s http://localhost:8080/api/v1/bookings/theatres/by-movie

# Using JMeter
jmeter -n -t test_plan.jmx -l results.jtl
```

## Deployment with Docker & Kubernetes
```dockerfile
FROM openjdk:17-slim
COPY target/movie-ticket-platform-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: movie-ticket-api
spec:
  replicas: 3  # Start with 3 instances
  selector:
    matchLabels:
      app: movie-ticket-api
  template:
    metadata:
      labels:
        app: movie-ticket-api
    spec:
      containers:
      - name: movie-ticket-api
        image: movie-ticket-platform:1.0.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

## Capacity Planning
- **Phase 1 (Single Instance)**: ~1000 concurrent users
- **Phase 2 (Vertical Scaling)**: ~5000 concurrent users
- **Phase 3 (Horizontal Scaling)**: ~50,000 concurrent users
- **Phase 4 (Microservices)**: ~100,000+ concurrent users

