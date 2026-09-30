/**
 * @file UserRole.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07, 2025
 * @version 1.1
 * @description Entity class for user-role
 */

package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_roles", schema = "user_schema")
public class UserRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
