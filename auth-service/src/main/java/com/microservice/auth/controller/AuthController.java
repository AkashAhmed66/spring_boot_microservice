package com.microservice.auth.controller;

import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.ChangePasswordRequest;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.security.AuthenticationService;
import com.microservice.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            String email = authenticationService.requireCurrentUserEmail();
            authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(AuthResponse.builder()
                    .message("Password changed successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }

    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo() {
        
        if (!authenticationService.isAuthenticated()) {
            return ResponseEntity.ok("User Info - Not authenticated");
        }
        
        return ResponseEntity.ok(String.format(
            "User Info - ID: %s, Email: %s, Name: %s, Roles: %s, Permissions: %s", 
            authenticationService.getCurrentUserId(),
            authenticationService.getCurrentUserEmail(),
            authenticationService.getCurrentUserFullName(),
            String.join(", ", authenticationService.getCurrentUserRoles()),
            String.join(", ", authenticationService.getCurrentUserPermissions())));
    }
}
