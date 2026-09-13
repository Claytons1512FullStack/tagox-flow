package com.tagox.flow.controller;

import com.tagox.flow.domain.user.User;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.dto.user.UserResponse;
import com.tagox.flow.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody CreateUserRequest request
    ) {

        User user = userService.criarUsuario(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        UserResponse.from(user)
                );
    }
}