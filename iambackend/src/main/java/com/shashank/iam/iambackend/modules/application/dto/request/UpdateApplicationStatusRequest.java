package com.shashank.iam.iambackend.modules.application.dto.request;

import com.shashank.iam.iambackend.modules.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {

    @NotNull
    private ApplicationStatus status;
}