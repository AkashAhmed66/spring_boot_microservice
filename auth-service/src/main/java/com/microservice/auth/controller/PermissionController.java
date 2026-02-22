package com.microservice.auth.controller;

import com.microservice.auth.dto.CreatePermissionRequest;
import com.microservice.auth.entity.Permission;
import com.microservice.auth.security.RequirePermission;
import com.microservice.auth.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @RequirePermission("CREATE_PERMISSION")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
                request.getName(),
                request.getDescription(),
                request.getResource(),
                request.getAction()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @GetMapping
    @RequirePermission("READ_PERMISSION")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    @RequirePermission("READ_PERMISSION")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @GetMapping("/resource/{resource}")
    @RequirePermission("READ_PERMISSION")
    public ResponseEntity<List<Permission>> getPermissionsByResource(@PathVariable String resource) {
        return ResponseEntity.ok(permissionService.getPermissionsByResource(resource));
    }

    @PutMapping("/{id}")
    @RequirePermission("UPDATE_PERMISSION")
    public ResponseEntity<Permission> updatePermission(
            @PathVariable Long id,
            @RequestParam String description
    ) {
        return ResponseEntity.ok(permissionService.updatePermission(id, description));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("DELETE_PERMISSION")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
