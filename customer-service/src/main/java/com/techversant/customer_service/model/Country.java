/**
 * @file Country.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Entity class for country
 */

package com.techversant.customer_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "countries", schema = "customer_schema")
public class Country {

    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;
    @Column(name = "iso3", length = 3)
    private String iso3;
    @Column(name = "iso2", length = 2)
    private String iso2;
    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;
    @ManyToOne
    @JoinColumn(name = "subregion_id")
    private SubRegion subRegion;
}
