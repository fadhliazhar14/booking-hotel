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

}