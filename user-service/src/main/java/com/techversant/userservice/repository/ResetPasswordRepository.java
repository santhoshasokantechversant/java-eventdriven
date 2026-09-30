package com.techversant.userservice.repository;

import com.techversant.userservice.model.ResetPassword;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, UUID> {
    @Query("SELECT r FROM ResetPassword r WHERE r.encryptedUserId = :encryptedUserId ORDER BY r.createdAt DESC")
    ResetPassword findByEncryptedUserIdOrderByCreatedAtDesc(@Param("encryptedUserId") String encryptedUserId);
}
