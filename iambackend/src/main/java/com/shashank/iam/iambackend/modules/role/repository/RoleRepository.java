package com.shashank.iam.iambackend.modules.role.repository;

import com.shashank.iam.iambackend.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Role> findByNameIgnoreCase(String name);
}