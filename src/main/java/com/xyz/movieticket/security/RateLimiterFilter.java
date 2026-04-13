package com.xyz.movieticket.security;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    // In-memory rate limiter for simple cases (fallback)
    private final Map<String, RateLimiter> localRateLimiters = new ConcurrentHashMap<>();

    @Value("${app.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${app.rate-limiting.default-per-minute:60}")
    private int defaultPermitsPerMinute;

    @Value("${app.rate-limiting.booking-per-minute:10}")
    private int bookingPermitsPerMinute;

    @Value("${app.rate-limiting.theatre-admin-per-minute:30}")
    private int theatreAdminPermitsPerMinute;

    @Value("${app.rate-limiting.use-redis:false}")
    private boolean useRedis;

    public RateLimiterFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip rate limiting for certain endpoints
        if (!rateLimitingEnabled || shouldSkipRateLimiting(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        String endpointType = getEndpointType(request);
        int permitsPerMinute = getPermitsForEndpoint(endpointType);

        boolean allowed;

        if (useRedis) {
            // Use Redis for distributed rate limiting
            allowed = checkRedisRateLimit(clientId, endpointType, permitsPerMinute);
        } else {
            // Use in-memory rate limiting (single instance)
            allowed = checkLocalRateLimit(clientId, permitsPerMinute);
        }

        if (!allowed) {
            log.warn("Rate limit exceeded for client: {}, endpoint: {}", clientId, endpointType);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkLocalRateLimit(String clientId, int permitsPerMinute) {
        RateLimiter rateLimiter = localRateLimiters.computeIfAbsent(
                clientId,
                k -> RateLimiter.create(permitsPerMinute / 60.0) // Convert to permits per second
        );

        return rateLimiter.tryAcquire(1, 0, TimeUnit.SECONDS);
    }

    private boolean checkRedisRateLimit(String clientId, String endpointType, int permitsPerMinute) {
        try {
            String key = "rate_limit:" + endpointType + ":" + clientId;
            Long currentCount = redisTemplate.opsForValue().increment(key, 1);

            if (currentCount == 1) {
                // Set expiration for 1 minute
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            if (currentCount != null && currentCount > permitsPerMinute) {
                return false;
            }

            // Add headers for rate limit information
            return true;

        } catch (Exception e) {
            log.error("Error checking rate limit in Redis", e);
            // Fallback to local rate limiter if Redis fails
            return checkLocalRateLimit(clientId, permitsPerMinute);
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user from JWT token first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract user from token (simplified)
            return extractUserIdFromToken(authHeader.substring(7));
        }

        // Fallback to IP address
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // Add API key if present
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "apikey:" + apiKey;
        }

        return "ip:" + ipAddress;
    }

    private String extractUserIdFromToken(String token) {
        // Simplified extraction - in production, properly parse JWT
        try {
            // This is a placeholder - you should use JwtTokenProvider
            return "user:" + token.substring(0, Math.min(10, token.length()));
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String getEndpointType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Booking endpoints - stricter limits
        if (uri.contains("/api/v1/bookings/book") && "POST".equals(method)) {
            return "BOOKING_CREATE";
        }

        if (uri.contains("/api/v1/bookings") && "GET".equals(method)) {
            return "BOOKING_READ";
        }

        // Theatre admin endpoints
        if (uri.contains("/api/v1/theatres") &&
                (request.getUserPrincipal() != null &&
                        request.isUserInRole("THEATRE_ADMIN"))) {
            return "THEATRE_ADMIN";
        }

        // Search endpoints - higher limits
        if (uri.contains("/api/v1/bookings/theatres/by-movie") ||
                uri.contains("/api/v1/movies") ||
                uri.contains("/search")) {
            return "SEARCH";
        }

        // Authentication endpoints
        if (uri.contains("/api/v1/auth")) {
            return "AUTH";
        }

        // Default
        return "DEFAULT";
    }

    private int getPermitsForEndpoint(String endpointType) {
        switch (endpointType) {
            case "BOOKING_CREATE":
                return bookingPermitsPerMinute;
            case "THEATRE_ADMIN":
                return theatreAdminPermitsPerMinute;
            case "SEARCH":
                return 120; // Higher limit for searches
            case "AUTH":
                return 20; // Stricter limit for auth endpoints
            case "BOOKING_READ":
                return 100;
            default:
                return defaultPermitsPerMinute;
        }
    }

    private boolean shouldSkipRateLimiting(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // Skip rate limiting for health checks and metrics
        if (uri.startsWith("/actuator/health") ||
                uri.startsWith("/actuator/info") ||
                uri.startsWith("/health")) {
            return true;
        }

        // Skip for static resources
        if (uri.startsWith("/swagger-ui/") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/webjars/")) {
            return true;
        }

        // Skip for webhooks (they come from trusted sources)
        if (uri.contains("/webhook")) {
            return true;
        }

        return false;
    }

    // Method to add rate limit headers to response
    private void addRateLimitHeaders(HttpServletResponse response,
                                     String clientId,
                                     int remaining,
                                     long resetTime) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(defaultPermitsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
    }
}