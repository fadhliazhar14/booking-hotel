package com.fadhliazhar.booking_hotel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for comprehensive API documentation
 * Includes OAuth2 security, pagination, caching, and error handling documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Hotel Booking Management System with OAuth2 Security}")
    private String appDescription;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .components(getComponents())
                .addSecurityItem(getSecurityRequirement());
    }

    private Info getApiInfo() {
        return new Info()
                .title("Booking Hotel API")
                .version(appVersion)
                .description(buildDescription())
                .contact(new Contact()
                        .name("Fadhli Azhar")
                        .email("fadhli@example.com")
                        .url("https://github.com/fadhliazhar"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private String buildDescription() {
        return """
                # Hotel Booking Management System
                
                A comprehensive hotel booking management system built with Spring Boot 3.5.4 and modern enterprise patterns.
                
                ## Key Features
                
                ### 🔐 Security & Authentication
                - **OAuth2 Resource Server** with JWT token validation
                - **Role-based Access Control** (USER, ADMIN)
                - **Integration with AuthServer** for centralized authentication
                
                ### 🏨 Core Booking Functionality  
                - **Room Management** with availability checking
                - **Booking Lifecycle** management (BOOKED → CHECKED_IN → CHECKED_OUT)
                - **Dynamic Amenity & Service Types** (converted from enums to entities)
                - **Conflict Prevention** for overlapping bookings
                
                ### 📊 Advanced Features
                - **Pagination, Sorting & Search** on all list endpoints
                - **Redis Caching** with multi-tier TTL strategies
                - **Comprehensive Error Handling** with consistent API responses
                - **Audit Logging** with correlation IDs and request tracing
                - **Health Monitoring** with Actuator and custom metrics
                
                ### 🚀 Performance & Scalability
                - **Database Query Optimization** with JPA Specifications
                - **Connection Pooling** with HikariCP
                - **Async Processing** capabilities
                - **Railway Cloud Deployment** ready configuration
                
                ## API Usage
                
                All endpoints require valid JWT token except public health checks.
                Use the authorize button below to authenticate with your OAuth2 provider.
                
                ### Pagination Format
                All list endpoints support pagination with these query parameters:
                - `page`: Page number (0-based, default: 0)
                - `size`: Page size (default: 20, max: 100)  
                - `sort`: Sort field (default: id)
                - `direction`: Sort direction (asc/desc, default: asc)
                - `search`: Search term for filtering
                
                ### Error Response Format
                All errors follow consistent format:
                ```json
                {
                  "timestamp": "2024-01-15T10:30:00.000Z",
                  "status": 400,
                  "error": "Bad Request", 
                  "message": "Detailed error message",
                  "path": "/api/bookings",
                  "correlationId": "abc-123-def"
                }
                ```
                """;
    }

    private List<Server> getServers() {
        return List.of(
                new Server().url(serverUrl).description("Development Server"),
                new Server().url("https://booking-hotel-production.up.railway.app").description("Production Server (Railway)")
        );
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token obtained from OAuth2 Authorization Server"))
                .addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("OAuth2 Authorization Code Flow")
                        .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                                .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                                        .authorizationUrl("${oauth2.authorization-server-url}/auth")
                                        .tokenUrl("${oauth2.authorization-server-url}/token")
                                        .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                .addString("read", "Read access to booking data")
                                                .addString("write", "Write access to booking data")
                                                .addString("admin", "Admin access to all operations")))));
    }

    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement()
                .addList("bearerAuth")
                .addList("oauth2", List.of("read", "write"));
    }
}