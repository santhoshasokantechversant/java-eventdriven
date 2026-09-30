/**
 * @file RoleRepository.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Dao for Role entity
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.Role;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds a single active Role entity by its name.
     *
     * @param name     the name of the role to search for
     * @param isActive the active status to filter by (true = only active roles)
     * @return the Role entity matching the name and active status, or null if not found
     */
    Role findOneByNameAndIsActive(String name, boolean isActive);

    /**
     * Retrieves a paginated list of roles filtered by their active status.
     *
     * @param isActive whether to fetch active (true) or inactive (false) roles
     * @param pageable the pagination and sorting information
     * @return a page of Role entities matching the specified active status
     */
    Page<Role> findAllByIsActive(boolean isActive, Pageable pageable);

    List<Role> findAllByIsActive(boolean isActive);

    /**
     * Retrieves a role by its unique identifier and active status.
     *
     * @param id       the UUID of the role to retrieve
     * @param isActive whether the role should be active
     * @return the Role entity matching the ID and active status, or null if not found
     */
    Role findByIdAndIsActive(UUID id, boolean isActive);

    /**
     * Retrieves a paginated list of roles filtered by name (case-insensitive, partial match) and active status.
     *
     * @param name     the role name pattern to search for (supports SQL LIKE syntax), or null to ignore this filter
     * @param isActive whether to include only active roles
     * @param pageable pagination and sorting information
     * @return a page of Role entities matching the filter criteria
     */
    @Query("""
            select r from Role r where 
            (:name is null or lower(r.name) like :name)
            and
            r.position in :positions
            and
            r.isActive = :isActive
            """)
    Page<Role> filterRoles(String name, boolean isActive, List<String> positions, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Permission p WHERE p.role.id = :roleId")
    void deleteByRoleIdPermission(@Param("roleId") UUID roleId);
    /**
     * Finds an active or inactive Role entity by its unique identifier (UUID).
     *
     * @param id       the UUID of the Role to search for
     * @param isActive the active status to filter the Role
     * @return the Role entity matching the given UUID and active status, or null if none found
     */
    Role findOneByIdAndIsActive(UUID id, boolean isActive);

    Optional<Role> findByName(String name);

    List<Role> findByIsActive(boolean isActive);

    @Query("SELECT r FROM Role r WHERE r.position LIKE CONCAT(:prefix, '%')")
    List<Role> findAllByPositionStartingWith(@Param("prefix") String prefix);

    Role findByPosition(String position);
}
