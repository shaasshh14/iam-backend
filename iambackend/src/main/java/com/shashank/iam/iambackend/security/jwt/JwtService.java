package com.shashank.iam.iambackend.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

        private final SecretKey secretKey;
        private final long accessTokenExpiration;
        private final long refreshTokenExpiration;

        public JwtService(
                        @Value("${jwt.secret}") String secret,
                        @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                        @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

                this.secretKey = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                this.accessTokenExpiration = accessTokenExpiration;
                this.refreshTokenExpiration = refreshTokenExpiration;
        }

        public String generateAccessToken(String email) {
                return generateToken(email, accessTokenExpiration);
        }

        public String generateRefreshToken(String email) {
                return generateToken(email, refreshTokenExpiration);
        }

        private String generateToken(
                        String email,
                        long expirationMillis) {

                Date now = new Date();

                Date expiration = new Date(
                                now.getTime() + expirationMillis);

                return Jwts.builder()
                                .subject(email)
                                .issuedAt(now)
                                .expiration(expiration)
                                .signWith(secretKey)
                                .compact();
        }

        public String extractUsername(String token) {
                return Jwts.parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getSubject();
        }

        public boolean isTokenValid(
                        String token,
                        UserDetails userDetails) {

                String username = extractUsername(token);

                return username.equals(userDetails.getUsername())
                                && !isTokenExpired(token);
        }

        public boolean isTokenValid(String token) {
                try {
                        extractUsername(token);
                        return !isTokenExpired(token);
                } catch (Exception exception) {
                        return false;
                }
        }

        public long getRefreshTokenExpiration() {
                return refreshTokenExpiration;
        }

        private boolean isTokenExpired(String token) {
                Date expiration = Jwts.parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getExpiration();

                return expiration.before(new Date());
        }
}