/**
 * @author Nihal Elton John
 * @version 1.0
 * @file PrivilegeRepository.java
 * @company Techversant Infotech
 * @date 11-08-2025
 * @description This Interface is for Privilege repository
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.Privileges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privileges, UUID> {

    /**
     * Finds an active or inactive Privileges entity by its name.
     *
     * @param name     the name of the Privileges to search for
     * @param isActive the active status to filter the Privileges
     * @return the Privileges entity matching the given name and active status, or null if none found
     */
    Privileges findOneByNameAndIsActive(String name, boolean isActive);

    /**
     * Finds an active or inactive Privileges entity by its unique identifier (UUID).
     *
     * @param id       the UUID of the Privileges to search for
     * @param isActive the active status to filter the Privileges
     * @return the Privileges entity matching the given UUID and active status, or null if none found
     */
    Privileges findByIdAndIsActive(UUID id, boolean isActive);
    List<Privileges> findAllByIsActive(boolean isActive);
}
