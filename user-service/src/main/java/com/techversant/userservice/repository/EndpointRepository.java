/**
 * @author Nihal Elton John
 * @version 1.0
 * @file EndpointRepository.java
 * @company Techversant Infotech
 * @date 11-08-2025
 * @description This Interface is for Endpoint Class
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.Endpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {

    /**
     * Finds an active or inactive Endpoint by its name.
     *
     * @param name     the name of the Endpoint to search for
     * @param isActive the active status to filter the Endpoint
     * @return the Endpoint entity matching the given name and active status, or null if none found
     */



    /**
     * Finds an active or inactive Endpoint by its unique identifier (UUID).
     *
     * @params id the UUID of the Endpoint to search for
     * @param isActive the active status to filter the Endpoint
     * @return the Endpoint entity matching the given UUID and active status, or null if none found
     */
    Endpoint findOneByHttpMethodAndIsActive(String httpMethod, boolean isActive);
    Endpoint findOneByHttpMethodAndEndponitNameAndIsActive(String httpMethod, String endponitName, boolean isActive);
    Endpoint findByIdAndIsActive(UUID id, boolean isActive);
    List<Endpoint> findAllByIsActive(boolean isActive);

    @Query("""
            select r from Endpoint r
            where (:endponitName is null or lower(r.endponitName) like :endponitName)
            and r.isActive = :isActive
            """)
    Page<Endpoint> filterEndpoint(String endponitName, boolean isActive, Pageable pageable);
}
