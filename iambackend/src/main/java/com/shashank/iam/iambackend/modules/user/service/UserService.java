package com.shashank.iam.iambackend.modules.user.service;

import com.shashank.iam.iambackend.modules.role.entity.Role;
import com.shashank.iam.iambackend.modules.role.repository.RoleRepository;
import com.shashank.iam.iambackend.modules.user.dto.request.CreateUserRequest;
import com.shashank.iam.iambackend.modules.user.dto.request.UpdateUserRequest;
import com.shashank.iam.iambackend.modules.user.dto.request.UpdateUserStatusRequest;
import com.shashank.iam.iambackend.modules.user.dto.response.UserResponse;
import com.shashank.iam.iambackend.modules.user.entity.User;
import com.shashank.iam.iambackend.modules.user.entity.UserStatus;
import com.shashank.iam.iambackend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user with this email already exists");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user with this employee ID already exists");
        }

        Role role = resolveRole(request.getRole());

        NameParts nameParts = splitName(request.getName());

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(resolvePassword(request)))
                .firstName(nameParts.firstName())
                .lastName(nameParts.lastName())
                .employeeId(request.getEmployeeId().trim())
                .department(request.getDepartment().trim())
                .status(request.getStatus())
                .enabled(request.getStatus() == UserStatus.ACTIVE)
                .applicationCount(0)
                .build();

        user.getRoles().add(role);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUser(id);

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A user with this email already exists");
                });

        userRepository.findByEmployeeId(request.getEmployeeId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A user with this employee ID already exists");
                });

        Role role = resolveRole(request.getRole());

        NameParts nameParts = splitName(request.getName());

        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFirstName(nameParts.firstName());
        user.setLastName(nameParts.lastName());
        user.setEmployeeId(request.getEmployeeId().trim());
        user.setDepartment(request.getDepartment().trim());
        user.setStatus(request.getStatus());
        user.setEnabled(request.getStatus() == UserStatus.ACTIVE);

        user.getRoles().clear();
        user.getRoles().add(role);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUserStatus(
            UUID id,
            UpdateUserStatusRequest request) {

        User user = findUser(id);

        user.setStatus(request.getStatus());
        user.setEnabled(request.getStatus() == UserStatus.ACTIVE);

        return toResponse(userRepository.save(user));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }

    private Role resolveRole(String roleName) {
        return roleRepository.findByNameIgnoreCase(roleName.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role not found: " + roleName));
    }

    private UserResponse toResponse(User user) {
        String roleName = user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .employeeId(valueOrDefault(
                        user.getEmployeeId(),
                        "EMP-" + user.getId()))
                .department(valueOrDefault(
                        user.getDepartment(),
                        "Unassigned"))
                .role(valueOrDefault(
                        roleName,
                        "User"))
                .status(user.getStatus() == null
                        ? UserStatus.ACTIVE
                        : user.getStatus())
                .lastActive(user.getLastActive() == null
                        ? user.getUpdatedAt()
                        : user.getLastActive())
                .createdAt(user.getCreatedAt())
                .applicationCount(user.getApplicationCount() == null
                        ? 0
                        : user.getApplicationCount())
                .build();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    private NameParts splitName(String name) {
        String trimmed = name.trim().replaceAll("\\s+", " ");
        String[] parts = trimmed.split(" ", 2);

        String firstName = parts[0];
        String lastName = parts.length > 1
                ? parts[1]
                : "-";

        return new NameParts(firstName, lastName);
    }

    private String resolvePassword(CreateUserRequest request) {
        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            return "ChangeMe@123";
        }

        return request.getPassword();
    }

    private record NameParts(
            String firstName,
            String lastName) {
    }
}