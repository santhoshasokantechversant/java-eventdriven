package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import com.techversant.userservice.utils.enums.ResetPasswordStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "reset_password", schema = "user_schema")
public class ResetPassword  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "encrypted_user_id", nullable = false, unique = true, length = 500)
    private String encryptedUserId;

    @Column(name = "user_id", nullable = false, length = 50)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "api_status", nullable = false)
    private ResetPasswordStatus ApiStatus = ResetPasswordStatus.ACTIVE;


}
