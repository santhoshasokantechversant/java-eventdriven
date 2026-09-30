package com.techversant.userservice.service.impl;

import com.techversant.userservice.dto.SaveRolePermissionDto;
import com.techversant.userservice.dto.UpdatePermissionDto;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.repository.RoleRepository;
import com.techversant.userservice.repository.privilegesnew.PermissionRepositorynew;
import com.techversant.userservice.service.IPermissionService;
import com.techversant.userservice.service.keyclock.KeyclockRoleService;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.exceptions.PermissionUpdateException;
import com.techversant.userservice.utils.exceptions.SomethingWentWrongException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService implements IPermissionService {
    private final PermissionRepositorynew permissionRepositorynew;
    private final RoleRepository roleRepository;
    private final KeyclockRoleService keyclockRoleService;
    private final HttpServletRequest request;
    private final RoleService roleService;

    public PermissionService(RoleService roleService, HttpServletRequest request, KeyclockRoleService keyclockRoleService, RoleRepository roleRepository, PermissionRepositorynew permissionRepositorynew) {
        this.permissionRepositorynew = permissionRepositorynew;
        this.roleRepository = roleRepository;
        this.keyclockRoleService = keyclockRoleService;
        this.request = request;
        this.roleService = roleService;
    }

    @Async
    public void addPermissions(List<PermissionsNewEntity> permissions) {
        try {
            this.permissionRepositorynew.saveAll(permissions);
            List<Role> role = this.roleRepository.findAllByIsActive(true);
            List<PermissionsNewEntity> allPermissions = this.permissionRepositorynew.findAll();

            if (!role.isEmpty() && !allPermissions.isEmpty()) {
                this.keyclockRoleService.replaceAllAttributesForRoles(role, allPermissions);
            }

        } catch (RuntimeException e) {
            throw new PermissionUpdateException("Failed to add or update permissions", e);
        }
    }


    @Override
    public PermissionsNewEntity updatePermissions(UUID id, UpdatePermissionDto updatePermissionDto) {
        try {
            Optional<PermissionsNewEntity> permissionsNew = this.permissionRepositorynew.findById(id);

            if (permissionsNew.isEmpty()) {
                throw new SomethingWentWrongException("Permission not found with ID: " + id);
            }

            PermissionsNewEntity permission = permissionsNew.get();
            UUID roleId = permission.getRoleId();
            List<PermissionsNewEntity> permissions = this.permissionRepositorynew.findAllByPrivilegeIdAndRoleId(permission.getPrivilegeId(), roleId);

            if (permissions == null || permissions.isEmpty()) {
                throw new SomethingWentWrongException("No permissions found for privilege ID: " + id);
            }


            List<PermissionsNewEntity> permissionsToSave = new ArrayList<>();

            for (PermissionsNewEntity per : permissions) {

                boolean updated = false;

                // Update slug name if it changed
                if (!per.getSlugName().equals(updatePermissionDto.getSlugName())) {
                    per.setSlugName(updatePermissionDto.getSlugName());
                    per.setUpdatableSlug(PermissionType.NO);
                    updated = true;
                }
                // Update side nav if YES is passed
                if (updatePermissionDto.getSideNav() != null &&
                        !per.getSideNav().equals(updatePermissionDto.getSideNav())) {
                    per.setSideNav(updatePermissionDto.getSideNav());
                    updated = true;
                }

                if (updated) {
                    permissionsToSave.add(per);
                }
            }

            if (!permissionsToSave.isEmpty()) {
                this.permissionRepositorynew.saveAll(permissionsToSave);
            }
            Role role = this.roleRepository.findByIdAndIsActive(roleId, true);
            List<PermissionsNewEntity> allPermissions = this.permissionRepositorynew.findAllByRoleId(roleId);
            if (role != null && !allPermissions.isEmpty()) {
                this.keyclockRoleService.replaceAttributesForRole(role, allPermissions);
            }
            return permissions.get(0);
        } catch (RuntimeException e) {
            throw new PermissionUpdateException("Failed to update permissions for ID: " + id, e);
        }
        }



    @Transactional
    @Override
    public List<PermissionsNewEntity> saveRolesPermissions(UUID id, List<SaveRolePermissionDto> saveRolePermissionDto) {
        List<PermissionsNewEntity> allPermissions = this.permissionRepositorynew.findAllByRoleId(id);

// Map existing permissions by ID for quick lookup
        Map<UUID, PermissionsNewEntity> permissionMap = allPermissions.stream()
                .collect(Collectors.toMap(PermissionsNewEntity::getId, p -> p));

        List<PermissionsNewEntity> updatedPermissions = new ArrayList<>();

        for (SaveRolePermissionDto permissionSave : saveRolePermissionDto) {
            if (permissionSave.getId() != null) {
                PermissionsNewEntity permission = permissionMap.get(permissionSave.getId());
                if (permission != null) {
                    permission.setPermissionType(permissionSave.getPermissionType());
                    updatedPermissions.add(permission);
                }
            }
        }

        this.permissionRepositorynew.saveAll(updatedPermissions);
        Role role = this.roleRepository.findByIdAndIsActive(id, true);
        List<PermissionsNewEntity> updatePermision = this.permissionRepositorynew.findAllByRoleId(id);
        if (role != null && !updatePermision.isEmpty()) {
            this.keyclockRoleService.replaceAttributesForRole(role, updatePermision);
        }
        return updatePermision;
    }

    @Override
    public PermissionsNewEntity getPermissions(UUID id) {
        return permissionRepositorynew.findById(id)
                .orElseThrow(() -> new SomethingWentWrongException("No Permission found for the given id"));
    }


    @Override
    public List<Map<String, Object>> listPermissions(UUID roleId) {
        String roleNameHeader = request.getHeader("X-User-Roles");
        List<Role> allRoles = roleRepository.findAllByIsActive(true);

        Role roleDetails = getRoleDetails(roleId);
        Role currentUserRole = resolveCurrentUserRole(roleNameHeader, allRoles);

        List<PermissionsNewEntity> permissions = permissionRepositorynew.findAllByRoleId(roleId);
        Map<String, List<PermissionsNewEntity>> groupedPermissions = groupPermissionsByPrivilege(permissions);

        List<PermissionsNewEntity> superiorPermissions = getSuperiorPermissions(currentUserRole, roleId, roleDetails);

        return buildPermissionResponse(groupedPermissions, currentUserRole, superiorPermissions, roleId);
    }


    private Role getRoleDetails(UUID roleId) {
        Role roleDetails = roleRepository.findByIdAndIsActive(roleId, true);
        if (roleDetails == null) {
            throw new SomethingWentWrongException("Role not found for ID: " + roleId);
        }
        return roleDetails;
    }

    private Role resolveCurrentUserRole(String roleNameHeader, List<Role> allRoles) {
        if (roleNameHeader == null) {
            return null;
        }

        String[] rolesArray = roleNameHeader.split(",");
        Role currentUserRole = allRoles.stream()
                .filter(r -> Arrays.stream(rolesArray)
                        .anyMatch(role -> role.equalsIgnoreCase(r.getName())))
                .findFirst()
                .orElseThrow(() -> new SomethingWentWrongException("5--->Invalid user role."));

        if (PermissionType.NO.equals(currentUserRole.getRoleCreate())) {
            throw new SomethingWentWrongException("You don't have access to create a role");
        }

        return currentUserRole;
    }

    private Map<String, List<PermissionsNewEntity>> groupPermissionsByPrivilege(List<PermissionsNewEntity> permissions) {
        return permissions.stream()
                .collect(Collectors.groupingBy(PermissionsNewEntity::getPrivilegeName));
    }

    private List<PermissionsNewEntity> getSuperiorPermissions(Role currentUserRole, UUID roleId, Role roleDetails) {
        if (currentUserRole == null
                || currentUserRole.getId().equals(roleId)
                || "1".equals(currentUserRole.getPosition())) {
            return List.of();
        }

        List<String> superiorPositions = roleService.getSuperiorPositions(roleDetails.getPosition());
        if (!superiorPositions.contains(currentUserRole.getPosition())) {
            return List.of();
        }

        return permissionRepositorynew.findAllByRoleId(currentUserRole.getId()).stream()
                .filter(p -> PermissionType.YES.equals(p.getPermissionType()))
                .toList();
    }

    private List<Map<String, Object>> buildPermissionResponse(
            Map<String, List<PermissionsNewEntity>> groupedPermissions,
            Role currentUserRole,
            List<PermissionsNewEntity> superiorPermissions,
            UUID roleId) {

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map.Entry<String, List<PermissionsNewEntity>> entry : groupedPermissions.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey());
            map.put("privilegeId", entry.getValue().isEmpty() ? null : entry.getValue().get(0).getPrivilegeId());

            List<PermissionsNewEntity> sortedPermissions = entry.getValue().stream()
                    .sorted(Comparator.comparing(PermissionsNewEntity::getEndpointName,
                            Comparator.nullsLast(String::compareTo)))
                    .map(p -> buildCopyWithUpdatable(p, currentUserRole, superiorPermissions, roleId))
                    .toList();

            map.put("values", sortedPermissions);
            response.add(map);
        }
        return response;
    }

    private PermissionsNewEntity buildCopyWithUpdatable(
            PermissionsNewEntity p,
            Role currentUserRole,
            List<PermissionsNewEntity> superiorPermissions,
            UUID roleId) {

        PermissionsNewEntity copy = new PermissionsNewEntity();
        copy.setId(p.getId());
        copy.setPrivilegeId(p.getPrivilegeId());
        copy.setEndpointId(p.getEndpointId());
        copy.setPrivilegeName(p.getPrivilegeName());
        copy.setEndpointName(p.getEndpointName());
        copy.setSlugName(p.getSlugName());
        copy.setBackendUrl(p.getBackendUrl());
        copy.setHttpMethod(p.getHttpMethod());
        copy.setRoleId(p.getRoleId());
        copy.setUserId(p.getUserId());
        copy.setPosition(p.getPosition());
        copy.setPermissionType(p.getPermissionType());
        copy.setSideNav(p.getSideNav());
        copy.setUpdatableEndpoint(p.getUpdatableEndpoint());
        copy.setUpdatableSlug(p.getUpdatableSlug());
        copy.setActive(p.isActive());
        copy.setUpdatable(determineUpdatable(copy, currentUserRole, superiorPermissions, roleId));

        return copy;
    }

    private String determineUpdatable(
            PermissionsNewEntity copy,
            Role currentUserRole,
            List<PermissionsNewEntity> superiorPermissions,
            UUID roleId) {

        if (currentUserRole == null) return "NO";
        if (currentUserRole.getId().equals(roleId)) return "NO";
        if ("1".equals(currentUserRole.getPosition())) return "YES";

        boolean match = superiorPermissions.stream()
                .anyMatch(sp -> sp.getPrivilegeId().equals(copy.getPrivilegeId())
                        && sp.getEndpointId().equals(copy.getEndpointId())
                        && PermissionType.YES.equals(sp.getPermissionType()));

        return match ? "YES" : "NO";
    }


}
