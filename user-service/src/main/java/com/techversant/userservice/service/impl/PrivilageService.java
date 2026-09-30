/**
 * @file PrivilageService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This Service file is for Privilage Service
 */
package com.techversant.userservice.service.impl;

import com.techversant.userservice.dto.*;
import com.techversant.userservice.mapper.EndpointMapper;
import com.techversant.userservice.mapper.PermissionMapper;
import com.techversant.userservice.mapper.PrivilegesMapper;
import com.techversant.userservice.mapper.SideNavMapper;
import com.techversant.userservice.model.*;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import com.techversant.userservice.model.privileges.SideNavNewEntity;
import com.techversant.userservice.repository.*;
import com.techversant.userservice.repository.privilegesnew.PermissionRepositorynew;
import com.techversant.userservice.repository.privilegesnew.PrivilegesRepositoryNew;
import com.techversant.userservice.repository.privilegesnew.SidenavRepositoryNew;
import com.techversant.userservice.service.IPrivilegeService;
import com.techversant.userservice.service.keyclock.KeyclockRoleService;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.techversant.userservice.utils.Constants.*;

@Service
public class PrivilageService implements IPrivilegeService {
    private static final Logger logger = LoggerFactory.getLogger(PrivilageService.class);
    private final PrivilegeRepository privilegeRepositor;
    private final PrivilegesMapper privilegesMapper;
    private final EndpointRepository endpointRepository;
    private final EndpointMapper endpointMapper;
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeEndpointRepository privilegeEndpointRepository;
    private final KeyclockRoleService keyclockRoleService;
    private final SideNavMapper sideNavMapper;
    private final SideNavRepository sideNavRepository;
    private final PermissionRepositorynew permissionRepositorynew;
    private final PrivilegesRepositoryNew privilegesRepositoryNew;
    private final SidenavRepositoryNew sidenavRepositoryNew;
    private final PermissionService permissionService;
    private final IPrivilegeService selfProxy;
    private static final String PRIVILEGE_NOT_FOUND = "No privilege found for the given id.";

    public PrivilageService(PermissionService permissionService, SidenavRepositoryNew sidenavRepositoryNew, PrivilegesRepositoryNew privilegesRepositoryNew, PermissionRepositorynew permissionRepositorynew, KeyclockRoleService keyclockRoleService, SideNavMapper sideNavMapper, SideNavRepository sideNavRepository, UserRepository userRepository, PrivilegeEndpointRepository privilegeEndpointRepository, PrivilegeRepository privilegeRepositor, RoleRepository roleRepository, PermissionRepository permissionRepository, PermissionMapper permissionMapper, EndpointMapper endpointMapper, PrivilegesMapper privilegesMapper, EndpointRepository endpointRepository,@Lazy IPrivilegeService selfProxy) {
        this.privilegeRepositor = privilegeRepositor;
        this.privilegesMapper = privilegesMapper;
        this.endpointRepository = endpointRepository;
        this.endpointMapper = endpointMapper;
        this.permissionMapper = permissionMapper;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.privilegeEndpointRepository = privilegeEndpointRepository;
        this.keyclockRoleService = keyclockRoleService;
        this.sideNavRepository = sideNavRepository;
        this.sideNavMapper = sideNavMapper;
        this.permissionRepositorynew = permissionRepositorynew;
        this.privilegesRepositoryNew = privilegesRepositoryNew;
        this.sidenavRepositoryNew = sidenavRepositoryNew;
        this.permissionService = permissionService;
        this.selfProxy = selfProxy;
    }

