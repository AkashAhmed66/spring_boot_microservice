package com.microservice.product.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect that intercepts methods annotated with @RequirePermission
 * and validates that the user has the required permission.
 */
@Aspect
@Component
public class PermissionCheckAspect {

    @Around("@annotation(com.microservice.product.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Get the required permission from annotation
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String requiredPermission = requirePermission.value();
        
        // Get the current HTTP request
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Unable to access request context"
            );
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // Get user permissions from header
        String permissionsHeader = request.getHeader("X-User-Permissions");
        
        if (permissionsHeader == null || permissionsHeader.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Access denied: No permissions found"
            );
        }
        
        // Parse permissions (comma-separated)
        Set<String> userPermissions = Arrays.stream(permissionsHeader.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        
        // Check if user has the required permission
        if (!userPermissions.contains(requiredPermission)) {
            String userEmail = request.getHeader("X-User-Email");
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                String.format("Access denied: User '%s' does not have permission '%s'", 
                    userEmail != null ? userEmail : "unknown", 
                    requiredPermission)
            );
        }
        
        // Permission granted, proceed with method execution
        return joinPoint.proceed();
    }
}
