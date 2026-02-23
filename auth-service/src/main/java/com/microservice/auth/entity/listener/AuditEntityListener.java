package com.microservice.auth.entity.listener;

import com.microservice.auth.entity.AuditEntity;
import com.microservice.auth.security.JwtUserDetails;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * JPA Entity Listener for automatic audit field population.
 * Extracts authenticated user from SecurityContext and populates audit fields.
 */
public class AuditEntityListener {
    
    @PrePersist
    public void prePersist(AuditEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUser();
        
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        
        if (entity.getIsDeleted() == null) {
            entity.setIsDeleted(false);
        }
    }
    
    @PreUpdate
    public void preUpdate(AuditEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
    }
    
    /**
     * Extracts the current authenticated user ID from Spring Security context.
     * Returns userId if authenticated, otherwise returns "system".
     */
    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof JwtUserDetails) {
                    JwtUserDetails userDetails = (JwtUserDetails) principal;
                    return userDetails.getUserId();
                }
                
                // Fallback if principal is a string
                if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception e) {
            // If SecurityContext is not available or any error occurs
        }
        
        return "system";
    }
}
