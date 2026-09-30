package com.techversant.userservice.service.impl;

import com.techversant.userservice.dto.EndpointDto;
import com.techversant.userservice.dto.PaginatedEndpointsResponseDto;
import com.techversant.userservice.dto.RolesFilterDto;
import com.techversant.userservice.mapper.EndpointMapper;
import com.techversant.userservice.model.*;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import com.techversant.userservice.repository.EndpointRepository;
import com.techversant.userservice.repository.RoleRepository;
import com.techversant.userservice.repository.privilegesnew.PermissionRepositorynew;
import com.techversant.userservice.repository.privilegesnew.PrivilegeRepositoryNew;
import com.techversant.userservice.service.IEndpointService;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.exceptions.SomethingWentWrongException;
import com.techversant.userservice.utils.exceptions.UserNameAlreadyExistsException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EndponitService implements IEndpointService {
    private final EndpointRepository endpointRepository;
    private final PrivilegeRepositoryNew privilegeRepository;
    private final EndpointMapper endpointMapper;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final PermissionRepositorynew permissionRepositorynew;
    public EndponitService(PermissionRepositorynew permissionRepositorynew, PermissionService permissionService, RoleRepository roleRepository, EndpointMapper endpointMapper, EndpointRepository endpointRepository, PrivilegeRepositoryNew privilegeRepository) {
        this.endpointRepository = endpointRepository;
        this.privilegeRepository = privilegeRepository;
        this.endpointMapper = endpointMapper;
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
        this.permissionRepositorynew = permissionRepositorynew;
    }
    @Override
    public Endpoint creatEndpoint(EndpointDto endpointDto) {
        Endpoint getDuplicate = this.endpointRepository.findOneByHttpMethodAndEndponitNameAndIsActive(endpointDto.getHttpMethod(), endpointDto.getEndpointName(), true);
        if (getDuplicate != null) {
            throw new UserNameAlreadyExistsException("Endpoint already exist with same method");
        }
        Endpoint saveEndpoint = this.endpointRepository.save(endpointMapper.endpointDtoToEntity(endpointDto, new Endpoint()));
        PrivilegesNewEntity privileges = this.privilegeRepository.findOneByIdAndIsActive(saveEndpoint.getPrivilegeId(), true);
        List<Role> roles = this.roleRepository.findAllByIsActive(true);
        List<PermissionsNewEntity> permissions = new ArrayList<>();
        if (!roles.isEmpty() && privileges != null) {
            for (Role role : roles) {
                PermissionsNewEntity permission = new PermissionsNewEntity();
                permission.setPermissionType(PermissionType.NO);
                if (role.getPosition().equals("1")) {
                    permission.setPermissionType(PermissionType.YES);
                }
                permission.setEndpointName(saveEndpoint.getEndponitName());
                permission.setBackendUrl(saveEndpoint.getBackendUrl());
                permission.setHttpMethod(saveEndpoint.getHttpMethod());
                permission.setPrivilegeId(saveEndpoint.getPrivilegeId());
                permission.setPosition(privileges.getPosition());
                permission.setEndpointId(saveEndpoint.getId());
                permission.setRoleId(role.getId());
                permission.setSideNav(PermissionType.NO);
                if (role.getPosition().equals("1")) {
                    permission.setSideNav(PermissionType.YES);
                }
                permission.setPrivilegeName(privileges.getPrivilegeName());
                permission.setSlugName(privileges.getSlugName());
                permission.setActive(true); // if applicable
                permissions.add(permission);
            }
            this.permissionService.addPermissions(permissions);
        }
        return saveEndpoint;
    }

    @Override
    public Endpoint updateEndpoint(UUID id, EndpointDto endpointDto) {
        Endpoint endpoint = this.endpointRepository.findByIdAndIsActive(id, true);
        if (endpoint == null) {
            throw new SomethingWentWrongException("No endpoint found for the given ID.");
        }

        // Update the endpoint data
        Endpoint savedEndpoint = this.endpointRepository.save(
                endpointMapper.endpointDtoToEntity(endpointDto, endpoint)
        );

        // Fetch privilege and roles
        PrivilegesNewEntity privilege = this.privilegeRepository.findOneByIdAndIsActive(
                savedEndpoint.getPrivilegeId(), true
        );
        if (privilege == null) {
            throw new SomethingWentWrongException("No privilege found for the given endpoint.");
        }

        List<Role> roles = this.roleRepository.findAllByIsActive(true);
        List<PermissionsNewEntity> permissions = this.permissionRepositorynew.findAllByEndpointId(id);
        List<PermissionsNewEntity> updatedPermissions = new ArrayList<>();

        // Create a map for quick role-to-permission lookup
        Map<UUID, PermissionsNewEntity> permissionMap = permissions.stream()
                .collect(Collectors.toMap(PermissionsNewEntity::getRoleId, p -> p));

        for (Role role : roles) {
            PermissionsNewEntity permission = permissionMap.get(role.getId());

            // If a permission exists for this role, update it
            if (permission != null) {
                if ("1".equals(role.getPosition())) {
                    permission.setPermissionType(PermissionType.YES);
                    permission.setSideNav(PermissionType.YES);
                }

                if (PermissionType.YES.equals(permission.getUpdatableEndpoint())) {
                    permission.setBackendUrl(savedEndpoint.getBackendUrl());
                }

                if (PermissionType.YES.equals(permission.getUpdatableSlug())) {
                    permission.setSlugName(privilege.getSlugName());
                }

                permission.setEndpointName(savedEndpoint.getEndponitName());
                permission.setHttpMethod(savedEndpoint.getHttpMethod());
                permission.setPrivilegeId(savedEndpoint.getPrivilegeId());
                permission.setPosition(privilege.getPosition());
                permission.setEndpointId(savedEndpoint.getId());
                permission.setPrivilegeName(privilege.getPrivilegeName());
                permission.setActive(true);

                updatedPermissions.add(permission);
            }
        }

        if (!updatedPermissions.isEmpty()) {
            this.permissionService.addPermissions(updatedPermissions);
        }

        return savedEndpoint;
    }

    @Override
    public Endpoint deleteEndpoint(UUID id) {
        Endpoint endpoint = this.endpointRepository.findByIdAndIsActive(id, true);
        if (endpoint == null) {
            throw new SomethingWentWrongException("No end-point found for the given ID.");
        }
        if(endpoint.getPrivilegeId() == null) {
            throw new UserNameAlreadyExistsException("Auto-generated method, which are unable to delete");
        }
        this.endpointRepository.delete(endpoint);
        List<PermissionsNewEntity> permissionsNewEntities = this.permissionRepositorynew.findAllByEndpointId(id);
        this.permissionRepositorynew.deleteAll(permissionsNewEntities);
        return endpoint;
    }

    @Override
    public Endpoint getAEndpoint(UUID id) {
        Endpoint endpoint = this.endpointRepository.findByIdAndIsActive(id, true);
        if (endpoint == null) {
            throw new SomethingWentWrongException("No end-point found for the given ID.");
        }
        return endpoint;
    }

    @Override
    public PaginatedEndpointsResponseDto listEndpoint(RolesFilterDto rolesFilterDto){
        if (rolesFilterDto.getName() != null && rolesFilterDto.getName().isBlank()) {
            rolesFilterDto.setName(null);
        } else if (rolesFilterDto.getName() != null) {
            rolesFilterDto.setName("%" + rolesFilterDto.getName().toLowerCase() + "%");
        }
        Sort sort = rolesFilterDto.getSortDirection() == SortDirection.ASC ? Sort.by(rolesFilterDto.getSortField()).ascending() : Sort.by(rolesFilterDto.getSortField()).descending();
        Pageable pageable = PageRequest.of(rolesFilterDto.getPage(), rolesFilterDto.getSize(), sort);
        return endpointMapper.endpointToPaginatedRoleResponseDto(endpointRepository.filterEndpoint(rolesFilterDto.getName(), true, pageable));
    }

    @Override
    public List<Endpoint> listAllEndpoint() {
        return this.endpointRepository.findAllByIsActive(true);
    }


}
