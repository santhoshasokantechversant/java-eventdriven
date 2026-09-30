/**
 * @file City.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Entity class for city
 */

package com.techversant.customer_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cities", schema = "customer_schema")
public class City {

    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;
    @ManyToOne(optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;
    @ManyToOne(optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
}
