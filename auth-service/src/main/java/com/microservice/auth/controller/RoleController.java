package com.microservice.auth.controller;

import com.microservice.auth.dto.AssignRolesRequest;
import com.microservice.auth.dto.CreateRoleRequest;
import com.microservice.auth.dto.UpdateRoleRequest;
import com.microservice.auth.entity.Role;
import com.microservice.auth.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleService.createRole(
                request.getName(),
                request.getDescription(),
                request.getPermissionIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Role>> getActiveRoles() {
        return ResponseEntity.ok(roleService.getActiveRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRole(id, request.getDescription(), request.getActive()));
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<Role> addPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(roleService.addPermissionsToRole(roleId, request.getRoleIds()));
    }

    @DeleteMapping("/{roleId}/permissions")
    public ResponseEntity<Role> removePermissionsFromRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        return ResponseEntity.ok(roleService.removePermissionsFromRole(roleId, request.getRoleIds()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
