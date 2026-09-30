/**
 * @file PrivilegeEndpointRepository.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 12-08-2025
 * @version 1.0
 * @description Repository class for Privilege and endpoint
 */
package com.techversant.userservice.repository;

import com.techversant.userservice.model.PrivilageEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrivilegeEndpointRepository extends JpaRepository<PrivilageEndpoint, UUID> {

    /**
     * Retrieves all privilege endpoints based on their active status.
     *
     * @param isActive a boolean indicating whether to fetch active (true) or inactive (false) privilege endpoints
     * @return a list of PrivilageEndpoint objects matching the specified active status
     */
    List<PrivilageEndpoint> findAllByIsActive(boolean isActive);
}
