package com.tagox.flow.controller;

import com.tagox.flow.domain.user.User;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.dto.user.UserResponse;
import com.tagox.flow.security.AuthenticatedUser;
import com.tagox.flow.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> criar(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {

        User user = userService.criarUsuario(
                request,
                authenticatedUser.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        UserResponse.from(user)
                );
    }
}
