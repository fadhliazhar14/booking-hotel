package com.fadhliazhar.booking_hotel.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitoring configuration for application metrics and performance tracking
 */
@Slf4j
@Configuration
public class MonitoringConfig {

    /**
     * Custom metrics collector for business operations
     */
    @Component
    public static class BookingMetrics {
        
        private final MeterRegistry meterRegistry;
        private final Counter bookingCreatedCounter;
        private final Counter bookingCancelledCounter;
        private final Counter bookingCompletedCounter;
        private final Counter authenticationFailureCounter;
        private final Timer bookingCreationTimer;
        private final Timer roomSearchTimer;
        private final AtomicLong activeBookingsGauge;

        @Autowired
        public BookingMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            
            // Counters for business events
            this.bookingCreatedCounter = Counter.builder("bookings.created")
                    .description("Total number of bookings created")
                    .register(meterRegistry);
                    
            this.bookingCancelledCounter = Counter.builder("bookings.cancelled")
                    .description("Total number of bookings cancelled")
                    .register(meterRegistry);
                    
            this.bookingCompletedCounter = Counter.builder("bookings.completed")
                    .description("Total number of bookings completed")
                    .register(meterRegistry);
                    
            this.authenticationFailureCounter = Counter.builder("auth.failures")
                    .description("Total number of authentication failures")
                    .register(meterRegistry);
            
            // Timers for operation performance
            this.bookingCreationTimer = Timer.builder("bookings.creation.duration")
                    .description("Time taken to create a booking")
                    .register(meterRegistry);
                    
            this.roomSearchTimer = Timer.builder("rooms.search.duration")
                    .description("Time taken to search for available rooms")
                    .register(meterRegistry);
            
            // Gauge for active bookings
            this.activeBookingsGauge = new AtomicLong(0);
            meterRegistry.gauge("bookings.active", activeBookingsGauge);
        }

        public void incrementBookingCreated() {
            bookingCreatedCounter.increment();
            activeBookingsGauge.incrementAndGet();
            log.debug("Booking created metric incremented");
        }

        public void incrementBookingCancelled() {
            bookingCancelledCounter.increment();
            activeBookingsGauge.decrementAndGet();
            log.debug("Booking cancelled metric incremented");
        }

        public void incrementBookingCompleted() {
            bookingCompletedCounter.increment();
            activeBookingsGauge.decrementAndGet();
            log.debug("Booking completed metric incremented");
        }

        public void incrementAuthenticationFailure() {
            authenticationFailureCounter.increment();
            log.debug("Authentication failure metric incremented");
        }

        public Timer.Sample startBookingCreationTimer() {
            return Timer.start(meterRegistry);
        }

        public void recordBookingCreationTime(Timer.Sample sample) {
            sample.stop(bookingCreationTimer);
        }

        public Timer.Sample startRoomSearchTimer() {
            return Timer.start(meterRegistry);
        }

        public void recordRoomSearchTime(Timer.Sample sample) {
            sample.stop(roomSearchTimer);
        }

        public void setActiveBookings(long count) {
            activeBookingsGauge.set(count);
        }
    }

    /**
     * Performance monitoring utility
     */
    @Component
    public static class PerformanceMonitor {
        
        private final MeterRegistry meterRegistry;
        
        @Autowired
        public PerformanceMonitor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }
        
        /**
         * Record custom timer metric
         */
        public Timer.Sample startTimer(String name, String description) {
            Timer timer = Timer.builder(name)
                    .description(description)
                    .register(meterRegistry);
            return Timer.start(meterRegistry);
        }
        
        /**
         * Record duration metric
         */
        public void recordDuration(String name, Duration duration) {
            Timer.builder(name)
                    .description("Custom duration metric")
                    .register(meterRegistry)
                    .record(duration);
        }
        
        /**
         * Increment counter metric
         */
        public void incrementCounter(String name, String description) {
            Counter.builder(name)
                    .description(description)
                    .register(meterRegistry)
                    .increment();
        }
        
        /**
         * Record gauge metric
         */
        public void recordGauge(String name, Number value, String description) {
            meterRegistry.gauge(name, value);
        }
    }

    /**
     * Application monitoring bean
     */
    @Bean
    public ApplicationMonitor applicationMonitor(MeterRegistry meterRegistry) {
        return new ApplicationMonitor(meterRegistry);
    }

    /**
     * Application-wide monitoring and alerting
     */
    public static class ApplicationMonitor {
        
        private final MeterRegistry meterRegistry;
        private final long applicationStartTime;
        
        public ApplicationMonitor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            this.applicationStartTime = System.currentTimeMillis();
            
            // Register application uptime gauge
            meterRegistry.gauge("application.uptime", this, 
                monitor -> (System.currentTimeMillis() - monitor.applicationStartTime) / 1000.0);
        }
        
        @PostConstruct
        public void init() {
            log.info("Application monitoring initialized at {}", applicationStartTime);
            
            // Record application startup metric
            Counter.builder("application.startup")
                    .description("Application startup events")
                    .register(meterRegistry)
                    .increment();
        }
        
        /**
         * Monitor critical business operations
         */
        public void monitorCriticalOperation(String operationName, Runnable operation) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                operation.run();
                
                // Record successful operation
                Counter.builder("operations.success")
                        .tag("operation", operationName)
                        .description("Successful critical operations")
                        .register(meterRegistry)
                        .increment();
                        
            } catch (Exception ex) {
                // Record failed operation
                Counter.builder("operations.failure")
                        .tag("operation", operationName)
                        .description("Failed critical operations")
                        .register(meterRegistry)
                        .increment();
                
                log.error("Critical operation '{}' failed", operationName, ex);
                throw ex;
                
            } finally {
                // Record operation duration
                sample.stop(Timer.builder("operations.duration")
                        .tag("operation", operationName)
                        .description("Duration of critical operations")
                        .register(meterRegistry));
            }
        }
        
        public long getUptimeSeconds() {
            return (System.currentTimeMillis() - applicationStartTime) / 1000;
        }
    }

    /**
     * Custom application health monitor without Spring Boot Actuator dependencies
     */
    @Component
    public static class ApplicationHealthMonitor {
        
        private final AtomicLong lastHealthCheck = new AtomicLong(System.currentTimeMillis());
        private volatile boolean isHealthy = true;
        private final MeterRegistry meterRegistry;
        
        @Autowired
        public ApplicationHealthMonitor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            
            // Register health status gauge
            meterRegistry.gauge("application.health.status", this, 
                monitor -> monitor.isHealthy ? 1.0 : 0.0);
        }
        
        public void performHealthCheck() {
            try {
                lastHealthCheck.set(System.currentTimeMillis());
                
                // Add your custom health checks here
                // For example: database connectivity, external service availability, etc.
                
                setHealthy(true);
                log.debug("Health check passed");
                
            } catch (Exception ex) {
                setHealthy(false);
                log.error("Health check failed", ex);
            }
        }
        
        public void setHealthy(boolean healthy) {
            this.isHealthy = healthy;
            
            // Record health status change
            Counter.builder("application.health.changes")
                    .tag("status", healthy ? "healthy" : "unhealthy")
                    .description("Application health status changes")
                    .register(meterRegistry)
                    .increment();
        }
        
        public boolean isHealthy() {
            return isHealthy;
        }
        
        public long getLastHealthCheckTime() {
            return lastHealthCheck.get();
        }
    }
}