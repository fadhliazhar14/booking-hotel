# Railway Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying the Booking Hotel application to Railway cloud platform with full production readiness.

## Prerequisites

- Railway account ([railway.app](https://railway.app))
- GitHub repository with the application code
- MySQL database service (Railway MySQL or external)
- Redis service (Railway Redis or external)
- OAuth2 Authorization Server (deployed separately)

## Railway Services Setup

### 1. Database Service (MySQL)

```bash
# Create MySQL service in Railway
railway add mysql

# Note the generated connection details:
# - MYSQL_URL
# - MYSQL_HOST
# - MYSQL_PORT
# - MYSQL_USER
# - MYSQL_PASSWORD
# - MYSQL_DATABASE
```

### 2. Redis Service

```bash
# Create Redis service in Railway
railway add redis

# Note the generated connection details:
# - REDIS_URL
# - REDIS_HOST
# - REDIS_PORT
# - REDIS_PASSWORD
```

### 3. Application Service

```bash
# Create new project from GitHub
railway login
railway link [project-id]

# Or create new project
railway new
```

## Environment Variables Configuration

Set the following environment variables in Railway dashboard:

### Required Variables

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database Configuration
MYSQL_CURRENT_URL=jdbc:mysql://[MYSQL_HOST]:[MYSQL_PORT]/[MYSQL_DATABASE]?useSSL=true&requireSSL=false&serverTimezone=UTC
MYSQL_CURRENT_USERNAME=[MYSQL_USER]
MYSQL_CURRENT_PASSWORD=[MYSQL_PASSWORD]

# Redis Configuration
REDIS_HOST=[REDIS_HOST]
REDIS_PORT=[REDIS_PORT]
REDIS_PASSWORD=[REDIS_PASSWORD]

# OAuth2 Configuration
OAUTH2_ISSUER_URI=https://your-auth-server.com/auth/realms/booking
OAUTH2_JWK_SET_URI=https://your-auth-server.com/auth/realms/booking/.well-known/jwks.json

# Server Configuration
SERVER_PORT=${PORT}
```

### Optional Variables (with defaults)

```bash
# Database Pool
DB_POOL_SIZE=10
DB_POOL_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# JPA Configuration
JPA_HIBERNATE_DDL_AUTO=validate
JPA_SHOW_SQL=false

# Flyway
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=true

# Cache Configuration
CACHE_TYPE=redis
CACHE_TTL=300000

# Logging
APP_LOG_LEVEL=INFO
SECURITY_LOG_LEVEL=WARN

# API Documentation
SWAGGER_UI_ENABLED=true

# Application
APP_VERSION=1.0.0
PAGINATION_DEFAULT_SIZE=20
PAGINATION_MAX_SIZE=100

# CORS (adjust for your frontend domains)
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS,PATCH
CORS_ALLOWED_HEADERS=Content-Type,Authorization,X-Requested-With
```

## Deployment Process

### 1. Automatic Deployment

Railway automatically deploys when you push to the connected GitHub repository branch.

```bash
# Push your code
git add .
git commit -m "Deploy to Railway"
git push origin main
```

### 2. Manual Deployment

```bash
# Deploy using Railway CLI
railway up

# Deploy specific service
railway up --service booking-hotel
```

### 3. Build Process

Railway will:
1. Use the `Dockerfile` to build the application
2. Install dependencies via Maven
3. Build the JAR file
4. Create optimized runtime container
5. Start the application with environment variables

## Health Checks

Railway automatically monitors application health using:

- **Health Check Endpoint**: `GET /actuator/health`
- **Port Check**: Application responds on `$PORT`
- **Process Check**: Java process is running

### Custom Health Check

The application provides comprehensive health information:

```bash
curl https://your-app.railway.app/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1073741824,
        "free": 536870912,
        "threshold": 10485760
      }
    }
  }
}
```

## Performance Optimization

### 1. JVM Tuning

The Dockerfile includes optimized JVM settings:

```bash
# Memory settings
JVM_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

### 2. Database Connection Pool

```properties
# Optimized for Railway resources
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### 3. Caching Strategy

```properties
# Multi-tier Redis caching
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
```

## Monitoring and Logging

### 1. Application Metrics

```bash
curl https://your-app.railway.app/actuator/metrics
```

### 2. Prometheus Metrics

```bash
curl https://your-app.railway.app/actuator/prometheus
```

### 3. Log Aggregation

Railway automatically captures application logs:

```bash
# View logs via CLI
railway logs

# Follow logs in real-time
railway logs --follow
```

## Security Configuration

### 1. HTTPS Enforcement

Railway automatically provides HTTPS for all applications.

### 2. Environment Variables Security

- Never commit sensitive data to Git
- Use Railway's environment variable management
- Rotate secrets regularly

### 3. Database Security

- Use connection encryption (`useSSL=true`)
- Implement connection pooling limits
- Regular security updates

## Scaling Configuration

### 1. Horizontal Scaling

```json
// railway.json
{
  "deploy": {
    "numReplicas": 2
  }
}
```

### 2. Resource Limits

Railway automatically manages resources, but you can optimize:

- **CPU**: Optimized for I/O operations
- **Memory**: 512MB default (adjustable)
- **Storage**: Ephemeral filesystem

## Testing Deployment

### 1. Health Check

```bash
curl https://your-app.railway.app/actuator/health
```

### 2. API Endpoints

```bash
# Test authentication (requires valid JWT)
curl -H "Authorization: Bearer YOUR_TOKEN" \
     https://your-app.railway.app/api/v1/rooms

# Test Swagger UI
open https://your-app.railway.app/swagger-ui.html
```

### 3. Database Connectivity

```bash
# Check database migrations
curl https://your-app.railway.app/actuator/flyway
```

## Troubleshooting

### Common Issues

1. **Application Won't Start**
   - Check environment variables
   - Verify database connectivity
   - Review application logs

2. **Database Connection Errors**
   - Verify MySQL service is running
   - Check connection string format
   - Test database credentials

3. **Redis Connection Issues**
   - Verify Redis service status
   - Check Redis host/port configuration
   - Test Redis authentication

4. **Memory Issues**
   - Adjust JVM heap size
   - Monitor memory usage via metrics
   - Consider scaling up resources

### Log Analysis

```bash
# Filter error logs
railway logs | grep ERROR

# Search for specific patterns
railway logs | grep "database"
railway logs | grep "redis"
```

## Backup and Recovery

### 1. Database Backup

```bash
# Railway automatically backs up MySQL
# Access backups via Railway dashboard
```

### 2. Application Configuration

- Store all configuration in environment variables
- Version control all code changes
- Document all manual changes

## Production Checklist

- [ ] All environment variables configured
- [ ] Database migrations applied
- [ ] Redis connectivity verified
- [ ] OAuth2 integration tested
- [ ] Health checks passing
- [ ] HTTPS certificate active
- [ ] Monitoring and logging configured
- [ ] Load testing completed
- [ ] Backup strategy implemented
- [ ] Documentation updated

## Support

- **Railway Status**: [status.railway.app](https://status.railway.app)
- **Railway Discord**: [Community Support](https://discord.gg/railway)
- **Application Logs**: `railway logs`
- **Health Monitoring**: `/actuator/health`

---

*Last updated: January 2024*
*Railway Platform Version: Latest*