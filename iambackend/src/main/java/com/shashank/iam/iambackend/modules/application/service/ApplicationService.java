package com.shashank.iam.iambackend.modules.application.service;

import com.shashank.iam.iambackend.modules.application.dto.request.CreateApplicationRequest;
import com.shashank.iam.iambackend.modules.application.dto.request.UpdateApplicationRequest;
import com.shashank.iam.iambackend.modules.application.dto.request.UpdateApplicationStatusRequest;
import com.shashank.iam.iambackend.modules.application.dto.response.ApplicationResponse;
import com.shashank.iam.iambackend.modules.application.entity.Application;
import com.shashank.iam.iambackend.modules.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID id) {
        return toResponse(findApplication(id));
    }

    @Transactional
    public ApplicationResponse createApplication(
            CreateApplicationRequest request) {

        String name = request.getName().trim();

        if (applicationRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An application with this name already exists");
        }

        Application application = Application.builder()
                .name(name)
                .description(normalizeOptional(request.getDescription()))
                .owner(normalizeOptional(request.getOwner()))
                .department(normalizeOptional(request.getDepartment()))
                .status(request.getStatus())
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse updateApplication(
            UUID id,
            UpdateApplicationRequest request) {

        Application application = findApplication(id);

        String name = request.getName().trim();

        applicationRepository.findByNameIgnoreCase(name)
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "An application with this name already exists");
            });

        application.setName(name);
        application.setDescription(
                normalizeOptional(request.getDescription()));
        application.setOwner(
                normalizeOptional(request.getOwner()));
        application.setDepartment(
                normalizeOptional(request.getDepartment()));
        application.setStatus(request.getStatus());

        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(
            UUID id,
            UpdateApplicationStatusRequest request) {

        Application application = findApplication(id);

        application.setStatus(request.getStatus());

        return toResponse(applicationRepository.save(application));
    }

    private Application findApplication(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"));
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .description(application.getDescription())
                .owner(application.getOwner())
                .department(application.getDepartment())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isBlank() ? null : trimmed;
    }
}