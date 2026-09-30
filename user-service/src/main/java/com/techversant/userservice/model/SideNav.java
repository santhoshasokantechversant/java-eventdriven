/**
 * @file SideNav.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 22-08-2025
 * @version 1.0
 * @description This class is for side-nav Entity
 */
package com.techversant.userservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "side_nav", schema = "user_schema")
public class SideNav extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id", nullable = true)
    @JsonBackReference
    private SideNav parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @JsonManagedReference
    private List<SideNav> subPrivileges;

    @Column(name = "slug_name", nullable = false)
    private String slugName;

    @Column(name = "icon", nullable = true)
    private String icon;

    @Column(name = "position", nullable = true, updatable = true)
    private int position;

    private boolean isActive = true;
}

