package com.shashank.iam.iambackend.modules.authentication.service;

import com.shashank.iam.iambackend.modules.authentication.entity.RefreshToken;
import com.shashank.iam.iambackend.modules.authentication.repository.RefreshTokenRepository;
import com.shashank.iam.iambackend.modules.user.entity.User;
import com.shashank.iam.iambackend.modules.user.repository.UserRepository;
import com.shashank.iam.iambackend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public String createRefreshToken(String email) {

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is disabled");
        }

        // Remove existing refresh tokens for this user.
        refreshTokenRepository.deleteAllByUserId(user.getId());

        String token = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(
                    LocalDateTime.now().plus(
                        Duration.ofMillis(jwtService.getRefreshTokenExpiration())
                    )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired");
        }

        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token");
        }

        User user = refreshToken.getUser();

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is disabled");
        }

        return jwtService.generateAccessToken(user.getEmail());
    }

    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}