package com.techversant.userservice.repository.privilegesnew;

import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrivilegesRepositoryNew extends JpaRepository<PrivilegesNewEntity, UUID> {
    @Query("""
            select r from PrivilegesNewEntity r
            where (:privilegeName is null or lower(r.privilegeName) like :privilegeName)
            and r.isActive = :isActive
            """)
    Page<PrivilegesNewEntity> filterPrivileges(String privilegeName, boolean isActive, Pageable pageable);

}
