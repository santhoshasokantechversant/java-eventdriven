package com.techversant.userservice.model.privileges;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "sidenav_new", schema = "user_schema")
public class SideNavNewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, updatable = true)
    private String name;

    // ✅ parent should be of same entity type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    @JsonBackReference
    private SideNavNewEntity parent;

    // ✅ subPrivileges should reference same entity type
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @JsonManagedReference
    private List<SideNavNewEntity> subPrivileges;

    @Column(name = "slug_name", nullable = false)
    private String slugName;

    @Column(name = "icon", nullable = true)
    private String icon;

    @Column(name = "position", nullable = true, updatable = true)
    private int position;

    @Column(name = "privilege_id", nullable = true, updatable = true)
    private UUID privilegeId;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;

}
