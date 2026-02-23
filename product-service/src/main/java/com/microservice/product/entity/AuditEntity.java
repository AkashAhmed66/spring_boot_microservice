package com.microservice.product.entity;

import com.microservice.product.entity.listener.AuditEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base audit entity with common audit fields.
 * All entities requiring audit tracking should extend this class.
 */
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
public abstract class AuditEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
