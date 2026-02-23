package com.microservice.product.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

/**
 * Aspect that intercepts methods annotated with @RequirePermission
 * and validates that the user has the required permission using SecurityContext.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    @Around("@annotation(com.microservice.product.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Get the required permission from annotation
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String requiredPermission = requirePermission.value();
        
        // Get authentication from SecurityContext (already set by JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not authenticated"
            );
        }
        
        // Get JwtUserDetails from principal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JwtUserDetails)) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invalid authentication principal"
            );
        }
        
        JwtUserDetails userDetails = (JwtUserDetails) principal;
        
        // Check if user has the required permission
        if (!userDetails.hasPermission(requiredPermission)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                String.format("Access denied: User '%s' does not have permission '%s'",
                    userDetails.getEmail(),
                    requiredPermission)
            );
        }
        
        // Permission granted, proceed with method execution
        return joinPoint.proceed();
    }
}
