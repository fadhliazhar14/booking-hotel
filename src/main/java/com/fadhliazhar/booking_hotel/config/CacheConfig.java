package com.fadhliazhar.booking_hotel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple caching configuration using in-memory concurrent maps
 * Lightweight and memory efficient - no external dependencies required
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

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
     * Simple cache manager using in-memory concurrent maps
     * No Redis dependency - lightweight and memory efficient
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            ROOMS_CACHE,
            BOOKINGS_CACHE,
            AVAILABLE_ROOMS_CACHE,
            AMENITY_TYPES_CACHE,
            SERVICE_TYPES_CACHE,
            ROOM_AMENITIES_CACHE,
            ROOM_SERVICES_CACHE,
            USER_BOOKINGS_CACHE
        );
        
        cacheManager.setAllowNullValues(false);
        
        log.info("Simple cache manager configured with {} caches: {}", 
                cacheManager.getCacheNames().size(), cacheManager.getCacheNames());
        
        return cacheManager;
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