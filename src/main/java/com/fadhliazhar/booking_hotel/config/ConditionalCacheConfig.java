package com.fadhliazhar.booking_hotel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Conditional cache configuration for environments without Redis
 * This configuration is used when Redis is not available (like Railway free tier)
 */
@Slf4j
@Configuration
@EnableCaching
@Profile("railway")
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
public class ConditionalCacheConfig {

    // Cache names constants (same as in CacheConfig)
    public static final String ROOMS_CACHE = "rooms";
    public static final String BOOKINGS_CACHE = "bookings";
    public static final String AVAILABLE_ROOMS_CACHE = "availableRooms";
    public static final String AMENITY_TYPES_CACHE = "amenityTypes";
    public static final String SERVICE_TYPES_CACHE = "serviceTypes";
    public static final String ROOM_AMENITIES_CACHE = "roomAmenities";
    public static final String ROOM_SERVICES_CACHE = "roomServices";
    public static final String USER_BOOKINGS_CACHE = "userBookings";

    /**
     * Simple cache manager for Railway deployment (no Redis)
     * Uses in-memory caching as fallback
     */
    @Bean
    public CacheManager simpleCacheManager() {
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
        
        log.info("Simple cache manager configured for Railway deployment with {} caches", 
                cacheManager.getCacheNames().size());
        
        return cacheManager;
    }
}
