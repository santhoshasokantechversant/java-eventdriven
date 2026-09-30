/**
 * @file Endpoint.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This class is an entity class for Endpoint
 */
package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "endpoints", schema = "user_schema")
public class Endpoint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name="backend_url", unique=false, nullable=true)
    private String backendUrl;
    @Column(name="http_methods", unique=false, nullable=false)
    private String httpMethod;
    @Column(name="endpoint_name", unique=true, nullable=true)
    private String endponitName;
    @Column(name="privilege_id", unique=false, nullable=true)
    private UUID privilegeId;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
}
