/**
 * @file UserRoleRepository.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 08,2025
 * @version 1.0
 * @description Dao for UserRole entity
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.User;
import com.techversant.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findOneByUser(User user);
    void deleteAllByUser(User user);
    UserRole findOneByRole(Role role);
}
