/**
 * @file RoleService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Class implementing methods of IRoleService interface
 */

package com.techversant.userservice.service.impl;

import com.google.gson.JsonObject;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.mapper.RoleMapper;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.UserRole;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.repository.*;

import com.techversant.userservice.repository.privilegesnew.PermissionRepositorynew;
import com.techversant.userservice.service.IRoleService;
import com.techversant.userservice.service.keyclock.KeyclockRoleService;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.exceptions.*;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.techversant.userservice.utils.Constants.*;

@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final KeyclockRoleService keyclockRoleService;
    private final PrivilegeEndpointRepository privilegeEndpointRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final HttpServletRequest request;
    private final PermissionRepositorynew permissionRepositorynew;
    private final EndpointRepository endpointRepository;
    private final EntityManager entityManager;


    public RoleService(EntityManager entityManager, EndpointRepository endpointRepository, PermissionRepositorynew permissionRepositorynew, HttpServletRequest request, UserRoleRepository userRoleRepository, RoleRepository roleRepository, RoleMapper roleMapper, PermissionRepository permissionRepository, KeyclockRoleService keyclockRoleService, PrivilegeEndpointRepository privilegeEndpointRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.keyclockRoleService = keyclockRoleService;
        this.privilegeEndpointRepository = privilegeEndpointRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.request = request;
        this.permissionRepositorynew = permissionRepositorynew;
        this.endpointRepository = endpointRepository;
        this.entityManager=entityManager;
    }

    /**
     * Adds a new role to the system.
     *
     * @param roleDto the role data received from the client
     * @return the saved Role entity
     * @throws UserNameAlreadyExistsException if the role name already exists or Keycloak creation fails
     */
    @Override
    public Role addRole(RoleDto roleDto) {
        String roleName = request.getHeader("X-User-Roles");

        List<Role> allRoles = this.roleRepository.findAllByIsActive(true);
        Role currentUserRole = new Role();

        // 🔹 1️⃣ Get current user's role
        if (roleName != null) {
            String[] rolesArray = roleName.split("\\s*,\\s*");

            currentUserRole = allRoles.stream()
                    .filter(r -> Arrays.stream(rolesArray)
                            .anyMatch(role -> role.equalsIgnoreCase(r.getName())))
                    .findFirst()
                    .orElseThrow(() ->
                            new SomethingWentWrongException("1--->Invalid user role.")
                    );


            if (currentUserRole.getRoleCreate().equals(PermissionType.NO)) {
                throw new SomethingWentWrongException("You don't have access to create a role");
            }
        }
        Role existingRole = roleRepository.findOneByNameAndIsActive(roleDto.getName(), true);
        if (existingRole != null) {
            throw new UserNameAlreadyExistsException("Role already exists");
        }
        if (currentUserRole == null || currentUserRole.getId() == null) {
            String keycloakId = this.keyclockRoleService.createRole(roleDto);
            if (keycloakId == null || keycloakId.isBlank()) {
                throw new SomethingWentWrongException("Failed to create role in Keycloak");
            }
            roleDto.setKeyclockRoleId(keycloakId);
            if (roleDto.getPosition().equals("1")) {
                roleDto.setRoleCreate(PermissionType.YES);
            }
            Role role = roleRepository.save(roleMapper.roleDtoToEntity(roleDto, new Role()));
            initilizePermission(role);
            return role;
        } else {
            Role created;
            if(roleDto.getHierarchy() == null) {
                if(currentUserRole.getPosition().equals("1")){
                    roleDto.setPosition(this.getNextRootPosition(allRoles));
                }
                else{
                    roleDto.setPosition(getNextSubPosition(currentUserRole.getPosition()));
                }

            } else {
                Role hierarchyData = allRoles.stream()
                        .filter(role -> role.getId().equals(roleDto.getHierarchy()))
                        .findFirst()
                        .orElse(null);
                if (hierarchyData != null && hierarchyData.getPosition() != null) {
                    roleDto.setPosition(hierarchyData.getPosition());
                    this.hierarchyPositionShift(hierarchyData.getPosition());
                } else {
                    throw new SomethingWentWrongException("No Hierarchy position found.");
                }

            }
            String keycloakId = this.keyclockRoleService.createRole(roleDto);
            if (keycloakId == null || keycloakId.isBlank()) {
                throw new SomethingWentWrongException("Failed to create role in Keycloak");
            }
            roleDto.setKeyclockRoleId(keycloakId);
            roleDto.setRoleCreate(PermissionType.NO);
            if (roleDto.getPosition().equals("1")) {
                roleDto.setRoleCreate(PermissionType.YES);
            }
            created = roleRepository.save(roleMapper.roleDtoToEntity(roleDto, new Role()));
            initilizePermission(created);
            return created;
        }

    }

    public String getNextSubPosition(String currentPosition) {
        // Fetch all roles that start with current position
        List<Role> roles = roleRepository.findAllByPositionStartingWith(currentPosition);

        if (roles.isEmpty()) {
            // No children exist yet, first child
            return currentPosition + ".1";
        }

        // Find the maximum last segment among children
        Optional<Integer> maxLast = roles.stream()
                .map(Role::getPosition)
                .map(pos -> {
                    // If pos equals currentPosition, it's not a child, skip it
                    if (pos.equals(currentPosition)) return null;
                    String[] parts = pos.split("\\.");
                    return Integer.parseInt(parts[parts.length - 1]);
                })
                .filter(i -> i != null)
                .max(Comparator.naturalOrder());

        int nextValue = maxLast.map(i -> i + 1).orElse(1);

        return currentPosition + "." + nextValue;
    }

    public List<Role> hierarchyPositionShift(String givenPos) {
        boolean isRoot = !givenPos.contains(".");
        List<Role> updateRoles = new ArrayList<>();

        // Fetch all roles
        List<Role> allRoles = roleRepository.findAll();

        if (isRoot) {
            // Filter root-level roles to shift (>= givenPos)
            List<Role> rootRolesToShift = allRoles.stream()
                    .filter(role -> role.getPosition() != null)
                    .filter(role -> !role.getPosition().contains(".")) // root-level
                    .filter(role -> Integer.parseInt(role.getPosition()) >= Integer.parseInt(givenPos))
                    .sorted((a, b) -> Integer.parseInt(b.getPosition()) - Integer.parseInt(a.getPosition())) // descending
                    .toList();

            for (Role rootRole : rootRolesToShift) {
                String oldRootPos = rootRole.getPosition();
                int pos = Integer.parseInt(oldRootPos);
                String newRootPos = String.valueOf(pos + 1);

                // Update root role
                rootRole.setPosition(newRootPos);
                roleRepository.save(rootRole);
                updateRoles.add(rootRole);

                // Update all children of this root
                List<Role> childRoles = allRoles.stream()
                        .filter(r -> r.getPosition() != null)
                        .filter(r -> r.getPosition().startsWith(oldRootPos + "."))
                        .sorted((a, b) -> b.getPosition().compareTo(a.getPosition())) // descending
                        .toList();

                for (Role child : childRoles) {
                    String childPos = child.getPosition();
                    // Replace old root prefix with new root prefix
                    String newChildPos = newRootPos + childPos.substring(oldRootPos.length());
                    child.setPosition(newChildPos);
                    roleRepository.save(child);
                    updateRoles.add(child);
                }
            }

        } else {
            // Sub-level shift (existing logic)
            int level = givenPos.split("\\.").length - 1;

            // Sort all descending
            allRoles.sort((a, b) -> b.getPosition().compareTo(a.getPosition()));

            for (Role role : allRoles) {
                String pos = role.getPosition();
                if (pos == null || pos.isEmpty()) continue;

                if (isPositionGreaterOrEqual(pos, givenPos)) {
                    String[] parts = pos.split("\\.");
                    if (parts.length > level) {
                        int value = Integer.parseInt(parts[level]);
                        parts[level] = String.valueOf(value + 1);
                        role.setPosition(String.join(".", parts));
                        this.roleRepository.save(role);
                        updateRoles.add(role);
                    }
                }
            }
        }

        return updateRoles;
    }

    // Helper unchanged
    private boolean isPositionGreaterOrEqual(String pos, String givenPos) {
        String[] posParts = pos.split("\\.");
        String[] givenParts = givenPos.split("\\.");

        for (int i = 0; i < Math.min(posParts.length, givenParts.length); i++) {
            int p = Integer.parseInt(posParts[i]);
            int g = Integer.parseInt(givenParts[i]);
            if (p > g) return true;
            if (p < g) return false;
        }

        return posParts.length >= givenParts.length;
    }


    public String getNextRootPosition(List<Role> allRoles) {
        int next = allRoles.stream()
                .map(Role::getPosition)
                .filter(Objects::nonNull)
                .map(pos -> pos.trim().split("\\.")[0])   // take part before '.'
                .filter(s -> s.matches("\\d+"))           // only numeric roots
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;                           // if none found, start from 1

        return String.valueOf(next);
    }


