package com.microservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    private String name;
    
    private String description;
    
    @NotEmpty(message = "At least one permission is required")
    private Set<Long> permissionIds;
}
