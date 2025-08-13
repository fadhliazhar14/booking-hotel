# OAuth2 Client Credentials Flow Integration Guide

## Overview

Booking Hotel application implementasi **OAuth2 Client Credentials Flow** sebagai Resource Server yang terintegrasi dengan AuthServer untuk validasi JWT token. Flow ini adalah **machine-to-machine authentication** tanpa melibatkan user credentials.

## 🔐 OAuth2 Client Credentials Flow Mechanism

### **Flow Architecture**
```
┌─────────────────┐    1. Request Token     ┌─────────────────┐
│                 │  ──────────────────────▶ │                 │
│   Client App    │                          │   AuthServer    │
│   (Frontend)    │  ◀──────────────────────  │  (OAuth2 Auth)  │
│                 │    2. JWT Access Token   │                 │
└─────────────────┘                          └─────────────────┘
         │                                            │
         │                                            │
         │ 3. API Request + JWT Token                 │
         ▼                                            │
┌─────────────────┐    4. Token Validation   ┌───────▼─────────┐
│                 │  ──────────────────────▶ │                 │
│ Booking Hotel   │                          │   AuthServer    │
│ (Resource       │  ◀──────────────────────  │ (Token         │
│  Server)        │    5. Token Valid/Claims │  Validation)    │
└─────────────────┘                          └─────────────────┘
```

### **Step-by-Step Flow**

**1. Client Request Token (Client Credentials Grant)**
```bash
POST /realms/booking-hotel/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=booking-hotel-client
&client_secret=your-client-secret
&scope=booking-read booking-write
```

**2. AuthServer Response**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "booking-read booking-write"
}
```

**3. Client API Request**
```bash
GET /api/v1/rooms
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**4. BookingHotel Token Validation**
- BookingHotel receives request with JWT token
- Validates token signature using JWK Set from AuthServer
- Extracts claims (scope, authorities) from token
- Authorizes request based on scopes/roles

**5. API Response**
```json
{
  "status": 200,
  "message": "Success",
  "data": [...]
}
```

## 🏗️ Current Implementation

### **Resource Server Configuration**

[`SecurityConfig.java`](../src/main/java/com/fadhliazhar/booking_hotel/config/SecurityConfig.java) sudah dikonfigurasi untuk:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

### **JWT Token Configuration**

**Development (application-dev.properties):**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/booking-hotel
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/booking-hotel/.well-known/jwks.json
```

**Production (application-prod.properties):**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${OAUTH2_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${OAUTH2_JWK_SET_URI}
```

### **Authority Mapping**

JWT token claims akan dimapping ke Spring Security authorities:

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthorityPrefix("ROLE_");
    authoritiesConverter.setAuthoritiesClaimName("roles");
    
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return jwtConverter;
}
```

## 🔧 AuthServer Configuration Requirements

Untuk Client Credentials Flow, AuthServer harus dikonfigurasi:

### **1. Client Registration**
```json
{
  "clientId": "booking-hotel-client",
  "clientSecret": "your-secret-key",
  "grantTypes": ["client_credentials"],
  "scopes": ["booking-read", "booking-write", "booking-admin"],
  "accessTokenValidity": 3600
}
```

### **2. Scope Definition**
- `booking-read`: Read access to bookings and rooms
- `booking-write`: Create/update bookings and rooms  
- `booking-admin`: Administrative access (delete, user management)

### **3. JWT Claims**
Expected JWT token claims:
```json
{
  "iss": "http://localhost:8080/realms/booking-hotel",
  "sub": "booking-hotel-client", 
  "aud": ["booking-hotel"],
  "exp": 1640995200,
  "iat": 1640991600,
  "scope": "booking-read booking-write",
  "roles": ["USER", "ADMIN"],
  "client_id": "booking-hotel-client"
}
```

## 🧪 Testing OAuth2 Integration

### **1. Prerequisites**
- AuthServer running on `http://localhost:8080`
- Booking Hotel running on `http://localhost:8081` 
- MySQL database `booking_hotel_dev` available
- Redis server running on `localhost:6379`

### **2. Get Access Token**
```bash
#!/bin/bash
# get-token.sh

TOKEN_RESPONSE=$(curl -s -X POST \
  http://localhost:8080/realms/booking-hotel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=booking-hotel-client" \
  -d "client_secret=your-client-secret" \
  -d "scope=booking-read booking-write")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')
echo "Access Token: $ACCESS_TOKEN"
```

### **3. Test API Endpoints**
```bash
#!/bin/bash
# test-api.sh

ACCESS_TOKEN="your-jwt-token-here"

# Test public endpoint (should work without token)
curl -X GET http://localhost:8081/actuator/health

# Test protected endpoint (requires token)
curl -X GET http://localhost:8081/api/v1/rooms \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Test admin endpoint (requires admin scope)
curl -X POST http://localhost:8081/api/v1/rooms/create \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roomNumber": 101,
    "roomPrice": 150.00,
    "adultCapacity": 2,
    "childrenCapacity": 1
  }'
```

### **4. Expected Behaviors**

**✅ Valid Token with Correct Scope:**
```bash
HTTP/1.1 200 OK
{
  "status": 200,
  "message": "Success",
  "data": [...]
}
```

**❌ No Token:**
```bash
HTTP/1.1 401 Unauthorized
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**❌ Invalid Token:**
```bash
HTTP/1.1 401 Unauthorized
{
  "timestamp": "2024-01-15T10:30:00.000Z", 
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT token"
}
```

**❌ Insufficient Scope:**
```bash
HTTP/1.1 403 Forbidden
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden", 
  "message": "Insufficient scope for this resource"
}
```

## 📋 Role-Based Access Control

### **Controller Method Security**
```java
@RestController
@RequestMapping("/api/v1/bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_booking-read')")
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getAllBookings() {
        // Only accessible with booking-read scope
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SCOPE_booking-write')")
    public ResponseEntity<BookingResponseDTO> createBooking() {
        // Only accessible with booking-write scope
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking() {
        // Only accessible with ADMIN role
    }
}
```

### **Scope vs Role Mapping**
- **Scopes**: Define what the client can access (`booking-read`, `booking-write`)
- **Roles**: Define user permissions (`USER`, `ADMIN`)
- **Authorities**: Spring Security format (`SCOPE_booking-read`, `ROLE_ADMIN`)

## 🔍 Debugging OAuth2 Issues

### **Enable Debug Logging**
```properties
# application-dev.properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
```

### **Common Issues & Solutions**

**1. Token Validation Errors**
- ✅ Verify `issuer-uri` matches AuthServer exactly
- ✅ Check `jwk-set-uri` is accessible
- ✅ Ensure clock sync between servers

**2. Authority Mapping Issues** 
- ✅ Check JWT claims structure
- ✅ Verify `authoritiesClaimName` configuration
- ✅ Test authority prefix settings

**3. CORS Issues**
- ✅ Configure CORS for OPTIONS requests
- ✅ Allow Authorization header
- ✅ Check allowed origins match frontend

## ✅ Integration Checklist

- [ ] AuthServer configured with Client Credentials grant
- [ ] JWT issuer URI matches between services  
- [ ] JWK Set URI accessible from BookingHotel
- [ ] Scopes properly defined and mapped
- [ ] Role-based access control tested
- [ ] Token expiration handling implemented
- [ ] CORS configuration for frontend integration
- [ ] Error responses consistent and informative
- [ ] Logging configured for troubleshooting

---

**Note**: This implementation follows **OAuth2 RFC 6749 Client Credentials Grant** specification untuk machine-to-machine authentication without user interaction.