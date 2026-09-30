/**
 * @file UserRepository.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Dao for User entity
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.User;
import com.techversant.userservice.utils.enums.PermissionType;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds and returns a User entity by its username and active status.
     *
     * @param userName the username of the user to find
     * @param isActive the active status to filter the user by
     * @return the User entity matching the given username and active status, or null if none found
     */
    User findOneByUserNameAndIsActive(String userName, boolean isActive);

    /**
     * Finds and returns a User entity by its email address and active status.
     *
     * @param email    the email address of the user to find
     * @param isActive the active status to filter the user by
     * @return the User entity matching the given email and active status, or null if none found
     */

    User findOneByEmailAndIsActive(String email, boolean isActive);
    User findOneByPhoneNumberAndIsActive(String phoneNumber, boolean isActive);
    /**
     * Retrieves a paginated list of User entities filtered by their active status.
     *
     * @param isActive the active status to filter users by
     * @param pageable the pagination and sorting information
     * @return a Page of User entities matching the specified active status
     */
    Page<User> findAllByIsActive(boolean isActive, Pageable pageable);
    List<User> findAllByIsActive(boolean isActive);
    /**
     * Finds an active User by their unique ID and active status.
     *
     * @param id       the UUID of the user to find
     * @param isActive the active status to filter the user by
     * @return the matching User entity if found; otherwise, null
     */
    User findByIdAndIsActive(UUID id, boolean isActive);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    void deleteByRoleId(@Param("userId") UUID userId);


    /**
     * Retrieves a paginated list of active users filtered by optional fields such as first name, last name, username, and email.
     *
     * @param firstName the first name filter
     * @param lastName  the last name filter
     * @param userName  the username filter
     * @param email     the email filter
     * @param isActive  the active status filter
     * @param pageable  pagination and sorting parameters
     * @return a Page of User entities matching the filter criteria
     */
    @Query(
            """
                    select u from User u where 
                    (:firstName is null or lower(u.firstName) like :firstName )
                    and
                    (:lastName is null or lower(u.lastName) like :lastName )
                    and
                    (:userName is null or lower(u.userName) like :userName )
                    and
                    (:email is null or lower(u.email) like :email )
                    and
                    (:roleId is null or u.roleId=:roleId)
                    and
                    u.isActive = :isActive
                    """
    )
    Page<User> filterUsers(String firstName, String lastName, String userName, String email, UUID roleId, boolean isActive, Pageable pageable);

    User findByStaticUserAndIsActive(PermissionType staticUser, boolean isActive);

    boolean existsByUserName(String userName);

    User findByUserName(String userName);

    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    Optional<User> findByEmailOrPhoneNumber(@Param("email") String email,
                                            @Param("phoneNumber") String phoneNumber);


    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = :emailId")
    Optional<User> findByEmailId(@Param("emailId") String emailId);

}
