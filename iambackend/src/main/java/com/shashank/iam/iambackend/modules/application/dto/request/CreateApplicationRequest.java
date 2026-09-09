package com.shashank.iam.iambackend.modules.application.dto.request;

import com.shashank.iam.iambackend.modules.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 5000)
    private String description;

    @Size(max = 255)
    private String owner;

    @Size(max = 100)
    private String department;

    @NotNull
    private ApplicationStatus status;
}