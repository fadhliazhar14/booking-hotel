package com.fadhliazhar.booking_hotel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive caching configuration for performance optimization
 * Implements Redis-based caching with different TTL strategies
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class CacheConfig {

    @Value("${spring.cache.redis.time-to-live:PT1H}")
    private Duration defaultTtl;

    @Value("${spring.cache.redis.cache-null-values:false}")
    private boolean cacheNullValues;

    // Cache names constants
    public static final String ROOMS_CACHE = "rooms";
    public static final String BOOKINGS_CACHE = "bookings";
    public static final String AVAILABLE_ROOMS_CACHE = "availableRooms";
    public static final String AMENITY_TYPES_CACHE = "amenityTypes";
    public static final String SERVICE_TYPES_CACHE = "serviceTypes";
    public static final String ROOM_AMENITIES_CACHE = "roomAmenities";
    public static final String ROOM_SERVICES_CACHE = "roomServices";
    public static final String USER_BOOKINGS_CACHE = "userBookings";

    /**
     * Configure Redis template with proper serialization
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        
        log.info("Redis template configured with JSON serialization");
        return template;
    }

    /**
     * Configure cache manager with different TTL policies
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = createDefaultCacheConfig();
        
        // Configure specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Short-lived caches (5 minutes) - frequently changing data
        cacheConfigurations.put(AVAILABLE_ROOMS_CACHE, 
            createCacheConfig(Duration.ofMinutes(5)));
        cacheConfigurations.put(USER_BOOKINGS_CACHE, 
            createCacheConfig(Duration.ofMinutes(5)));
        
        // Medium-lived caches (30 minutes) - moderately changing data
        cacheConfigurations.put(ROOMS_CACHE, 
            createCacheConfig(Duration.ofMinutes(30)));
        cacheConfigurations.put(BOOKINGS_CACHE, 
            createCacheConfig(Duration.ofMinutes(30)));
        cacheConfigurations.put(ROOM_AMENITIES_CACHE, 
            createCacheConfig(Duration.ofMinutes(30)));
        cacheConfigurations.put(ROOM_SERVICES_CACHE, 
            createCacheConfig(Duration.ofMinutes(30)));
        
        // Long-lived caches (2 hours) - rarely changing data
        cacheConfigurations.put(AMENITY_TYPES_CACHE, 
            createCacheConfig(Duration.ofHours(2)));
        cacheConfigurations.put(SERVICE_TYPES_CACHE, 
            createCacheConfig(Duration.ofHours(2)));
        
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
        
        log.info("Redis cache manager configured with {} cache configurations", 
                cacheConfigurations.size());
        
        return cacheManager;
    }

    /**
     * Create default cache configuration
     */
    private RedisCacheConfiguration createDefaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(defaultTtl)
            .disableCachingNullValues()
            .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(createJsonSerializer()))
            .computePrefixWith(cacheName -> "booking-hotel:" + cacheName + ":");
    }

    /**
     * Create cache configuration with specific TTL
     */
    private RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return createDefaultCacheConfig().entryTtl(ttl);
    }

    /**
     * Create JSON serializer with proper configuration
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * Cache monitoring and management utilities
     */
    @Bean
    public CacheMonitor cacheMonitor(CacheManager cacheManager) {
        return new CacheMonitor(cacheManager);
    }

    /**
     * Utility class for cache monitoring and management
     */
    public static class CacheMonitor {
        private final CacheManager cacheManager;

        public CacheMonitor(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        /**
         * Clear specific cache
         */
        public void clearCache(String cacheName) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cache '{}' cleared", cacheName);
            }
        }

        /**
         * Clear all caches
         */
        public void clearAllCaches() {
            cacheManager.getCacheNames().forEach(cacheName -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info("All caches cleared");
        }

        /**
         * Get cache statistics (basic implementation)
         */
        public Map<String, Object> getCacheStats() {
            Map<String, Object> stats = new HashMap<>();
            for (String cacheName : cacheManager.getCacheNames()) {
                org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    stats.put(cacheName, "Cache exists and is accessible");
                }
            }
            return stats;
        }

        /**
         * Evict specific cache entry
         */
        public void evictCacheEntry(String cacheName, Object key) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("Cache entry evicted - Cache: {}, Key: {}", cacheName, key);
            }
        }

        /**
         * Check if cache entry exists
         */
        public boolean cacheEntryExists(String cacheName, Object key) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        }
    }

    /**
     * Cache warming utility for preloading frequently accessed data
     */
    @Bean
    public CacheWarmer cacheWarmer(CacheManager cacheManager) {
        return new CacheWarmer(cacheManager);
    }

    /**
     * Utility class for warming up caches with frequently accessed data
     */
    public static class CacheWarmer {
        private final CacheManager cacheManager;

        public CacheWarmer(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        /**
         * Warm up specific cache with data
         */
        public void warmUpCache(String cacheName, Object key, Object value) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                log.debug("Cache warmed up - Cache: {}, Key: {}", cacheName, key);
            }
        }

        /**
         * Warm up caches during application startup
         */
        public void warmUpCaches() {
            log.info("Starting cache warm-up process");
            
            // This method can be called during application startup
            // to preload frequently accessed data into caches
            // Implementation would depend on your specific use case
            
            log.info("Cache warm-up process completed");
        }
    }
}