package com.shashank.iam.iambackend.modules.application.repository;

import com.shashank.iam.iambackend.modules.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Application> findByNameIgnoreCase(String name);
}