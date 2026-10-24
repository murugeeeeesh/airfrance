package com.airfrance.airfrance.security;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminTokenService {

    private final String username;
    private final String passwordHash;
    private final String issuer;
    private final long ttlSeconds;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AdminTokenService(
            @Value("${app.security.admin.username:}") String username,
            @Value("${app.security.admin.password:}") String password,
            @Value("${app.security.jwt-issuer:airfrance-api}") String issuer,
            @Value("${app.security.jwt-ttl-seconds:900}") long ttlSeconds,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder) {
        this.username = username;
        this.passwordHash = password.isBlank() ? "" : passwordEncoder.encode(password);
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        if (ttlSeconds < 1) {
            throw new IllegalArgumentException("app.security.jwt-ttl-seconds must be positive");
        }
    }

    public TokenResponse issueToken(String suppliedUsername, String suppliedPassword) {
        if (username.isBlank() || passwordHash.isBlank()
                || !username.equals(suppliedUsername)
                || !passwordEncoder.matches(suppliedPassword, passwordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(username)
                .claim("roles", List.of("ADMIN"))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", ttlSeconds);
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }
}