    /**
     * Creates a new Privileges entity after checking for duplicates based on the privilege name.
     * If a privilege with the same name already exists and is active, a UserNameAlreadyExistsException is thrown.
     *
     * @param privilegesDto the data transfer object containing privilege details to be added
     * @return the newly created Privileges entity
     * @throws UserNameAlreadyExistsException if a privilege with the same name already exists and is active
     */
    @Override
    public Privileges addPrivileges(PrivilegesDto privilegesDto) {
        Privileges getDuplicate = this.privilegeRepositor.findOneByNameAndIsActive(privilegesDto.getName(), true);
        if (getDuplicate != null) {
            throw new UserNameAlreadyExistsException(PRIVILEGE_ALREADY_EXISTS);
        }
        Privileges save = this.privilegeRepositor.save(privilegesMapper.privilegeDtoToEntity(privilegesDto, new Privileges()));
        SideNav subPrivilege = new SideNav();
        if (privilegesDto.getSubName() != null && !privilegesDto.getSubName().equals("")) {
            UUID subPriv = UUID.fromString(privilegesDto.getSubName());
            subPrivilege = this.sideNavRepository.findByIdAndIsActive(subPriv, true);
        }
        this.sideNavRepository.save(sideNavMapper.sideNavDtoToEntity(privilegesDto, new SideNav(), subPrivilege));
        List<Endpoint> endpoints = this.endpointRepository.findAllByIsActive(true);
        if (!endpoints.isEmpty()) {
            List<Role> roles = this.roleRepository.findAllByIsActive(true);

            // Collect PrivilegeEndpoint list
            List<PrivilageEndpoint> privilegeEndpoints = roles.stream()
                    .flatMap(role -> endpoints.stream().map(endpoint -> {
                        PrivilageEndpoint privilageEnd = new PrivilageEndpoint();
                        privilageEnd.setPrivileges(save);
                        privilageEnd.setEndpoint(endpoint);
                        return privilageEnd;
                    }))
                    .toList();

            // Save all PrivilegeEndpoints at once
            this.privilegeEndpointRepository.saveAll(privilegeEndpoints);
            List<Permission> permissions = roles.stream()
                    .flatMap(role -> {
                        // Build Permission + PermissionKeycloakDto for each endpoint
                        List<Permission> rolePermissions = endpoints.stream()
                                .map(end -> {
                                    Permission permission = new Permission();
                                    permission.setPrivileges(save);
                                    permission.setRole(role);
                                    permission.setEndpoint(end);
                                    permission.setSlugName(privilegesDto.getSlugName());
                                    permission.setPermissionType(PermissionType.NO);
                                    return permission;
                                })
                                .toList();

                        // Build matching DTO list
                        List<PermissionKeyclockDto> permissionKeycloakDtoArr = rolePermissions.stream()
                                .map(perm -> {
                                    PermissionKeyclockDto dto = new PermissionKeyclockDto();
                                    dto.setPermission(perm.getPermissionType());
                                    dto.setMethod(perm.getEndpoint().getHttpMethod());
                                    dto.setModule(perm.getPrivileges().getName());
                                    dto.setEndpoint(perm.getPrivileges().getUrl());
                                    return dto;
                                })
                                .toList();

                        // Update Keycloak for this role
                        this.keyclockRoleService.updateNewAttributeRole(role.getKeyclockRoleId(), permissionKeycloakDtoArr);

                        return rolePermissions.stream();
                    })
                    .toList();

// Save all permissions at once
            this.permissionRepository.saveAll(permissions);


        }

        return save;
    }

    /**
     * Creates a new Endpoint entity after checking for duplicates based on the endpoint name and URL.
     * If an endpoint with the same name or URL already exists and is active, a UserNameAlreadyExistsException is thrown.
     *
     * @param endpointDto the data transfer object containing endpoint details to be added
     * @return the newly created Endpoint entity
     * @throws UserNameAlreadyExistsException if an endpoint with the same name or URL already exists and is active
     */
    @Override
    public Endpoint addEndPoints(EndpointDto endpointDto) {
        Endpoint getDuplicate = this.endpointRepository.findOneByHttpMethodAndIsActive(endpointDto.getHttpMethod(), true);
        if (getDuplicate != null) {
            throw new UserNameAlreadyExistsException(HTTP_METHODS_ALREADY_EXISTS);
        }
        List<Privileges> privileges = this.privilegeRepositor.findAllByIsActive(true);
        Endpoint saveEndpoint = this.endpointRepository.save(endpointMapper.endpointDtoToEntity(endpointDto, new Endpoint()));
        List<Role> roles = this.roleRepository.findAllByIsActive(true);
        if (!roles.isEmpty()) {
            // Save permissions in DB
            this.permissionRepository.saveAll(
                    roles.stream()
                            .flatMap(role -> privileges.stream().map(priv -> {
                                // Create PrivilegeEndpoint
                                PrivilageEndpoint privilageEnd = new PrivilageEndpoint();
                                privilageEnd.setPrivileges(priv);
                                privilageEnd.setEndpoint(saveEndpoint);
                                this.privilegeEndpointRepository.save(privilageEnd);

                                // Create Permission
                                Permission perm = new Permission();
                                perm.setEndpoint(saveEndpoint);
                                perm.setRole(role);
                                perm.setPrivileges(priv);
                                perm.setPermissionType(PermissionType.NO);
                                return perm;
                            }))
                            .toList()
            );


            // Update Keycloak role permissions for each role
            roles.forEach(role -> {
                List<Permission> permission =
                        this.permissionRepository.findAllByRoleAndIsActive(role, true);
                List<PermissionKeyclockDto> permissionKeycloakDtoArr =
                        permission.stream()
                                .map(pe -> {
                                    PermissionKeyclockDto permK = new PermissionKeyclockDto();
                                    permK.setEndpoint(pe.getPrivileges().getUrl());
                                    permK.setModule(pe.getPrivileges().getName());
                                    permK.setMethod(pe.getEndpoint().getHttpMethod());
                                    permK.setPermission(
                                            pe.getPermissionType() == PermissionType.YES
                                                    ? PermissionType.YES
                                                    : PermissionType.NO
                                    );
                                    return permK;
                                })
                                .toList();

                this.keyclockRoleService.updateNewAttributeRole(role.getKeyclockRoleId(), permissionKeycloakDtoArr);
            });
        }

        return saveEndpoint;
    }

