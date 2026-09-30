/**
 * @file CustomerUserEntity.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Entity class for CustomerUSER
 */

package com.techversant.customer_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "Customer_user_table", schema = "customer_schema")
public class CustomerUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;
}
