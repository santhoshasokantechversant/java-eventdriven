/**
 * @file Privileges.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This class is an entity class for Privileges
 */
package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "privileges", schema = "user_schema")
// Privileges class, it’s meant to be a JPA entity for storing application permissions
public class Privileges extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(unique=true, nullable=false)
    private String name;
    @Column(unique=false, nullable=false)
    private String url;
    private String description;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
}
