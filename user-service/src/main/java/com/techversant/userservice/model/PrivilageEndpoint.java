/**
 * @file PrivilageEndpoint.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 12-08-2025
 * @version 1.0
 * @description This class is an entity class which is for Privilage and Endpoint
 */
package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "privilage_endpoint", schema = "user_schema")
public class PrivilageEndpoint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name="privilege_id", nullable = false)
    private Privileges privileges;
    @ManyToOne
    @JoinColumn(name="endpoint_id", nullable = false)
    private Endpoint endpoint;
    @ManyToOne
    @JoinColumn(name="user_id", nullable = true)
    private User user;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
}
