/**
 * @file Account.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Entity class for account
 */

package com.techversant.accountservice.model;

import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.enums.Status;
import com.techversant.accountservice.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account", schema = "account_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_no", nullable = false)
    private Long customerNo;

    @Column(name = "acc_no", nullable = false, updatable = false, unique = true)
    private Long accNo;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;



    /**
     * Default initialization before persisting
     */
    @PrePersist
    public void prePersist() {
        if (accountType == null) {
            accountType = AccountType.SAVINGS;
        }
        if (balance == null) {
            balance = BigDecimal.valueOf(1000.00); // default min balance
        }
        if (!isActive) {
            isActive = true; // default active
        }
        if (status == null) {
            status = Status.ACTIVE; // default active
        }

    }
}
