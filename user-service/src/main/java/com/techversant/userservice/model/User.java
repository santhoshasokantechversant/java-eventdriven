/**
 * @file User.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Entity class for user
 */

package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name = "users", schema = "user_schema")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "user_no", nullable = false, updatable = false, unique = true)
    private Long userNo;
    @Column(nullable = false, unique = true, name = "user_name", length=50)
    private String userName;
    @Column(nullable = false, unique = true, name = "email", length=100)
    private String email;
    @Column(nullable = false, unique = true, name = "phone_number", length=100)
    private String phoneNumber;
    @Column(nullable = false, name = "first_name", length=50)
    private String firstName;
    @Column(nullable = false, name = "last_name", length=50)
    private String lastName;
    @Column(nullable = false, name = "keyclock_user_id", length=50, unique = true)
    private String keyclockUserId;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "address", length = 200)
    private String address;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "state", length = 100)
    private String state;
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    @Column(name = "country", length = 100)
    private String country;
    @Column(name = "role_id")
    private UUID roleId;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
    @Column(name = "static_user", updatable = false)
    private PermissionType staticUser = PermissionType.NO;
    @Column(name = "userType", updatable = false)
    private UserType userType = UserType.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "registered")
    private PermissionType registered;
}
