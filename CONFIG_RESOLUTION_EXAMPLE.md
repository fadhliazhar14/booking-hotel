# Spring Boot Configuration Resolution Example

## Scenario: SPRING_PROFILES_ACTIVE=railway

### Step 1: Load application.properties (BASE)
```properties
spring.cache.type=${CACHE_TYPE:redis}                    # redis (default)
spring.datasource.url=${MYSQL_CURRENT_URL}               # dari env var
server.port=${SERVER_PORT:8080}                          # 8080 (default)
logging.level.com.fadhliazhar=${APP_LOG_LEVEL:INFO}     # INFO (default)
```

### Step 2: Load application-railway.properties (OVERRIDE)
```properties
spring.cache.type=simple                                 # OVERRIDE: redis → simple
spring.datasource.url=${DATABASE_URL}                    # OVERRIDE: MYSQL_CURRENT_URL → DATABASE_URL
server.port=${PORT:8080}                                 # OVERRIDE: SERVER_PORT → PORT  
logging.level.com.fadhliazhar=DEBUG                      # OVERRIDE: INFO → DEBUG
```

### Step 3: Apply Environment Variables (FINAL OVERRIDE)
```bash
# Railway Environment Variables
PORT=3000                    # OVERRIDE: server.port=${PORT:8080} → 3000
DATABASE_URL=mysql://...     # OVERRIDE: spring.datasource.url value
CACHE_TYPE=simple           # NOT USED (karena railway profile sudah set spring.cache.type=simple)
```

### Final Configuration Result:
```properties
spring.cache.type=simple              # From railway profile
spring.datasource.url=mysql://...     # From env var DATABASE_URL  
server.port=3000                      # From env var PORT
logging.level.com.fadhliazhar=DEBUG   # From railway profile
```

## ✅ RECOMMENDED APPROACH for Railway:

### Option 1: Profile-based (RECOMMENDED)
```bash
# Railway Environment Variables:
SPRING_PROFILES_ACTIVE=railway
DATABASE_URL=mysql://user:pass@host:port/db
PORT=8080
```

### Option 2: Environment Variable Override  
```bash
# Railway Environment Variables (overrides application.properties):
SPRING_PROFILES_ACTIVE=prod
CACHE_TYPE=simple
SPRING_CACHE_TYPE=simple  
MYSQL_CURRENT_URL=mysql://user:pass@host:port/db
SERVER_PORT=8080
```

## 🚨 IMPORTANT NOTES:

1. **Profile file ALWAYS loads AFTER base file**
2. **Environment variables have HIGHEST priority**
3. **Use consistent variable names** across profiles
4. **Profile-specific files are ADDITIVE + OVERRIDE**
