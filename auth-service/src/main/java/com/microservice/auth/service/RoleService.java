package com.microservice.auth.service;

import com.microservice.auth.entity.Permission;
import com.microservice.auth.entity.Role;
import com.microservice.auth.repository.PermissionRepository;
import com.microservice.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Role createRole(String name, String description, Set<Long> permissionIds) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists: " + name);
        }

        Set<Permission> permissions = permissionIds.stream()
                .map(id -> permissionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + id)))
                .collect(Collectors.toSet());

        Role role = Role.builder()
                .name(name)
                .description(description)
                .permissions(permissions)
                .active(true)
                .build();

        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> getActiveRoles() {
        return roleRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByNameWithPermissions(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    @Transactional
    public Role updateRole(Long id, String description, Boolean active) {
        Role role = getRoleById(id);
        if (description != null) {
            role.setDescription(description);
        }
        if (active != null) {
            role.setActive(active);
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Role addPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Role role = getRoleById(roleId);
        
        Set<Permission> newPermissions = permissionIds.stream()
                .map(id -> permissionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + id)))
                .collect(Collectors.toSet());

        role.getPermissions().addAll(newPermissions);
        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermissionsFromRole(Long roleId, Set<Long> permissionIds) {
        Role role = getRoleById(roleId);
        
        role.getPermissions().removeIf(permission -> permissionIds.contains(permission.getId()));
        
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        if (!role.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete role that is assigned to users");
        }
        roleRepository.deleteById(id);
    }
}
