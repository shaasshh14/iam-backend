package com.shashank.iam.iambackend.modules.role.service;

import com.shashank.iam.iambackend.modules.permission.entity.Permission;
import com.shashank.iam.iambackend.modules.role.dto.request.CreateRoleRequest;
import com.shashank.iam.iambackend.modules.role.dto.request.UpdateRoleRequest;
import com.shashank.iam.iambackend.modules.role.dto.response.RoleResponse;
import com.shashank.iam.iambackend.modules.role.entity.Role;
import com.shashank.iam.iambackend.modules.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse getRoleById(UUID id) {
        return toResponse(findRole(id));
    }

    public RoleResponse createRole(CreateRoleRequest request) {
        String name = normalizeName(request.getName());

        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Role already exists: " + name
            );
        }

        Role role = Role.builder()
                .name(name)
                .description(normalizeOptional(request.getDescription()))
                .build();

        return toResponse(roleRepository.save(role));
    }

    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        Role role = findRole(id);
        String name = normalizeName(request.getName());

        roleRepository.findByNameIgnoreCase(name)
                .filter(existingRole -> !existingRole.getId().equals(id))
                .ifPresent(existingRole -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Role already exists: " + name
                    );
                });

        role.setName(name);
        role.setDescription(normalizeOptional(request.getDescription()));

        return toResponse(roleRepository.save(role));
    }

    public void deleteRole(UUID id) {
        Role role = findRole(id);

        if (!role.getUsers().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a role assigned to users"
            );
        }

        roleRepository.delete(role);
    }

    private Role findRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Role not found: " + id
                ));
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissionIds(
                        role.getPermissions()
                                .stream()
                                .map(Permission::getId)
                                .collect(Collectors.toSet())
                )
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}