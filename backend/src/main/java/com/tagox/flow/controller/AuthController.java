package com.tagox.flow.controller;

import com.tagox.flow.dto.auth.AuthResponse;
import com.tagox.flow.dto.auth.LoginRequest;
import com.tagox.flow.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token = authService.autenticar(request);

        AuthResponse response = new AuthResponse(
                token,
                "Bearer"
        );

        return ResponseEntity.ok(response);
    }
}