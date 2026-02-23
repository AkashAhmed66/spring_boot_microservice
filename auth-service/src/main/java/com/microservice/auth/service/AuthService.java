package com.microservice.auth.service;

import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.entity.Role;
import com.microservice.auth.entity.User;
import com.microservice.auth.repository.RoleRepository;
import com.microservice.auth.repository.UserRepository;
import com.microservice.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Get default USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> createDefaultUserRole());

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);

        String rolesStr = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        // Extract all permissions from all roles
        String permissionsStr = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.joining(","));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getFullName(), rolesStr, permissionsStr);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(rolesStr)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Account is locked");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String rolesStr = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        // Extract all permissions from all roles
        String permissionsStr = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.joining(","));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getFullName(), rolesStr, permissionsStr);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(rolesStr)
                .message("Login successful")
                .build();
    }

    @Transactional
    public User assignRolesToUser(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                .collect(Collectors.toSet());

        user.getRoles().addAll(roles);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRolesFromUser(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().removeIf(role -> roleIds.contains(role.getId()));
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private Role createDefaultUserRole() {
        Role role = Role.builder()
                .name("ROLE_USER")
                .description("Default user role")
                .active(true)
                .permissions(new HashSet<>())
                .build();
        return roleRepository.save(role);
    }
}
