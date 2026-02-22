package com.microservice.auth.service;

import com.microservice.auth.entity.Permission;
import com.microservice.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    public Permission createPermission(String name, String description, String resource, String action) {
        if (permissionRepository.existsByName(name)) {
            throw new RuntimeException("Permission already exists: " + name);
        }

        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .resource(resource)
                .action(action)
                .build();

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Permission getPermissionByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    @Transactional
    public Permission updatePermission(Long id, String description) {
        Permission permission = getPermissionById(id);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = getPermissionById(id);
        if (!permission.getRoles().isEmpty()) {
            throw new RuntimeException("Cannot delete permission that is assigned to roles");
        }
        permissionRepository.deleteById(id);
    }
}
