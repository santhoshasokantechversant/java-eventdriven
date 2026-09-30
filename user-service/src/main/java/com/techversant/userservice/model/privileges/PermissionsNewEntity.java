package com.techversant.userservice.model.privileges;

import com.techversant.userservice.utils.BaseEntity;
import com.techversant.userservice.utils.enums.PermissionType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "permission_new", schema = "user_schema")
public class PermissionsNewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name="backend_url")
    private String backendUrl;

    @Column(name="http_method")
    private String httpMethod;

    @Column(name="role_id")
    private UUID roleId;

    @Column(name="user_id")
    private UUID userId;

    @Column(name="privilege_id")
    private UUID privilegeId;

    @Column(name="endpoint_id")
    private UUID endpointId;


    @Column(name="endpoint_name")
    private String endpointName;

    @Column(name="privilege_name")
    private String privilegeName;

    @Column(name="slug_name")
    private String slugName;

    @Column(name="position")
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "side_nav", nullable = false)
    private PermissionType sideNav = PermissionType.NO;

    @Enumerated(EnumType.STRING)
    @Column(name = "updatable_endpoint", nullable = false)
    private PermissionType updatableEndpoint = PermissionType.YES;

    @Enumerated(EnumType.STRING)
    @Column(name = "updatable_slug", nullable = false)
    private PermissionType updatableSlug = PermissionType.YES;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
    @Column(name = "updatable", nullable = true)
    private String updatable = null;
}
