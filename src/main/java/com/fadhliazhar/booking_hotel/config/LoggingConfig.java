package com.fadhliazhar.booking_hotel.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Logging configuration for request tracing and monitoring
 * Adds correlation IDs and request/response logging
 */
@Slf4j
@Configuration
public class LoggingConfig implements WebMvcConfigurer {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLoggingInterceptor());
    }

    /**
     * Interceptor for logging HTTP requests and responses with correlation tracking
     */
    public static class RequestLoggingInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            long startTime = System.currentTimeMillis();
            String requestId = UUID.randomUUID().toString();
            
            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Add to MDC for structured logging
            MDC.put(CORRELATION_ID_KEY, correlationId);
            MDC.put(REQUEST_ID_KEY, requestId);
            
            // Add correlation ID to response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Store start time for performance monitoring
            request.setAttribute("startTime", startTime);
            
            // Log incoming request
            log.info("HTTP Request - Method: {}, URI: {}, RemoteAddr: {}, UserAgent: {}", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"));
            
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            try {
                long startTime = (Long) request.getAttribute("startTime");
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                String level = "INFO";
                if (response.getStatus() >= 400) {
                    level = "WARN";
                }
                if (response.getStatus() >= 500) {
                    level = "ERROR";
                }
                
                // Log response details
                if ("ERROR".equals(level)) {
                    log.error("HTTP Response - Method: {}, URI: {}, Status: {}, Duration: {}ms", 
                            request.getMethod(), 
                            request.getRequestURI(), 
                            response.getStatus(),
                            duration);
                } else if ("WARN".equals(level)) {
                    log.warn("HTTP Response - Method: {}, URI: {}, Status: {}, Duration: {}ms", 
                            request.getMethod(), 
                            request.getRequestURI(), 
                            response.getStatus(),
                            duration);
                } else {
                    log.info("HTTP Response - Method: {}, URI: {}, Status: {}, Duration: {}ms", 
                            request.getMethod(), 
                            request.getRequestURI(), 
                            response.getStatus(),
                            duration);
                }
                
                // Log slow requests
                if (duration > 1000) { // More than 1 second
                    log.warn("Slow Request Detected - Method: {}, URI: {}, Duration: {}ms", 
                            request.getMethod(), request.getRequestURI(), duration);
                }
                
                // Log exception if present
                if (ex != null) {
                    log.error("Request completed with exception - Method: {}, URI: {}", 
                            request.getMethod(), request.getRequestURI(), ex);
                }
                
            } finally {
                // Clean up MDC to prevent memory leaks
                MDC.remove(CORRELATION_ID_KEY);
                MDC.remove(REQUEST_ID_KEY);
                MDC.remove(USER_ID_KEY);
            }
        }

        /**
         * Get the real client IP address, accounting for proxies and load balancers
         */
        private String getClientIpAddress(HttpServletRequest request) {
            String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP", 
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
            };
            
            for (String header : headers) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    // Handle multiple IPs (X-Forwarded-For can contain multiple IPs)
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    return ip;
                }
            }
            
            return request.getRemoteAddr();
        }
    }

    /**
     * Utility class for adding user context to logs
     */
    public static class LoggingUtils {
        
        public static void addUserContext(String userId) {
            if (userId != null && !userId.trim().isEmpty()) {
                MDC.put(USER_ID_KEY, userId);
            }
        }
        
        public static void removeUserContext() {
            MDC.remove(USER_ID_KEY);
        }
        
        public static String getCorrelationId() {
            return MDC.get(CORRELATION_ID_KEY);
        }
        
        public static String getRequestId() {
            return MDC.get(REQUEST_ID_KEY);
        }
        
        public static void logBusinessEvent(String event, String description, Object... args) {
            log.info("Business Event: {} - Description: {} - Args: {}", event, description, args);
        }
        
        public static void logPerformanceMetric(String operation, long duration) {
            log.info("Performance Metric - Operation: {}, Duration: {}ms", operation, duration);
        }
        
        public static void logSecurityEvent(String event, String details) {
            log.warn("Security Event: {} - Details: {}", event, details);
        }
    }
}