    /**
     * Creates a new Permission entity after validating the provided role, privilege, endpoint, and user.
     * If a permission with the same combination of role, privilege, endpoint, and user already exists and is active,
     * an InvalidUuidException is thrown.
     *
     * @param permissionDto the data transfer object containing permission details to be added
     * @return the newly created Permission entity
     * @throws InvalidUuidException if a permission with the same role, privilege, endpoint, and user already exists and is active
     */
    @Override
    public Permission addPermission(PermissionDto permissionDto) {
        User user;
        Role role = new Role();
        Privileges privilege = new Privileges();
        Endpoint endpoint = new Endpoint();
        Permission permission;
        if (permissionDto.getRoleId() != null) {
            role = this.getByRoleId(permissionDto.getRoleId());
        }
        if (permissionDto.getPrivilegeId() != null) {
            privilege = this.getByPrivilegesId(permissionDto.getPrivilegeId());
        }
        if (permissionDto.getEndpointId() != null) {
            endpoint = this.getByEndpointId(permissionDto.getEndpointId());
        }
        if (permissionDto.getUserId() != null) {
            user = this.getByUserId(permissionDto.getUserId());
            permission = this.permissionRepository.findOneByRoleAndPrivilegesAndEndpointAndUserAndIsActive(role, privilege, endpoint, user, true);
            if (permission != null) {
                throw new InvalidUuidException(INVALID_ENDPOINT_ID);
            }
            return this.permissionRepository.save(permissionMapper.permissionDtoToEntity(user, endpoint, privilege, role, new Permission()));

        }
        permission = this.permissionRepository.findOneByRoleAndPrivilegesAndEndpointAndIsActive(role, privilege, endpoint, true);
        if (permission != null) {
            throw new InvalidUuidException(INVALID_ENDPOINT_ID);
        }
        return this.permissionRepository.save(permissionMapper.permissionDtoToEntity(endpoint, privilege, role, new Permission()));
    }

    /**
     * Retrieves a list of active permissions associated with a specific active role.
     *
     * @param id the UUID of the role whose permissions are to be listed
     * @return a list of active Permission objects linked to the specified role
     * @throws RoleNotFoundException if the role with the given ID does not exist or is inactive
     */
    @Override
    public List<Permission> listPermission(UUID id) {

        Role role = this.roleRepository.findByIdAndIsActive(id, true);
        if (role.getId() == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }
        return this.permissionRepository.findAllByRoleAndIsActive(role, true);
    }

    /**
     * Updates the permissions for a given active role.
     *
     * @param id         the UUID of the role whose permissions are to be updated
     * @param permission the list of Permission objects containing updated permission details
     * @return true if the update is successful
     * @throws RoleNotFoundException       if the role with the given ID does not exist or is inactive
     * @throws SomethingWentWrongException if an error occurs during the update process
     */
    @Override
    public boolean upatePermission(UUID id, List<Permission> permission) {
        try {
            Role role = this.roleRepository.findByIdAndIsActive(id, true);
            if (role.getId() == null) {
                throw new RoleNotFoundException(ROLE_NOT_FOUND);
            }
            List<PermissionKeyclockDto> permissionKeyclockDtos = permission.stream()
                    .map(p -> {
                        PermissionKeyclockDto dto = new PermissionKeyclockDto();
                        dto.setModule(p.getPrivileges().getName());
                        dto.setEndpoint(p.getPrivileges().getUrl());
                        dto.setMethod(p.getEndpoint().getHttpMethod());
                        dto.setPermission(p.getPermissionType());
                        return dto;
                    })
                    .toList();
            this.keyclockRoleService.updateRole(role.getKeyclockRoleId(), permissionKeyclockDtos);
            this.permissionRepository.saveAll(permission);
            return true;
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to Update Permissions");
        }
    }

