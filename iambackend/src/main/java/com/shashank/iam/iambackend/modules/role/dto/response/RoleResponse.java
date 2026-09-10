package com.shashank.iam.iambackend.modules.role.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private UUID id;
    private String name;
    private String description;
    private Set<UUID> permissionIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}