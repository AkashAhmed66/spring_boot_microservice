package com.microservice.auth.controller;

import com.microservice.auth.dto.AssignRolesRequest;
import com.microservice.auth.entity.User;
import com.microservice.auth.security.RequirePermission;
import com.microservice.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final AuthService authService;

    @PostMapping("/{userId}/roles")
    @RequirePermission("ASSIGN_ROLES")
    public ResponseEntity<User> assignRolesToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(authService.assignRolesToUser(userId, request.getRoleIds()));
    }

    @DeleteMapping("/{userId}/roles")
    @RequirePermission("ASSIGN_ROLES")
    public ResponseEntity<User> removeRolesFromUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(authService.removeRolesFromUser(userId, request.getRoleIds()));
    }
}
