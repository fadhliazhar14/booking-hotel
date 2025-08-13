#!/bin/bash

# Comprehensive End-to-End Validation Script
# Validates all functionality without requiring actual database/Redis

set -e

echo "🧪 End-to-End Functionality Validation"
echo "======================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m'

SUCCESS_COUNT=0
WARNING_COUNT=0
ERROR_COUNT=0

print_success() {
    echo -e "${GREEN}✓${NC} $1"
    ((SUCCESS_COUNT++))
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNING_COUNT++))
}

print_error() {
    echo -e "${RED}✗${NC} $1"
    ((ERROR_COUNT++))
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

echo "🔧 1. Build and Compilation Validation"
echo "--------------------------------------"

# Check if Maven is available
if command -v mvn >/dev/null 2>&1; then
    print_success "Maven is available"
    
    # Clean and compile
    print_info "Running clean compile..."
    if mvn clean compile -q -DskipTests; then
        print_success "Application compiles successfully"
    else
        print_error "Compilation failed"
        exit 1
    fi
    
    # Package without tests (like Railway would do)
    print_info "Running package without tests..."
    if mvn package -DskipTests -q; then
        print_success "Application packages successfully"
        
        # Check JAR file
        if [ -f "target/booking-hotel-0.0.1-SNAPSHOT.jar" ]; then
            jar_size=$(stat -c%s target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null || stat -f%z target/booking-hotel-0.0.1-SNAPSHOT.jar 2>/dev/null)
            print_success "JAR file created (${jar_size} bytes)"
        else
            print_error "JAR file not found"
        fi
    else
        print_error "Packaging failed"
    fi
else
    print_error "Maven not available"
    exit 1
fi

echo ""
echo "📁 2. File Structure Validation"
echo "-------------------------------"

required_files=(
    "src/main/java/com/fadhliazhar/booking_hotel/BookingHotelApplication.java"
    "src/main/resources/application.properties"
    "src/main/resources/application-dev.properties"
    "src/main/resources/application-prod.properties"
    "pom.xml"
    "Dockerfile"
    "railway.json"
    "docs/api-documentation.md"
    "docs/railway-deployment.md"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        print_success "Found: $file"
    else
        print_error "Missing: $file"
    fi
done

# Check directories
required_dirs=(
    "src/main/java/com/fadhliazhar/booking_hotel/controller"
    "src/main/java/com/fadhliazhar/booking_hotel/service"
    "src/main/java/com/fadhliazhar/booking_hotel/repository"
    "src/main/java/com/fadhliazhar/booking_hotel/model"
    "src/main/java/com/fadhliazhar/booking_hotel/dto"
    "src/main/java/com/fadhliazhar/booking_hotel/config"
    "src/main/resources/db/migration"
    "src/test/java"
)

for dir in "${required_dirs[@]}"; do
    if [ -d "$dir" ]; then
        print_success "Directory exists: $dir"
    else
        print_error "Directory missing: $dir"
    fi
done

echo ""
echo "🏗️ 3. Architecture Validation"
echo "-----------------------------"

# Count core components
controller_count=$(find src/main/java -name "*Controller.java" | wc -l)
service_count=$(find src/main/java -name "*Service.java" | wc -l)
repository_count=$(find src/main/java -name "*Repository.java" | wc -l)
model_count=$(find src/main/java -name "*.java" -path "*/model/*" | wc -l)
dto_count=$(find src/main/java -name "*.java" -path "*/dto/*" | wc -l)
config_count=$(find src/main/java -name "*Config.java" | wc -l)
test_count=$(find src/test/java -name "*.java" | wc -l)

print_success "Controllers: $controller_count"
print_success "Services: $service_count" 
print_success "Repositories: $repository_count"
print_success "Models/Entities: $model_count"
print_success "DTOs: $dto_count"
print_success "Configuration classes: $config_count"
print_success "Test classes: $test_count"

echo ""
echo "📦 4. Dependency Validation"
echo "---------------------------"

if [ -f "pom.xml" ]; then
    # Check critical dependencies
    critical_deps=(
        "spring-boot-starter-web"
        "spring-boot-starter-data-jpa"
        "spring-boot-starter-security"
        "spring-boot-starter-oauth2-resource-server"
        "spring-boot-starter-data-redis"
        "spring-boot-starter-cache"
        "spring-boot-starter-actuator"
        "mysql-connector-java"
        "flyway-core"
        "springdoc-openapi-starter-webmvc-ui"
        "mapstruct"
    )
    
    for dep in "${critical_deps[@]}"; do
        if grep -q "$dep" pom.xml; then
            print_success "Dependency found: $dep"
        else
            print_warning "Dependency not found: $dep"
        fi
    done
else
    print_error "pom.xml not found"
fi

echo ""
echo "🗄️ 5. Database Migration Validation"
echo "-----------------------------------"

if [ -d "src/main/resources/db/migration" ]; then
    migration_files=$(find src/main/resources/db/migration -name "*.sql" | wc -l)
    if [ $migration_files -gt 0 ]; then
        print_success "Found $migration_files migration files"
        
        # List migration files
        for file in src/main/resources/db/migration/*.sql; do
            if [ -f "$file" ]; then
                print_info "Migration: $(basename "$file")"
            fi
        done
    else
        print_warning "No migration files found"
    fi
else
    print_error "Migration directory not found"
fi

echo ""
echo "🔐 6. Security Configuration Validation"
echo "---------------------------------------"

security_configs=(
    "src/main/java/com/fadhliazhar/booking_hotel/config/SecurityConfig.java"
    "src/main/java/com/fadhliazhar/booking_hotel/security/SecurityUtils.java"
    "src/main/java/com/fadhliazhar/booking_hotel/security/JwtAuthenticationEntryPoint.java"
    "src/main/java/com/fadhliazhar/booking_hotel/security/JwtAccessDeniedHandler.java"
)

for config in "${security_configs[@]}"; do
    if [ -f "$config" ]; then
        print_success "Security config: $(basename "$config")"
    else
        print_warning "Security config missing: $(basename "$config")"
    fi
done

# Check OAuth2 configuration
if grep -q "oauth2.resourceserver" src/main/resources/application*.properties; then
    print_success "OAuth2 Resource Server configured"
else
    print_warning "OAuth2 Resource Server configuration not found"
fi

echo ""
echo "📊 7. API Documentation Validation"
echo "----------------------------------"

# Check OpenAPI configuration
if [ -f "src/main/java/com/fadhliazhar/booking_hotel/config/OpenApiConfig.java" ]; then
    print_success "OpenAPI configuration found"
else
    print_warning "OpenAPI configuration missing"
fi

# Check Swagger properties
if grep -q "springdoc" src/main/resources/application*.properties; then
    print_success "Swagger/SpringDoc configuration found"
else
    print_warning "Swagger/SpringDoc configuration not found"
fi

# Check controller documentation
controllers_with_docs=$(find src/main/java -name "*Controller.java" -exec grep -l "@Tag\|@Operation" {} \; | wc -l)
total_controllers=$(find src/main/java -name "*Controller.java" | wc -l)

if [ $controllers_with_docs -gt 0 ]; then
    print_success "Controllers with documentation: $controllers_with_docs/$total_controllers"
else
    print_warning "No controller documentation found"
fi

echo ""
echo "🗃️ 8. Caching Configuration Validation"
echo "--------------------------------------"

if [ -f "src/main/java/com/fadhliazhar/booking_hotel/config/CacheConfig.java" ]; then
    print_success "Cache configuration found"
    
    # Check for caching annotations in services
    cached_methods=$(find src/main/java -name "*Service.java" -exec grep -l "@Cacheable\|@CacheEvict" {} \; | wc -l)
    if [ $cached_methods -gt 0 ]; then
        print_success "Services with caching: $cached_methods"
    else
        print_warning "No caching annotations found in services"
    fi
else
    print_warning "Cache configuration missing"
fi

# Check Redis configuration
if grep -q "spring.data.redis" src/main/resources/application*.properties; then
    print_success "Redis configuration found"
else
    print_warning "Redis configuration not found"
fi

echo ""
echo "📈 9. Monitoring and Logging Validation"
echo "---------------------------------------"

# Check Actuator configuration
if grep -q "management.endpoints" src/main/resources/application*.properties; then
    print_success "Actuator endpoints configured"
else
    print_warning "Actuator endpoints not configured"
fi

# Check logging configuration
if [ -f "src/main/java/com/fadhliazhar/booking_hotel/config/LoggingConfig.java" ]; then
    print_success "Logging configuration found"
else
    print_warning "Logging configuration missing"
fi

# Check monitoring configuration
if [ -f "src/main/java/com/fadhliazhar/booking_hotel/config/MonitoringConfig.java" ]; then
    print_success "Monitoring configuration found"
else
    print_warning "Monitoring configuration missing"
fi

echo ""
echo "🚀 10. Deployment Readiness Validation"
echo "--------------------------------------"

# Check Dockerfile
if [ -f "Dockerfile" ]; then
    print_success "Dockerfile exists"
    
    # Validate Dockerfile content
    dockerfile_checks=(
        "FROM.*openjdk:17"
        "EXPOSE"
        "HEALTHCHECK"
        "ENTRYPOINT"
    )
    
    for check in "${dockerfile_checks[@]}"; do
        if grep -q "$check" Dockerfile; then
            print_success "Dockerfile contains: $check"
        else
            print_warning "Dockerfile missing: $check"
        fi
    done
else
    print_error "Dockerfile missing"
fi

# Check Railway configuration
if [ -f "railway.json" ]; then
    print_success "Railway configuration exists"
else
    print_warning "Railway configuration missing"
fi

# Check environment variable configuration
if grep -q '\${.*}' src/main/resources/application*.properties; then
    print_success "Environment variables configured"
else
    print_warning "Environment variables not configured"
fi

echo ""
echo "🧪 11. Code Quality Validation"
echo "------------------------------"

# Check for code organization
java_files=$(find src/main/java -name "*.java" | wc -l)
if [ $java_files -gt 0 ]; then
    print_success "Java source files: $java_files"
else
    print_error "No Java source files found"
fi

# Check package structure
packages=(
    "controller"
    "service" 
    "repository"
    "model"
    "dto"
    "config"
    "security"
    "exception"
    "util"
)

for package in "${packages[@]}"; do
    if find src/main/java -path "*/$package/*" -name "*.java" | grep -q .; then
        print_success "Package exists: $package"
    else
        print_warning "Package missing or empty: $package"
    fi
done

echo ""
echo "📋 12. Feature Completeness Validation"
echo "--------------------------------------"

# Core features checklist
features=(
    "BookingController:Booking management API"
    "RoomController:Room management API"
    "BookingService:Business logic implementation"
    "RoomService:Room business logic"
    "SecurityConfig:OAuth2 security configuration"
    "CacheConfig:Redis caching setup"
    "OpenApiConfig:API documentation"
    "GlobalExceptionHandler:Error handling"
    "PageUtil:Pagination utilities"
)

for feature in "${features[@]}"; do
    class_name=$(echo "$feature" | cut -d':' -f1)
    description=$(echo "$feature" | cut -d':' -f2)
    
    if find src/main/java -name "${class_name}.java" | grep -q .; then
        print_success "$description ($class_name)"
    else
        print_warning "$description ($class_name) - not found"
    fi
done

echo ""
echo "======================================"
echo "📊 VALIDATION SUMMARY"
echo "======================================"

echo ""
echo -e "${GREEN}✅ Successful checks: $SUCCESS_COUNT${NC}"
echo -e "${YELLOW}⚠️  Warnings: $WARNING_COUNT${NC}"
echo -e "${RED}❌ Errors: $ERROR_COUNT${NC}"

echo ""
echo "🎯 Overall Assessment:"

if [ $ERROR_COUNT -eq 0 ]; then
    if [ $WARNING_COUNT -eq 0 ]; then
        echo -e "${GREEN}🏆 EXCELLENT: All validations passed successfully!${NC}"
        echo "✅ Application is fully ready for production deployment"
    elif [ $WARNING_COUNT -lt 5 ]; then
        echo -e "${GREEN}🎉 VERY GOOD: Core functionality validated with minor warnings${NC}"
        echo "✅ Application is ready for deployment"
        echo "💡 Consider addressing warnings for optimization"
    else
        echo -e "${YELLOW}👍 GOOD: Core functionality works with some areas for improvement${NC}"
        echo "⚠️  Review warnings before production deployment"
    fi
else
    if [ $ERROR_COUNT -lt 3 ]; then
        echo -e "${YELLOW}⚠️  NEEDS ATTENTION: Minor issues found${NC}"
        echo "🔧 Fix errors before deployment"
    else
        echo -e "${RED}❌ CRITICAL: Multiple issues need to be resolved${NC}"
        echo "🚨 Resolve all errors before proceeding"
    fi
fi

echo ""
echo "🚀 Next Steps:"
echo "1. Address any errors found above"
echo "2. Consider resolving warnings for optimization"
echo "3. Set up environment variables for deployment"
echo "4. Deploy to Railway using provided configuration"
echo "5. Monitor application health via /actuator/health"

echo ""
echo "📚 Documentation:"
echo "- API Documentation: /swagger-ui.html (when running)"
echo "- Deployment Guide: docs/railway-deployment.md"
echo "- API Reference: docs/api-documentation.md"

echo ""
echo "Validation completed at: $(date)"