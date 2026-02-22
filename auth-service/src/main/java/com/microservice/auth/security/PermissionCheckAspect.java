package com.microservice.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Aspect that intercepts methods annotated with @RequirePermission
 * and validates that the user has the required permission.
 */
@Aspect
@Component
@Slf4j
public class PermissionCheckAspect {

    @Around("@annotation(com.microservice.auth.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the HTTP request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // Get the required permission from annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String requiredPermission = requirePermission.value();

        // Get user's permissions from header (set by Gateway)
        String userPermissionsHeader = request.getHeader("X-User-Permissions");
        String userEmail = request.getHeader("X-User-Email");

        if (userPermissionsHeader == null || userPermissionsHeader.isEmpty()) {
            log.warn("Access denied: No permissions header found for user '{}'", userEmail);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: No permissions found"
            );
        }

        // Parse permissions (comma-separated)
        Set<String> userPermissions = new HashSet<>(Arrays.asList(userPermissionsHeader.split(",")));

        // Check if user has the required permission
        if (!userPermissions.contains(requiredPermission)) {
            log.warn("Access denied: User '{}' does not have permission '{}'", userEmail, requiredPermission);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    String.format("Access denied: User '%s' does not have permission '%s'", 
                            userEmail != null ? userEmail : "unknown", requiredPermission)
            );
        }

        log.debug("Permission check passed: User '{}' has permission '{}'", userEmail, requiredPermission);
        
        // Permission granted, proceed with method execution
        return joinPoint.proceed();
    }
}