    /**
     * Retrieves an active Endpoint entity by its unique identifier (UUID).
     * If the endpoint is not found or an error occurs during retrieval, exceptions are thrown.
     *
     * @param id the UUID of the Endpoint to search for
     * @return the Endpoint entity matching the given UUID and active status
     * @throws UserNotFoundException       if the endpoint with the specified ID is not found
     * @throws SomethingWentWrongException if an error occurs while retrieving the endpoint
     */
    public Endpoint getByEndpointId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Endpoint endpoint = this.endpointRepository.findByIdAndIsActive(uuid, true);
            if (endpoint == null) {
                throw new UserNotFoundException(ENDPOINT_NOT_FOUND);
            }
            return endpoint;
        } catch (SomethingWentWrongException e) {
            throw new SomethingWentWrongException(INVALID_ENDPOINT_ID);
        }
    }

    /**
     * Retrieves an active Privileges entity by its unique identifier (UUID).
     * If the privilege is not found or an error occurs during retrieval, exceptions are thrown.
     *
     * @param id the UUID of the Privileges to search for
     * @return the Privileges entity matching the given UUID and active status
     * @throws UserNotFoundException       if the privilege with the specified ID is not found
     * @throws SomethingWentWrongException if an error occurs while retrieving the privilege
     */
    public Privileges getByPrivilegesId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Privileges privileges = this.privilegeRepositor.findByIdAndIsActive(uuid, true);
            if (privileges == null) {
                throw new UserNotFoundException(PRIVILEGE_NOT_FOUND);
            }
            return privileges;
        } catch (SomethingWentWrongException e) {
            throw new SomethingWentWrongException(INVALID_ENDPOINT_ID);
        }
    }

    /**
     * Retrieves an active Role entity by its unique identifier (UUID).
     * If the role is not found or an error occurs during retrieval, exceptions are thrown.
     *
     * @param id the UUID of the Role to search for
     * @return the Role entity matching the given UUID and active status
     * @throws UserNotFoundException       if the role with the specified ID is not found
     * @throws SomethingWentWrongException if an error occurs while retrieving the role
     */
    public Role getByRoleId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Role role = this.roleRepository.findByIdAndIsActive(uuid, true);
            if (role == null) {
                throw new UserNotFoundException(ROLE_NOT_FOUND);
            }
            return role;
        } catch (SomethingWentWrongException e) {
            throw new SomethingWentWrongException(INVALID_ENDPOINT_ID);
        }
    }

    /**
     * Retrieves an active User entity by its unique identifier (UUID).
     * If the user is not found or an error occurs during retrieval, exceptions are thrown.
     *
     * @param id the UUID of the User to search for
     * @return the User entity matching the given UUID and active status
     * @throws UserNotFoundException       if the user with the specified ID is not found
     * @throws SomethingWentWrongException if an error occurs while retrieving the user
     */
    public User getByUserId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            User user = this.userRepository.findByIdAndIsActive(uuid, true);
            if (user == null) {
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            return user;
        } catch (SomethingWentWrongException e) {
            throw new SomethingWentWrongException(INVALID_ENDPOINT_ID);
        }
    }


    @Override
    public List<SideNavDTO> listSideNav() {
        List<SideNav> sideNavs = this.sideNavRepository.findAllByIsActive(true);
        return sideNavs.stream()
                .map(nav -> {
                    SideNavDTO dto = new SideNavDTO();
                    dto.setId(nav.getId());
                    dto.setName(nav.getName());
                    dto.setSlugName(nav.getSlugName());
                    dto.setIcon(nav.getIcon());
                    dto.setPosition(nav.getPosition());
                    dto.setActive(nav.isActive());
                    dto.setCreatedAt(nav.getCreatedAt());
                    dto.setUpdatedAt(nav.getUpdatedAt());
                    // Set subPrivileges to empty list to avoid nested children
                    dto.setSubPrivileges(List.of());
                    return dto;
                })
                .toList();
    }

    @Override
    public Permission updatePermission(UUID id, Permission permission) {
        if(permission.getSideNav() == PermissionType.NO) {
            permission.setSideNav(PermissionType.NO);
        } else {
            permission.setSideNav(PermissionType.YES);
        }
        Permission existingPermission = permissionRepository.findByIdAndIsActive(id, true);
        if (existingPermission == null) {
            throw new SomethingWentWrongException("Permission Id is Not valid.");
        }

        // Fetch all permissions for the same role + privilege
        List<Permission> listPermission = permissionRepository.findAllByRoleAndPrivilegesAndIsActive(
                existingPermission.getRole(), existingPermission.getPrivileges(), true
        );

        // Update only the slugName with the new value from the input
        // Update slugName and sideNav for each permission
        listPermission.forEach(p -> {
            p.setSlugName(permission.getSlugName());
            p.setSideNav(permission.getSideNav());
        });
        // Save all updated permissions
        permissionRepository.saveAll(listPermission);

        return existingPermission;
    }


    @Override
    public Permission getPermission(UUID id) {
        Permission permissions = permissionRepository.findByIdAndIsActive(id, true);
        if (permissions.getId() == null) {
            throw new SomethingWentWrongException("Permission Id is Not valid.");
        }
        return permissions;
    }


    @Override
    public List<SidenavResponseDto> sideNav(UUID id) {
        Role role = this.roleRepository.findOneByIdAndIsActive(id, true);
        if (role == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }

        List<Permission> permissions = permissionRepository.findAllByRoleAndIsActive(role, true);

        // Build permission map
        Map<String, PermissionResponseDto> permissionMap =
                permissions.stream()
                        .collect(Collectors.toMap(
                                per -> per.getPrivileges().getName(),
                                per -> new PermissionResponseDto(
                                        per.getPrivileges().getName(),
                                        per.getSlugName(),
                                        new ArrayList<>(List.of(per.getEndpoint().getHttpMethod())),
                                        per.getPermissionType(),
                                        per.getSideNav()
                                ),
                                (existing, replacement) -> {
                                    existing.getHttpMethod().addAll(replacement.getHttpMethod());
                                    if (replacement.getPermissionType() == PermissionType.YES) {
                                        existing.setPermissionType(PermissionType.YES);
                                    }
                                    return existing;
                                }
                        ));

        // Fetch root privileges (parent = null)
        List<SideNav> rootPrivileges = this.sideNavRepository.findByParentIsNullOrderByPositionAsc();

        return rootPrivileges.stream()
                .map(nav -> mapToResponse(nav, permissionMap))
                .filter(Objects::nonNull)
                .toList();
    }


    @Override
    public List<SidenavResponseDto> sideNavNew(UUID roleId) {
        Role role = this.roleRepository.findOneByIdAndIsActive(roleId, true);
        if (role == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }

        List<PermissionsNewEntity> permissions = permissionRepositorynew.findAllByRoleId(roleId);

        // Build permission map safely with mutable lists
        Map<String, PermissionResponseDto> permissionMap = permissions.stream()
                .collect(Collectors.toMap(
                        PermissionsNewEntity::getPrivilegeName,
                        per -> new PermissionResponseDto(
                                per.getPrivilegeName(),
                                per.getSlugName(),
                                per.getHttpMethod() != null ? new ArrayList<>(List.of(per.getHttpMethod())) : new ArrayList<>(),
                                per.getPermissionType(),
                                per.getSideNav()
                        ),
                        (existing, replacement) -> {
                            existing.getHttpMethod().addAll(replacement.getHttpMethod());
                            if (replacement.getPermissionType() == PermissionType.YES) {
                                existing.setPermissionType(PermissionType.YES);
                            }
                            if (replacement.getSideNav() == PermissionType.YES) {
                                existing.setSideNav(PermissionType.YES);
                            }
                            return existing;
                        }
                ));

        // Fetch root privileges (parent = null)
        List<SideNavNewEntity> rootPrivileges = this.sidenavRepositoryNew.findByParentIsNullOrderByPositionAsc();

        // Map root privileges recursively
        return rootPrivileges.stream()
                .map(nav -> mapToResponseNew(nav, permissionMap))
                .filter(Objects::nonNull)
                .toList();
    }

    // Recursive mapper
    private SidenavResponseDto mapToResponseNew(SideNavNewEntity sideNav, Map<String, PermissionResponseDto> permissionMap) {
        PermissionResponseDto permissionDto = permissionMap.get(sideNav.getName());

        // Process children
        List<SidenavResponseDto> allowedChildren = new ArrayList<>();
        if (sideNav.getSubPrivileges() != null && !sideNav.getSubPrivileges().isEmpty()) {
            allowedChildren = sideNav.getSubPrivileges()
                    .stream()
                    .map(sub -> mapToResponseNew(sub, permissionMap))
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Case 1: Parent has YES permission and sideNav = YES
        if (permissionDto != null &&
                permissionDto.getPermissionType() == PermissionType.YES &&
                permissionDto.getSideNav() == PermissionType.YES) {

            if (!sideNav.getSubPrivileges().isEmpty() && allowedChildren.isEmpty()) {
                return null; // skip parent if children exist but none allowed
            }

            SidenavResponseDto dto = buildDtoNew(sideNav, permissionDto, !sideNav.getSubPrivileges().isEmpty());
            dto.setSlugName(permissionDto.getSlugName()); // override slug
            dto.setSubPrivileges(allowedChildren);
            return dto;
        }

        // Case 2: Parent = NO but allowed children exist
        if (!allowedChildren.isEmpty()) {
            SidenavResponseDto dto = buildDtoNew(sideNav, null, !sideNav.getSubPrivileges().isEmpty());
            dto.setSubPrivileges(allowedChildren);
            return dto;
        }

        // Case 3: Parent = NO and children = NO → skip
        return null;
    }

    // Build DTO from entity
    private SidenavResponseDto buildDtoNew(SideNavNewEntity sideNav, PermissionResponseDto permissionDto, boolean isParent) {
        SidenavResponseDto dto = new SidenavResponseDto();
        dto.setId(sideNav.getId().toString());
        dto.setModuleName(sideNav.getName());
        dto.setSlugName(permissionDto != null ? permissionDto.getSlugName() : sideNav.getSlugName());
        dto.setIcon(sideNav.getIcon());
        dto.setPosition(sideNav.getPosition());

        if (isParent) {
            dto.setAccess(List.of()); // parent with children → no access
        } else if (permissionDto != null) {
            List<String> access = permissionDto.getHttpMethod()
                    .stream()
                    .map(this::mapHttpMethod)
                    .distinct()
                    .toList();
            dto.setAccess(access);
        } else {
            dto.setAccess(List.of());
        }

        return dto;
    }



    private SidenavResponseDto mapToResponse(SideNav sideNav, Map<String, PermissionResponseDto> permissionMap) {
        PermissionResponseDto permissionDto = permissionMap.get(sideNav.getName());

        // Process children
        List<SidenavResponseDto> allowedChildren = new ArrayList<>();
        if (sideNav.getSubPrivileges() != null && !sideNav.getSubPrivileges().isEmpty()) {
            allowedChildren = sideNav.getSubPrivileges()
                    .stream()
                    .map(sub -> mapToResponse(sub, permissionMap))
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Case 1: Parent = YES
        if (permissionDto != null && permissionDto.getPermissionType() == PermissionType.YES && permissionDto.getSideNav() == PermissionType.YES) {
            if (!sideNav.getSubPrivileges().isEmpty() && allowedChildren.isEmpty()) {
                return null; // skip parent if it has children but none allowed
            }
            SidenavResponseDto dto = buildDto(sideNav, permissionDto, !sideNav.getSubPrivileges().isEmpty());
            dto.setSlugName(permissionDto.getSlugName()); // override slug with permission's slug
            dto.setSubPrivileges(allowedChildren);
            return dto;
        }

        // Case 2: Parent = NO but children exist and are allowed
        if (!allowedChildren.isEmpty()) {
            SidenavResponseDto dto = buildDto(sideNav, null, !sideNav.getSubPrivileges().isEmpty());
            dto.setSubPrivileges(allowedChildren);
            return dto;
        }

        // Case 3: Parent = NO and children = NO → skip
        return null;
    }


    private SidenavResponseDto buildDto(SideNav sideNav, PermissionResponseDto permissionDto, boolean isParent) {
        SidenavResponseDto dto = new SidenavResponseDto();
        dto.setId(sideNav.getId().toString());
        dto.setModuleName(sideNav.getName());
        dto.setSlugName(sideNav.getSlugName());
        dto.setIcon(sideNav.getIcon());
        dto.setPosition(sideNav.getPosition());

        if (isParent) {
            // All parents (any node with children) → no access
            dto.setAccess(List.of());
        } else if (permissionDto != null) {
            List<String> access = permissionDto.getHttpMethod().stream()
                    .map(this::mapHttpMethod)
                    .distinct()
                    .toList();
            dto.setAccess(access);
        } else {
            dto.setAccess(List.of());
        }

        return dto;
    }


    private String mapHttpMethod(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> "LIST";
            case "POST" -> "CREATE";
            case "PUT" -> "EDIT";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }


    @Override
    public PrivilegesNewEntity createPrivileges(PrivilegesDto privilegesDto){
        try {
            PrivilegesNewEntity privilegesNewEntity = new PrivilegesNewEntity();
            privilegesNewEntity.setPrivilegeName(privilegesDto.getName());
            privilegesNewEntity.setIcon(privilegesDto.getIcon());
            privilegesNewEntity.setPosition(privilegesDto.getPosition());
            privilegesNewEntity.setSlugName(privilegesDto.getSlugName());
            privilegesNewEntity.setBackendUrl(privilegesDto.getUrl());
            privilegesNewEntity.setDescription(privilegesDto.getDescription());
            if (privilegesDto.getSubName() != null && !privilegesDto.getSubName().equals("")) {
                privilegesNewEntity.setParentPrivilegeId(UUID.fromString(privilegesDto.getSubName()));
            }
            PrivilegesNewEntity saved = this.privilegesRepositoryNew.save(privilegesNewEntity);
            SideNavNewEntity sideNav = new SideNavNewEntity();
            if (privilegesDto.getSubName() != null && !privilegesDto.getSubName().equals("")) {
                SideNavNewEntity sideNavExist = this.sidenavRepositoryNew.findByPrivilegeId(UUID.fromString(privilegesDto.getSubName()));
                sideNav.setParent(sideNavExist);
            }
            sideNav.setPrivilegeId(saved.getId());
            sideNav.setIcon(saved.getIcon());
            sideNav.setName(saved.getPrivilegeName());
            sideNav.setPosition(saved.getPosition());
            sideNav.setSlugName(saved.getSlugName());
            this.sidenavRepositoryNew.save(sideNav);
            selfProxy.createPermissions(saved);
            return privilegesNewEntity;
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to create privilege.");
        }
    }

    @Async
    public void createPermissions(PrivilegesNewEntity saved) {
        try {
            List<Role> roles = this.roleRepository.findAllByIsActive(true);
            List<Endpoint> endpoints = this.endpointRepository.findAllByIsActive(true);
            List<PermissionsNewEntity> permissionsNewEntities = new ArrayList<>();
            for (Role role : roles) {
                for (Endpoint endpoint : endpoints) {
                    PermissionsNewEntity permission = new PermissionsNewEntity();
                    if (endpoint.getPrivilegeId() == null) {
                        permission.setPermissionType(PermissionType.NO);
                        if (role.getPosition().equals("1")) {
                            permission.setPermissionType(PermissionType.YES);
                        }
                        permission.setBackendUrl(saved.getBackendUrl());
                        permission.setHttpMethod(endpoint.getHttpMethod());
                        permission.setPrivilegeId(saved.getId());
                        permission.setPosition(saved.getPosition());
                        permission.setRoleId(role.getId());
                        permission.setEndpointId(endpoint.getId());
                        permission.setSideNav(PermissionType.NO);
                        if (role.getPosition().equals("1")) {
                            permission.setSideNav(PermissionType.YES);
                        }
                        permission.setPrivilegeName(saved.getPrivilegeName());
                        permission.setSlugName(saved.getSlugName());
                        permission.setActive(true); // if applicable
                        permissionsNewEntities.add(permission);
                    }
                }
            }
            this.permissionService.addPermissions(permissionsNewEntities);
        } catch (RuntimeException e) {
            logger.error("Exception occurred: ", e);
        }

    }

    @Async
    public void updatePermissions(PrivilegesNewEntity saved) {
        List<Role> roles = this.roleRepository.findAllByIsActive(true);
        List<PermissionsNewEntity> permissions = this.permissionRepositorynew.findAllByPrivilegeId(saved.getId());

        Map<UUID, List<PermissionsNewEntity>> permissionMap = permissions.stream()
                .collect(Collectors.groupingBy(PermissionsNewEntity::getRoleId));

        List<PermissionsNewEntity> updatedPermissions = roles.stream()
                .flatMap(role -> processRolePermissions(role, saved, permissionMap).stream())
                .toList(); // Java 16+ Stream.toList() ensures unmodifiable list

        this.permissionService.addPermissions(updatedPermissions);
    }

    private List<PermissionsNewEntity> processRolePermissions(Role role,
                                                              PrivilegesNewEntity saved,
                                                              Map<UUID, List<PermissionsNewEntity>> permissionMap) {
        List<PermissionsNewEntity> rolePermissions = permissionMap.getOrDefault(role.getId(), new ArrayList<>());

        if (rolePermissions.isEmpty()) {
            PermissionsNewEntity permission = createDefaultPermission(role.getId(), saved.getId());
            rolePermissions.add(permission);
        }

        for (PermissionsNewEntity permission : rolePermissions) {
            updatePermissionWithSaved(permission, role, saved);
        }

        return rolePermissions;
    }

    private PermissionsNewEntity createDefaultPermission(UUID roleId, UUID privilegeId) {
        PermissionsNewEntity permission = new PermissionsNewEntity();
        permission.setRoleId(roleId);
        permission.setPrivilegeId(privilegeId);
        return permission;
    }

    private void updatePermissionWithSaved(PermissionsNewEntity permission, Role role, PrivilegesNewEntity saved) {
        permission.setPermissionType("1".equals(role.getPosition()) ? PermissionType.YES : PermissionType.NO);

        if (PermissionType.YES.equals(permission.getUpdatableEndpoint())) {
            permission.setBackendUrl(saved.getBackendUrl());
        }

        if (PermissionType.YES.equals(permission.getUpdatableSlug())) {
            permission.setSlugName(saved.getSlugName());
        }

        permission.setPrivilegeName(saved.getPrivilegeName());
        permission.setPosition(saved.getPosition());
        permission.setActive(true);

        if ("1".equals(role.getPosition())) {
            permission.setSideNav(PermissionType.YES);
        }
    }



    @Override
    public PrivilegesNewEntity updatePrivileges(UUID id, PrivilegesDto privilegesDto) {
        try {
            // 1️⃣ Fetch the privilege
            PrivilegesNewEntity privilege = privilegesRepositoryNew.findById(id)
                    .orElseThrow(() -> new SomethingWentWrongException(PRIVILEGE_NOT_FOUND));

            // 2️⃣ Update basic fields
            privilege.setPrivilegeName(privilegesDto.getName());
            privilege.setIcon(privilegesDto.getIcon());
            privilege.setPosition(privilegesDto.getPosition());
            privilege.setSlugName(privilegesDto.getSlugName());
            privilege.setBackendUrl(privilegesDto.getUrl());
            privilege.setDescription(privilegesDto.getDescription());

            // 3️⃣ Update parent if provided
            if (privilegesDto.getSubName() != null && !privilegesDto.getSubName().isBlank()) {
                privilege.setParentPrivilegeId(UUID.fromString(privilegesDto.getSubName()));
            } else {
                privilege.setParentPrivilegeId(null);
            }

            PrivilegesNewEntity saved = privilegesRepositoryNew.save(privilege);

            // 4️⃣ Update SideNav
            SideNavNewEntity sideNav = sidenavRepositoryNew.findByPrivilegeId(saved.getId());
            if (sideNav == null) {
                sideNav = new SideNavNewEntity(); // create if missing
            }

            sideNav.setPrivilegeId(saved.getId());
            sideNav.setIcon(saved.getIcon());
            sideNav.setName(saved.getPrivilegeName());
            sideNav.setPosition(saved.getPosition());
            sideNav.setSlugName(saved.getSlugName());

            // Assign parent SideNav if SubName exists
            if (privilegesDto.getSubName() != null && !privilegesDto.getSubName().isBlank()) {
                SideNavNewEntity parentNav = sidenavRepositoryNew.findByPrivilegeId(UUID.fromString(privilegesDto.getSubName()));
                sideNav.setParent(parentNav);
            } else {
                sideNav.setParent(null);
            }

            sidenavRepositoryNew.save(sideNav);

            // 5️⃣ Update permissions (handle duplicates inside updatePermissions)
            selfProxy.updatePermissions(saved);

            return saved;

        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to update privilege.");
        }
    }


    @Override
    public PrivilegesNewEntity deletePrivileges(UUID id) {
        PrivilegesNewEntity privilege = this.privilegesRepositoryNew.findById(id)
                .orElseThrow(() -> new SomethingWentWrongException(PRIVILEGE_NOT_FOUND));
        List<PermissionsNewEntity> permissions = this.permissionRepositorynew.findAllByPrivilegeId(privilege.getId());
        List<SideNavNewEntity> sidenav = this.sidenavRepositoryNew.findAllByPrivilegeId(privilege.getId());
        this.permissionRepositorynew.deleteAll(permissions);
        this.sidenavRepositoryNew.deleteAll(sidenav);
        this.privilegesRepositoryNew.delete(privilege);
        return privilege;
    }

    @Override
    public PrivilegesNewEntity getAPrivilege(UUID id){
        return this.privilegesRepositoryNew.findById(id)
                .orElseThrow(() -> new SomethingWentWrongException(PRIVILEGE_NOT_FOUND));
    }

    @Override
    public List<PrivilegesNewEntity> listPrivileges(){
        return this.privilegesRepositoryNew.findAll();
    }

    @Override
    public PaginatedPrivilegesResponseDto listAllPrivileges(RolesFilterDto rolesFilterDto) {

        // 🧹 Step 1: Clean and normalize name input
        if (rolesFilterDto.getName() != null) {
            String trimmedName = rolesFilterDto.getName().trim();
            if (trimmedName.isEmpty()) {
                rolesFilterDto.setName(null); // ✅ handle empty strings safely
            } else {
                rolesFilterDto.setName("%" + trimmedName.toLowerCase() + "%"); // ✅ prepare LIKE parameter
            }
        }

        // 🧾 Step 2: Logging for debugging
        logger.info("rolesFilterDto -----------> {}", rolesFilterDto);
        // ⚙️ Step 3: Handle sorting direction safely
        Sort sort;
        try {
            sort = rolesFilterDto.getSortDirection() == SortDirection.ASC
                    ? Sort.by(rolesFilterDto.getSortField()).ascending()
                    : Sort.by(rolesFilterDto.getSortField()).descending();
        } catch (RuntimeException e) {
            // fallback in case of invalid sort field
            sort = Sort.by("privilegeName").ascending();
        }

        // 🧭 Step 4: Build pageable
        int page = rolesFilterDto.getPage() >= 0 ? rolesFilterDto.getPage() : 0;
        int size = rolesFilterDto.getSize() > 0 ? rolesFilterDto.getSize() : 10;

        Pageable pageable = PageRequest.of(
                page,
                size,
                sort
        );

        // 📤 Step 5: Call repository
        var resultPage = privilegesRepositoryNew.filterPrivileges(rolesFilterDto.getName(), true, pageable);

        // 🧩 Step 6: Map and return
        return privilegesMapper.privilegeToPaginatedRoleResponseDto(resultPage);
    }


}
