package com.airfrance.airfrance.controller;

import com.airfrance.airfrance.security.AdminTokenService;
import com.airfrance.airfrance.security.AdminTokenService.TokenResponse;
import com.airfrance.airfrance.web.AdminLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AdminTokenService adminTokenService;

    public AuthController(AdminTokenService adminTokenService) {
        this.adminTokenService = adminTokenService;
    }

    @PostMapping("/token")
    public TokenResponse createToken(@Valid @RequestBody AdminLoginRequest request) {
        return adminTokenService.issueToken(request.username(), request.password());
    }
}
