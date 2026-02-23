package com.microservice.product.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT validation for public endpoints (actuator health checks, etc.)
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }

            // Extract claims
            Claims claims = jwtUtil.extractClaims(token);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String fullName = claims.get("fullName", String.class);
            String rolesStr = claims.get("roles", String.class);
            String permissionsStr = claims.get("permissions", String.class);

            // Parse roles and permissions into lists
            List<String> rolesList = Collections.emptyList();
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();
            if (rolesStr != null && !rolesStr.isEmpty()) {
                rolesList = Arrays.stream(rolesStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                authorities = rolesList.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // Parse permissions
            List<String> permissionsList = Collections.emptyList();
            if (permissionsStr != null && !permissionsStr.isEmpty()) {
                permissionsList = Arrays.stream(permissionsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                List<SimpleGrantedAuthority> permissionAuthorities = permissionsList.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                authorities.addAll(permissionAuthorities);
            }

            // Create JwtUserDetails object with all user information
            JwtUserDetails userDetails = new JwtUserDetails(
                userId, 
                email, 
                fullName, 
                rolesList, 
                permissionsList
            );

            // Create authentication token with JwtUserDetails as principal
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Failed to validate JWT token: " + e.getMessage());
        }
    }
}
