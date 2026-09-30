/**
 * @file State.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Entity class for state
 */

package com.techversant.customer_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "states", schema = "customer_schema")
public class State {

    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;
    @ManyToOne(optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    @Column(name = "country_code", columnDefinition = "TEXT")
    private String countryCode;
    @Column(name = "country_name", columnDefinition = "TEXT")
    private String countryName;

}
