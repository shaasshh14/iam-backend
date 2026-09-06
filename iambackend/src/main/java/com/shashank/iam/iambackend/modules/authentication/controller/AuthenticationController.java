package com.shashank.iam.iambackend.modules.authentication.controller;

import com.shashank.iam.iambackend.modules.authentication.dto.request.LoginRequest;
import com.shashank.iam.iambackend.modules.authentication.dto.request.RefreshTokenRequest;
import com.shashank.iam.iambackend.modules.authentication.dto.response.LoginResponse;
import com.shashank.iam.iambackend.modules.authentication.service.AuthenticationService;
import com.shashank.iam.iambackend.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

        private final AuthenticationManager authenticationManager;
        private final AuthenticationService authenticationService;
        private final JwtService jwtService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail().trim().toLowerCase(),
                                                request.getPassword()));

                String email = authentication.getName();

                String accessToken = jwtService.generateAccessToken(email);

                String refreshToken = authenticationService.createRefreshToken(email);

                return ResponseEntity.ok(
                                LoginResponse.builder()
                                                .accessToken(accessToken)
                                                .refreshToken(refreshToken)
                                                .build());
        }

        @PostMapping("/refresh")
        public ResponseEntity<LoginResponse> refresh(
                        @Valid @RequestBody RefreshTokenRequest request) {

                String accessToken = authenticationService.refreshAccessToken(
                                request.getRefreshToken());

                return ResponseEntity.ok(
                                LoginResponse.builder()
                                                .accessToken(accessToken)
                                                .refreshToken(request.getRefreshToken())
                                                .build());
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout(
                        @Valid @RequestBody RefreshTokenRequest request) {

                authenticationService.revokeRefreshToken(
                                request.getRefreshToken());

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/me")
        public ResponseEntity<String> getCurrentUser(
                        Authentication authentication) {

                return ResponseEntity.ok(authentication.getName());
        }
}