package com.microservice.auth.config;

import com.microservice.auth.entity.Permission;
import com.microservice.auth.entity.Role;
import com.microservice.auth.entity.User;
import com.microservice.auth.repository.PermissionRepository;
import com.microservice.auth.repository.RoleRepository;
import com.microservice.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * DataInitializer service for manual initialization of:
 * - Admin permissions (User, Role, Permission management)
 * - Product permissions (READ, WRITE, DELETE)
 * - ADMIN role with all permissions
 * - Admin user with ADMIN role
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String initializeData() {
        // Check if initialization is needed
        if (userRepository.existsByEmail("admin@example.com")) {
            log.info("Data already initialized. Skipping initialization...");
            return "Data already initialized. Admin user exists.";
        }
        
        log.info("Starting data initialization...");

        // Create permissions
        createPermissionsIfNotExist();

        // Create ADMIN role with all permissions
        createAdminRoleIfNotExist();

        // Create admin user
        createAdminUserIfNotExist();

        log.info("Data initialization completed successfully!");
        return "Data initialization completed successfully! Admin credentials - Email: admin@example.com, Password: admin123";
    }

    private void createPermissionsIfNotExist() {
        log.info("Creating permissions...");

        // User management permissions
        createPermission("CREATE_USER", "Create new users", "USER", "CREATE");
        createPermission("READ_USER", "View user details", "USER", "READ");
        createPermission("UPDATE_USER", "Update user information", "USER", "UPDATE");
        createPermission("DELETE_USER", "Delete users", "USER", "DELETE");
        createPermission("ASSIGN_ROLES", "Assign roles to users", "USER", "ASSIGN_ROLES");

        // Role management permissions
        createPermission("CREATE_ROLE", "Create new roles", "ROLE", "CREATE");
        createPermission("READ_ROLE", "View role details", "ROLE", "READ");
        createPermission("UPDATE_ROLE", "Update role information", "ROLE", "UPDATE");
        createPermission("DELETE_ROLE", "Delete roles", "ROLE", "DELETE");
        createPermission("ASSIGN_PERMISSIONS", "Assign permissions to roles", "ROLE", "ASSIGN_PERMISSIONS");

        // Permission management permissions
        createPermission("CREATE_PERMISSION", "Create new permissions", "PERMISSION", "CREATE");
        createPermission("READ_PERMISSION", "View permission details", "PERMISSION", "READ");
        createPermission("UPDATE_PERMISSION", "Update permission information", "PERMISSION", "UPDATE");
        createPermission("DELETE_PERMISSION", "Delete permissions", "PERMISSION", "DELETE");

        // Product management permissions (for product service)
        createPermission("READ_PRODUCTS", "View products", "PRODUCT", "READ");
        createPermission("WRITE_PRODUCTS", "Create or update products", "PRODUCT", "WRITE");
        createPermission("DELETE_PRODUCTS", "Delete products", "PRODUCT", "DELETE");

        log.info("Permissions created successfully!");
    }

    private void createPermission(String name, String description, String resource, String action) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = Permission.builder()
                    .name(name)
                    .description(description)
                    .resource(resource)
                    .action(action)
                    .build();
            permissionRepository.save(permission);
            log.info("Created permission: {}", name);
        } else {
            log.debug("Permission already exists: {}", name);
        }
    }

    private void createAdminRoleIfNotExist() {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            log.info("Creating ADMIN role...");

            // Get all permissions
            Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator role with full access to all resources")
                    .permissions(allPermissions)
                    .active(true)
                    .build();

            roleRepository.save(adminRole);
            log.info("ADMIN role created with {} permissions", allPermissions.size());
        } else {
            log.debug("ADMIN role already exists");
        }
    }

    private void createAdminUserIfNotExist() {
        String adminEmail = "admin@example.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating admin user...");

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123")) // Default password
                    .fullName("System Administrator")
                    .roles(roles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(adminUser);
            log.info("Admin user created successfully!");
            log.info("=================================================");
            log.info("Admin Credentials:");
            log.info("Email: {}", adminEmail);
            log.info("Password: admin123");
            log.info("=================================================");
            log.warn("IMPORTANT: Change the admin password after first login!");
        } else {
            log.debug("Admin user already exists");
        }
    }
}
