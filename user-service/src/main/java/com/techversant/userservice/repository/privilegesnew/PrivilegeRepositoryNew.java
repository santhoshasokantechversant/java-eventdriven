package com.techversant.userservice.repository.privilegesnew;

import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrivilegeRepositoryNew extends JpaRepository<PrivilegesNewEntity, UUID> {
    PrivilegesNewEntity findOneByIdAndIsActive(UUID id, boolean isActive);
}
