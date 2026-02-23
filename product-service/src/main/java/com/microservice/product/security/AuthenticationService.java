package com.microservice.product.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service to access current authenticated user information from JWT.
 * Can be injected anywhere in the application to get user details.
 */
@Service
@Slf4j
public class AuthenticationService {
    
    /**
     * Get the current authenticated user details.
     * @return Optional containing JwtUserDetails if authenticated, empty otherwise
     */
    public Optional<JwtUserDetails> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof JwtUserDetails) {
                    return Optional.of((JwtUserDetails) principal);
                }
            }
        } catch (Exception e) {
            log.error("Error getting current user from SecurityContext", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current user's ID.
     * @return User ID or null if not authenticated
     */
    public String getCurrentUserId() {
        return getCurrentUser()
                .map(JwtUserDetails::getUserId)
                .orElse(null);
    }
    
    /**
     * Get the current user's email.
     * @return User email or null if not authenticated
     */
    public String getCurrentUserEmail() {
        return getCurrentUser()
                .map(JwtUserDetails::getEmail)
                .orElse(null);
    }
    
    /**
     * Get the current user's full name.
     * @return User full name or null if not authenticated
     */
    public String getCurrentUserFullName() {
        return getCurrentUser()
                .map(JwtUserDetails::getFullName)
                .orElse(null);
    }
    
    /**
     * Get the current user's roles.
     * @return List of roles or empty list if not authenticated
     */
    public List<String> getCurrentUserRoles() {
        return getCurrentUser()
                .map(JwtUserDetails::getRoles)
                .orElse(List.of());
    }
    
    /**
     * Get the current user's permissions.
     * @return List of permissions or empty list if not authenticated
     */
    public List<String> getCurrentUserPermissions() {
        return getCurrentUser()
                .map(JwtUserDetails::getPermissions)
                .orElse(List.of());
    }
    
    /**
     * Check if current user has a specific permission.
     * @param permission Permission to check
     * @return true if user has the permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        return getCurrentUser()
                .map(user -> user.hasPermission(permission))
                .orElse(false);
    }
    
    /**
     * Check if current user has a specific role.
     * @param role Role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }
    
    /**
     * Check if there is an authenticated user.
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }
    
    /**
     * Get current user ID or throw exception if not authenticated.
     * @return User ID
     * @throws IllegalStateException if user is not authenticated
     */
    public String requireCurrentUserId() {
        return getCurrentUserId() != null 
            ? getCurrentUserId() 
            : throwNotAuthenticated();
    }
    
    /**
     * Get current user email or throw exception if not authenticated.
     * @return User email
     * @throws IllegalStateException if user is not authenticated
     */
    public String requireCurrentUserEmail() {
        return getCurrentUserEmail() != null 
            ? getCurrentUserEmail() 
            : throwNotAuthenticated();
    }
    
    private <T> T throwNotAuthenticated() {
        throw new IllegalStateException("No authenticated user found in security context");
    }
}