// ===============================================================
// PERMISSION INITIALIZATION
// ===============================================================

    public void initilizePermission(Role role) {
        String prefix = role.getPosition().contains(".")
                ? role.getPosition().substring(0, role.getPosition().lastIndexOf("."))
                : "1";

        Role adminRole = this.roleRepository.findByPosition(prefix);
        List<PermissionsNewEntity> permissionsNewEntities = this.permissionRepositorynew.findAllByRoleId(adminRole.getId());
        List<PermissionsNewEntity> newData = new ArrayList<>();

        for (PermissionsNewEntity per : permissionsNewEntities) {
            PermissionsNewEntity newPermission = new PermissionsNewEntity();
            newPermission.setRoleId(role.getId());
            newPermission.setPermissionType(PermissionType.NO);
            if(role.getPosition().equals("1")) {
                newPermission.setPermissionType(PermissionType.YES);
            }
            newPermission.setSideNav(PermissionType.NO);
            if(role.getPosition().equals("1")) {
                newPermission.setSideNav(PermissionType.YES);
            }
            newPermission.setSlugName(per.getSlugName());
            newPermission.setEndpointId(per.getEndpointId());
            newPermission.setPosition(per.getPosition());
            newPermission.setBackendUrl(per.getBackendUrl());
            newPermission.setEndpointName(per.getEndpointName());
            newPermission.setHttpMethod(per.getHttpMethod());
            newPermission.setPrivilegeId(per.getPrivilegeId());
            newPermission.setPrivilegeName(per.getPrivilegeName());
            newPermission.setUpdatable(per.getUpdatable());
            newPermission.setUpdatableEndpoint(per.getUpdatableEndpoint());
            newPermission.setUpdatableSlug(per.getUpdatableSlug());
            newData.add(newPermission);
        }
        this.permissionRepositorynew.saveAll(newData);
        if (!role.getPosition().equals("1")) {
            this.keyclockRoleService.replaceAttributesForRole(role, newData);
        }
    }
    // 🔹 Recursively shift children when parent position changes



    /**
     * Retrieves a paginated list of active roles from the database.
     *
     * @param pageable the pagination and sorting information
     * @return PaginatedRoleResponseDto containing the list of active roles and pagination metadata
     */
    @Override
    public PaginatedRoleResponseDto getAllRoles(Pageable pageable) {
        return roleMapper.roleToPaginatedRoleResponseDto(roleRepository.findAllByIsActive(true, pageable));
    }

    /**
     * Retrieves an active role by its unique identifier.
     *
     * @param id the UUID of the role to retrieve
     * @return the Role entity if found and active, otherwise null
     */
    @Override
    public Role getRoleById(UUID id) {
        return roleRepository.findByIdAndIsActive(id, true);
    }

    /**
     * Filters roles based on criteria provided in RolesFilterDto
     *
     * @param rolesFilterDto the filter criteria including role name, pagination, sorting, etc.
     * @return PaginatedRoleResponseDto containing the filtered, paginated list of active roles
     */
    @Override
    public PaginatedRoleResponseDto filterRoles(RolesFilterDto rolesFilterDto) {
        if (rolesFilterDto.getName() != null && rolesFilterDto.getName().isBlank()) {
            rolesFilterDto.setName(null);
        } else if (rolesFilterDto.getName() != null) {
            rolesFilterDto.setName("%" + rolesFilterDto.getName().toLowerCase() + "%");
        }
        String roleName = request.getHeader("X-User-Roles");
        List<Role> allRoles = this.roleRepository.findAllByIsActive(true);
        Role currentUserRole = new Role();
        if (roleName != null) {
            String[] rolesArray = roleName.split(",");
            currentUserRole = allRoles.stream()
                    .filter(r -> Arrays.stream(rolesArray)
                            .anyMatch(role -> role.equalsIgnoreCase(r.getName())))
                    .findFirst()
                    .orElseThrow(() -> new SomethingWentWrongException("2--->Invalid user role."));
        }
        String currentUserPosition = currentUserRole.getPosition();
        List<String> rolePositions = allRoles.stream().map(e -> e.getPosition()).toList();
        boolean isAdmin = currentUserPosition.equals("1");
        List<String> permissibleRolePositions = rolePositions.stream().filter(e -> filterRolesBasedOnHierarchy(currentUserPosition, e, !isAdmin)).collect(Collectors.toList());
        if (isAdmin) {
            permissibleRolePositions.add("1");
        }
        Sort sort = rolesFilterDto.getSortDirection() == SortDirection.ASC ? Sort.by(rolesFilterDto.getSortField()).ascending() : Sort.by(rolesFilterDto.getSortField()).descending();
        Pageable pageable = PageRequest.of(rolesFilterDto.getPage(), rolesFilterDto.getSize(), sort);
        Page<Role> roles = roleRepository.filterRoles(rolesFilterDto.getName(), true, permissibleRolePositions, pageable);
        Page<Role> sortedPage=null;
        List<Role> rolesList=new ArrayList<>(roles.getContent());
        rolesList.sort(new HierarchyComparator());
        sortedPage = new PageImpl<>(rolesList, pageable, roles.getTotalElements());
        return roleMapper.roleToPaginatedRoleResponseDto(sortedPage);
    }

    static class HierarchyComparator implements Comparator<Role>, java.io.Serializable {
        private static final long serialVersionUID = 1L;


        @Override
        public int compare(Role r1, Role r2) {
            String posA = r1.getPosition();
            String posB = r2.getPosition();

            if (posA == null && posB == null) return 0;
            if (posA == null) return -1;
            if (posB == null) return 1;

            String[] partsA = posA.split("\\.");
            String[] partsB = posB.split("\\.");

            int len = Math.max(partsA.length, partsB.length);
            for (int i = 0; i < len; i++) {
                int numA = i < partsA.length ? Integer.parseInt(partsA[i]) : 0;
                int numB = i < partsB.length ? Integer.parseInt(partsB[i]) : 0;

                if (numA != numB) {
                    return Integer.compare(numA, numB);
                }
            }
            return 0;
        }
    }

    /**
     * Performs a soft delete of a role by its unique identifier.
     *
     * @param id the UUID of the role to delete
     * @return ResponseEntity containing ApiResponse with success status and deletion message
     * @throws RoleNotFoundException     if no active role is found with the given ID
     * @throws DeleteRoleFailedException if an error occurs during the delete operation
     */
    @Override
    public ResponseEntity<ApiResponse<JsonObject>> deleteRole(UUID id) {
        Role role = roleRepository.findByIdAndIsActive(id, true);
        if (role == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }
        UserRole userRole = this.userRoleRepository.findOneByRole(role);
        if (userRole != null) {
            throw new DeleteRoleFailedException("Deletion failed, " + role.getName() + " is already assigned to the user");
        } else {
            role.setActive(false);
            try {
                this.keyclockRoleService.deleteRealmRole(role.getKeyclockRoleId());
                roleRepository.deleteByRoleId(role.getId());
                roleRepository.deleteByRoleIdPermission(role.getId());
                roleRepository.delete(role);
                ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
                apiResponse.setStatus(STATUS_SUCCESS);
                apiResponse.setMessage(ROLE_DELETED);
                return ResponseEntity.ok(apiResponse);

            } catch (RuntimeException e) {
                throw new DeleteRoleFailedException(ROLE_DELETE_FAILED);
            }
        }

    }

    /**
     * Updates an existing active role identified by the given UUID with the provided data.
     *
     * @param id            the UUID of the role to update
     * @param roleUpdateDto the DTO containing updated role information (name and/or description)
     * @return the updated Role entity
     * @throws RoleNotFoundException     if no active role is found with the given ID
     * @throws IllegalArgumentException  if provided name or description is blank
     * @throws UpdateRoleFailedException if the update operation fails due to persistence errors
     */
    @Override
    public Role updateRole(UUID id, RoleUpdateDto roleUpdateDto) {
        try {
            Role role = roleRepository.findByIdAndIsActive(id, true);
            if (role == null) {
                throw new RoleNotFoundException(ROLE_NOT_FOUND);
            }
            if (!roleUpdateDto.getName().isBlank() && !roleUpdateDto.getName().equals("")) {
                role.setName(roleUpdateDto.getName());
            }
            if (!roleUpdateDto.getDescription().isBlank() && !roleUpdateDto.getDescription().equals("")) {
                role.setDescription(roleUpdateDto.getDescription());
            }
            role.setRoleCreate(roleUpdateDto.getRoleCreate());

            this.keyclockRoleService.updateRealmRole(role.getKeyclockRoleId(), role);
            return roleRepository.save(role);
        } catch (RuntimeException e) {
            throw new UpdateRoleFailedException(ROLE_UPDATE_FAILED);
        }
    }

    /**
     * Retrieves an active Role by its unique identifier.
     *
     * @param id the unique identifier of the Role
     * @return the active Role entity matching the given ID
     * @throws RoleNotFoundException if no active Role is found with the provided ID
     */
    @Override
    public Role roleById(UUID id) {
        Role role = this.roleRepository.findOneByIdAndIsActive(id, true);
        if (role == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }
        return role;
    }

    @Override
    public List<Role> fetchAllRoles() {
        return roleRepository.findByIsActive(true);
    }


    public List<String> getSuperiorPositions(String position) {
        List<String> superiors = new ArrayList<>();
        if (position == null || position.isEmpty()) return superiors;

        String[] parts = position.split("\\."); // split by dot
        // Build from most specific to least
        for (int i = parts.length - 1; i > 0; i--) {
            String superior = String.join(".", Arrays.copyOfRange(parts, 0, i));
            superiors.add(superior);
        }
        // Always add top-level 1
        if (!superiors.contains("1")) superiors.add("1");
        return superiors;
    }


    @Override
    public List<Role> dropDown() {
        String roleName = request.getHeader("X-User-Roles");
        List<Role> allRoles = this.roleRepository.findAllByIsActive(true);
        Role currentUserRole = new Role();
        if (roleName != null) {
            String[] rolesArray = roleName.split(",");
            currentUserRole = allRoles.stream()
                    .filter(r -> Arrays.stream(rolesArray)
                            .anyMatch(role -> role.equalsIgnoreCase(r.getName())))
                    .findFirst()
                    .orElseThrow(() -> new SomethingWentWrongException("4--->Invalid user role."));
        }
        String currentUserPosition = currentUserRole.getPosition();
        boolean isAdmin = currentUserPosition.equals("1");
        List<Role> permittedRoles = new ArrayList<>();
        for (Role role : allRoles) {
            if (filterRolesBasedOnHierarchy(currentUserPosition, role.getPosition(), !isAdmin)) {
                permittedRoles.add(role);
            }
        }
        permittedRoles.sort(new HierarchyComparator());
        return permittedRoles;
    }

    public boolean filterRolesBasedOnHierarchy(String currentRolePosition, String rolePosition, boolean isSubOnly) {
        if (currentRolePosition.equals(rolePosition)) {
            return false;
        }
        if (isSubOnly) {
            return rolePosition.startsWith(currentRolePosition + ".");
        } else {
            String[] rolePositionSplit = rolePosition.split("\\.");
            String[] currentRolePositionSplit = currentRolePosition.split("\\.");
            return Integer.parseInt(rolePositionSplit[0]) > Integer.parseInt(currentRolePositionSplit[0]);
        }
    }
}
