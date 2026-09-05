package com.shashank.iam.iambackend.config;

import com.shashank.iam.iambackend.modules.user.entity.User;
import com.shashank.iam.iambackend.modules.user.entity.UserStatus;
import com.shashank.iam.iambackend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shashank.iam.iambackend.modules.role.entity.Role;
import com.shashank.iam.iambackend.modules.role.repository.RoleRepository;

@Configuration
@RequiredArgsConstructor
public class DevelopmentDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner initializeDevelopmentUser() {
        return args -> {
            if (userRepository.existsByEmail("admin@iam.local")) {
                return;
            }

            Role adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("ADMIN")
                                .description("Full system administrator")
                                .build()
                ));

            User user = User.builder()
                    .email("admin@iam.local")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("Admin")
                    .lastName("User")
                    .employeeId("EMP000")
                    .department("Security")
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .status(UserStatus.ACTIVE)
                    .applicationCount(0)
                    .enabled(true)
                    .build();

            userRepository.save(user);
        };
    }
}
