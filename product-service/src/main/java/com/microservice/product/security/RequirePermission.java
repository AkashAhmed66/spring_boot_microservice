package com.microservice.product.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required permission for a controller method.
 * The permission will be checked against the X-User-Permissions header
 * passed from the API Gateway.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * The permission name required to access the method.
     * Examples: "READ_PRODUCTS", "WRITE_PRODUCTS", "DELETE_PRODUCTS"
     */
    String value();
}
