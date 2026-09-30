package com.techversant.userservice.repository.privilegesnew;

import com.techversant.userservice.model.privileges.SideNavNewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SidenavRepositoryNew extends JpaRepository<SideNavNewEntity, UUID> {
    SideNavNewEntity findByPrivilegeId(UUID privilegeId);
    List<SideNavNewEntity> findAllByPrivilegeId(UUID privilegeId);
    List<SideNavNewEntity> findByParentIsNullOrderByPositionAsc();
}
