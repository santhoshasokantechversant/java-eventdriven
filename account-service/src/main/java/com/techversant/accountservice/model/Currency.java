/**
 * @file Currency.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Entity class for handling currency information
 */

package com.techversant.accountservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "currency", schema = "account_schema")
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "currency_id")
    private int id;
    @Column(name = "currency_code", nullable = false, unique = true)
    private String currencyCode;
    @Column(name = "currency_description", nullable = false)
    private String currencyDescription;
}
