package com.fadhliazhar.booking_hotel.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user's ID from JWT token
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentJwtToken()
                .map(jwt -> jwt.getClaimAsString("sub"));
    }

    /**
     * Get the current authenticated user's username from JWT token
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentJwtToken()
                .map(jwt -> jwt.getClaimAsString("preferred_username"))
                .or(() -> getCurrentJwtToken()
                        .map(jwt -> jwt.getClaimAsString("username")));
    }

    /**
     * Get the current authenticated user's email from JWT token
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentJwtToken()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    /**
     * Get the current JWT token
     */
    public static Optional<Jwt> getCurrentJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    /**
     * Get current user's authorities/roles
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities();
        for (String role : roles) {
            if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Log current user info for debugging
     */
    public static void logCurrentUser() {
        if (isAuthenticated()) {
            getCurrentUserId().ifPresent(userId -> 
                log.debug("Current user ID: {}", userId));
            getCurrentUsername().ifPresent(username -> 
                log.debug("Current username: {}", username));
            log.debug("Current user roles: {}", getCurrentUserAuthorities());
        } else {
            log.debug("No authenticated user");
        }
    }
}