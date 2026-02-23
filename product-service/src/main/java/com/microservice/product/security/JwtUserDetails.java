package com.microservice.product.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO to hold JWT user information extracted from the token.
 * Can be injected into controller methods using @AuthenticationPrincipal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDetails {
    
    private String userId;
    private String email;
    private String fullName;
    private List<String> roles;
    private List<String> permissions;
    
    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
