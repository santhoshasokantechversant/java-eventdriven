package com.techversant.userservice.model.privileges;

import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "privileges_new", schema = "user_schema")
public class PrivilegesNewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name= "privilege_name",unique=true, nullable=false)
    private String privilegeName;
    @Column(name= "backend_url",unique=false, nullable=false)
    private String backendUrl;
    @Column(name= "description",unique=false, nullable=true)
    private String description;
    @Column(name= "slug_name",unique=false, nullable=true)
    private String slugName;
    @Column(name= "position",unique=false, nullable=true)
    private int position;
    @Column(name = "icon", nullable = true)
    private String icon;
    @Column(name = "parent_privilege_id", nullable = true)
    private UUID parentPrivilegeId;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
}
