package com.microservice.product.controller;

import com.microservice.product.dto.ProductRequest;
import com.microservice.product.dto.ProductResponse;
import com.microservice.product.security.AuthenticationService;
import com.microservice.product.security.JwtUserDetails;
import com.microservice.product.security.RequirePermission;
import com.microservice.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @RequirePermission("WRITE_PRODUCTS")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        
        // Using AuthenticationService to get user email
        String userEmail = authenticationService.getCurrentUserEmail();
        ProductResponse response = productService.createProduct(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequirePermission("READ_PRODUCTS")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @RequirePermission("READ_PRODUCTS")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    @RequirePermission("READ_PRODUCTS")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @RequirePermission("WRITE_PRODUCTS")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        
        // Using AuthenticationService to get user email
        String userEmail = authenticationService.getCurrentUserEmail();
        ProductResponse response = productService.updateProduct(id, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("DELETE_PRODUCTS")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Product Service is running");
    }

    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo() {
        
        // Using AuthenticationService to check authentication and get user details
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
