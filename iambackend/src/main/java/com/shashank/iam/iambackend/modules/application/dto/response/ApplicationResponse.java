package com.shashank.iam.iambackend.modules.application.dto.response;

import com.shashank.iam.iambackend.modules.application.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {

    private UUID id;
    private String name;
    private String description;
    private String owner;
    private String department;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}