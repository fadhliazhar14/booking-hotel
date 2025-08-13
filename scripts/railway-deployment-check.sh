#!/bin/bash

# Railway Deployment Readiness Check Script
# This script validates that the application is ready for Railway deployment

set -e

echo "🚀 Railway Deployment Readiness Check"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
    else
        echo -e "${RED}✗${NC} $2"
        return 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

echo "1. Checking build configuration..."

# Check if JAR file exists
if [ -f "target/booking-hotel-0.0.1-SNAPSHOT.jar" ]; then
    print_status 0 "Spring Boot JAR file exists"
    
    # Check JAR file size (should be reasonable for Railway)
    jar_size=$(stat -f%z target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null || stat -c%s target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null)
    if [ $jar_size -lt 100000000 ]; then  # Less than 100MB
        print_status 0 "JAR file size is acceptable (${jar_size} bytes)"
    else
        print_warning "JAR file is large (${jar_size} bytes) - may impact deployment speed"
    fi
else
    print_status 1 "Spring Boot JAR file missing - run 'mvn clean package'"
    exit 1
fi

echo ""
echo "2. Checking Railway configuration files..."

# Check Dockerfile
if [ -f "Dockerfile" ]; then
    print_status 0 "Dockerfile exists"
    
    # Validate Dockerfile content
    if grep -q "FROM.*openjdk:17" Dockerfile; then
        print_status 0 "Dockerfile uses Java 17"
    else
        print_status 1 "Dockerfile should use Java 17"
    fi
    
    if grep -q "EXPOSE" Dockerfile; then
        print_status 0 "Dockerfile exposes port"
    else
        print_status 1 "Dockerfile should expose port"
    fi
    
    if grep -q "HEALTHCHECK" Dockerfile; then
        print_status 0 "Dockerfile includes health check"
    else
        print_warning "Consider adding health check to Dockerfile"
    fi
else
    print_status 1 "Dockerfile missing"
fi

# Check railway.json
if [ -f "railway.json" ]; then
    print_status 0 "railway.json configuration exists"
else
    print_warning "railway.json configuration missing (optional but recommended)"
fi

echo ""
echo "3. Checking application configuration..."

# Check if production profile exists
if [ -f "src/main/resources/application-prod.properties" ]; then
    print_status 0 "Production profile configuration exists"
    
    # Check for externalized configuration
    if grep -q '\${.*}' src/main/resources/application-prod.properties; then
        print_status 0 "Configuration uses environment variables"
    else
        print_warning "Consider using environment variables for production config"
    fi
else
    print_status 1 "Production profile configuration missing"
fi

# Check main application.properties
if [ -f "src/main/resources/application.properties" ]; then
    print_status 0 "Main application configuration exists"
    
    # Check for Railway compatibility features
    if grep -q "management.endpoints.web.exposure" src/main/resources/application.properties; then
        print_status 0 "Actuator endpoints configured for health checks"
    else
        print_status 1 "Actuator endpoints should be configured"
    fi
    
    if grep -q "server.port=\${PORT" src/main/resources/application.properties; then
        print_status 0 "Port configuration is Railway-compatible"
    else
        print_warning "Consider using \${PORT} environment variable for server port"
    fi
else
    print_status 1 "Main application configuration missing"
fi

echo ""
echo "4. Checking database configuration..."

# Check for Flyway migrations
if [ -d "src/main/resources/db/migration" ] && [ "$(ls -A src/main/resources/db/migration)" ]; then
    print_status 0 "Database migration scripts exist"
    
    migration_count=$(ls -1 src/main/resources/db/migration/*.sql 2>/dev/null | wc -l)
    print_status 0 "Found $migration_count migration files"
else
    print_warning "No database migration scripts found"
fi

echo ""
echo "5. Checking dependencies and security..."

# Check pom.xml for Railway-compatible dependencies
if [ -f "pom.xml" ]; then
    print_status 0 "Maven configuration exists"
    
    # Check Spring Boot version
    if grep -q "<version>3\." pom.xml; then
        print_status 0 "Uses Spring Boot 3.x (compatible with Java 17)"
    else
        print_warning "Consider upgrading to Spring Boot 3.x"
    fi
    
    # Check for security dependencies
    if grep -q "spring-boot-starter-security\|spring-security-oauth2" pom.xml; then
        print_status 0 "Security dependencies configured"
    else
        print_warning "Consider adding security dependencies"
    fi
    
    # Check for monitoring dependencies
    if grep -q "spring-boot-starter-actuator" pom.xml; then
        print_status 0 "Monitoring/health check dependencies configured"
    else
        print_status 1 "Actuator dependency required for Railway health checks"
    fi
else
    print_status 1 "Maven configuration missing"
fi

echo ""
echo "6. Checking environment variable requirements..."

# List of critical environment variables for Railway
critical_env_vars=(
    "MYSQL_CURRENT_URL"
    "MYSQL_CURRENT_USERNAME" 
    "MYSQL_CURRENT_PASSWORD"
    "OAUTH2_ISSUER_URI"
    "REDIS_HOST"
    "REDIS_PORT"
)

echo "Critical environment variables that need to be set in Railway:"
for var in "${critical_env_vars[@]}"; do
    echo "  - $var"
done

echo ""
echo "7. Performance and resource optimization..."

# Check for caching configuration
if grep -q "spring.cache" src/main/resources/application*.properties; then
    print_status 0 "Caching configuration detected"
else
    print_warning "Consider adding caching for better performance"
fi

# Check for connection pool configuration
if grep -q "spring.datasource.hikari" src/main/resources/application*.properties; then
    print_status 0 "Database connection pool configured"
else
    print_warning "Consider configuring database connection pool"
fi

echo ""
echo "8. Documentation and deployment readiness..."

# Check for deployment documentation
if [ -f "docs/railway-deployment.md" ]; then
    print_status 0 "Railway deployment documentation exists"
else
    print_warning "Consider creating deployment documentation"
fi

# Check for API documentation
if grep -q "springdoc\|swagger" src/main/resources/application*.properties; then
    print_status 0 "API documentation configured"
else
    print_warning "Consider enabling API documentation"
fi

echo ""
echo "======================================"
echo "🎯 Railway Deployment Readiness Summary"
echo "======================================"

echo ""
echo "✅ Ready for deployment if all checks passed"
echo "⚠️  Review warnings for optimization opportunities"
echo "❌ Fix any failed checks before deploying"

echo ""
echo "Next steps for Railway deployment:"
echo "1. Create Railway project: railway new"
echo "2. Add MySQL service: railway add mysql"
echo "3. Add Redis service: railway add redis"
echo "4. Configure environment variables in Railway dashboard"
echo "5. Deploy: railway up"

echo ""
echo "📊 Build Info:"
echo "- JAR Size: $(stat -f%z target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null || stat -c%s target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null) bytes"
echo "- Java Version: $(java -version 2>&1 | head -n 1)"
echo "- Maven Version: $(mvn -version 2>&1 | head -n 1)"

echo ""
echo "🔗 Useful links:"
echo "- Railway Dashboard: https://railway.app/dashboard"
echo "- Railway CLI: https://docs.railway.app/develop/cli"
echo "- Application Health: /actuator/health"
echo "- API Documentation: /swagger-ui.html"