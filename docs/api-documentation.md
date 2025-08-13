# Booking Hotel API Documentation

## Overview

The Booking Hotel API is a comprehensive hotel management system built with Spring Boot 3.5.4, featuring OAuth2 security, Redis caching, and enterprise-grade architecture patterns.

## Quick Start

### Authentication

All API endpoints require authentication via JWT Bearer token obtained from the OAuth2 authorization server.

```bash
# Example request with authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     https://your-api-url/api/v1/bookings
```

### API Base URL

- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://booking-hotel-production.up.railway.app/api/v1`

### Interactive Documentation

- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/api-docs`

## Core Features

### 🔐 Security & Authentication

- **OAuth2 Resource Server** with JWT token validation
- **Role-based Access Control**: USER and ADMIN roles
- **Integration with AuthServer** for centralized authentication
- **Automatic token validation** on all protected endpoints

### 🏨 Booking Management

- **Complete booking lifecycle**: BOOKED → CHECKED_IN → CHECKED_OUT
- **Room availability checking** with conflict prevention
- **User access control**: Users can only access their own bookings
- **Admin override**: Admins can manage all bookings

### 🛏️ Room Management

- **Room CRUD operations** with validation
- **Availability checking** based on dates and capacity
- **Dynamic pricing support**
- **Capacity validation** (adults + children)

### 📊 Advanced Features

- **Pagination & Sorting**: All list endpoints support pagination
- **Search functionality**: Filter across multiple fields
- **Redis Caching**: Multi-tier caching with TTL strategies
- **Error handling**: Consistent error response format
- **Audit logging**: Request correlation IDs and tracing

## API Endpoints Summary

### Booking Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/bookings` | Get paginated bookings | Yes |
| GET | `/bookings/{id}` | Get booking by ID | Yes |
| POST | `/bookings/create` | Create new booking | Yes |
| PUT | `/bookings/{id}` | Update booking | Yes |
| PATCH | `/bookings/{id}` | Update booking status | Yes |
| DELETE | `/bookings/{id}` | Delete booking | Yes |

### Room Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/rooms` | Get all rooms | Yes |
| GET | `/rooms/{id}` | Get room by ID | Yes |
| POST | `/rooms/available-room` | Find available room | Yes |
| POST | `/rooms/create` | Create new room | Admin |
| PUT | `/rooms/{id}` | Update room | Admin |
| DELETE | `/rooms/{id}` | Delete room | Admin |

## Pagination Parameters

All list endpoints support these query parameters:

- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field (default: id)
- `direction`: Sort direction (asc/desc, default: desc)
- `search`: Search term for filtering

### Example Pagination Request

```bash
GET /api/v1/bookings?page=0&size=10&sort=checkedInDate&direction=desc&search=John
```

## Response Format

### Success Response

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    // Response data here
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

### Paginated Response

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/v1/bookings",
  "correlationId": "abc-123-def"
}
```

## Business Rules

### Booking Rules

1. **Date Validation**:
   - Check-in date cannot be in the past
   - Check-out date must be after check-in date

2. **Capacity Validation**:
   - Adult capacity must be at least 1
   - Children capacity cannot be negative
   - Total guests cannot exceed room capacity

3. **Availability**:
   - Room must be available for requested dates
   - No overlapping bookings allowed

4. **Status Transitions**:
   - BOOKED → CHECKED_IN ✅
   - CHECKED_IN → CHECKED_OUT ✅
   - BOOKED → CANCELLED (Admin only) ✅
   - Cannot transition from terminal states ❌

### Room Rules

1. **Room Numbers**: Must be unique across the system
2. **Pricing**: Must be greater than 0
3. **Capacity**: Adult capacity ≥ 1, children capacity ≥ 0
4. **Deletion**: Cannot delete rooms with active bookings

## Caching Strategy

The API implements a multi-tier Redis caching strategy:

- **Short-term (5 min)**: Frequently changing data
- **Medium-term (30 min)**: Semi-static data
- **Long-term (2 hours)**: Static reference data

### Cached Endpoints

- Room data: 30-minute TTL
- Booking lookups: 5-minute TTL
- Availability searches: 5-minute TTL

## Error Codes

| HTTP Status | Description | Example Scenario |
|-------------|-------------|------------------|
| 200 | Success | Request completed successfully |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation errors, business rule violations |
| 401 | Unauthorized | Invalid or missing JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource conflicts (room not available) |
| 500 | Internal Server Error | System error |

## Rate Limiting

- **Default**: 100 requests per minute per IP
- **Authenticated**: 500 requests per minute per user
- **Admin**: 1000 requests per minute

## Health Monitoring

### Health Check Endpoint

```bash
GET /actuator/health
```

### Metrics Endpoint

```bash
GET /actuator/metrics
```

### Available Metrics

- Database connection pool status
- Redis cache hit/miss ratios
- API response times
- Authentication success/failure rates

## Development Setup

### Prerequisites

- Java 17+
- MySQL 8.0+
- Redis 7+
- OAuth2 Authorization Server

### Environment Variables

```bash
# Database
MYSQL_CURRENT_URL=jdbc:mysql://localhost:3306/booking_hotel
MYSQL_CURRENT_USERNAME=your_username
MYSQL_CURRENT_PASSWORD=your_password

# OAuth2
OAUTH2_ISSUER_URI=http://localhost:8080/auth/realms/booking

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Running the Application

```bash
# Clone the repository
git clone https://github.com/your-username/booking-hotel.git
cd booking-hotel

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

## Deployment

### Railway Deployment

The application is configured for Railway cloud deployment with:

- Automatic environment variable injection
- Health check endpoints
- Graceful shutdown handling
- Production-ready configurations

### Docker Support

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/booking-hotel-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Support

- **API Issues**: Create an issue in the GitHub repository
- **Documentation**: Check the interactive Swagger UI
- **Performance**: Monitor via `/actuator/metrics`

---

*Last updated: January 2024*
*API Version: 1.0.0*