package com.tagox.flow.controller;

import com.tagox.flow.domain.user.User;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.dto.user.UserResponseDTO;
import com.tagox.flow.service.UserService;

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
    public ResponseEntity<UserResponseDTO> criar(
            @RequestBody CreateUserRequest request
    ) {


        User user = userService.criarUsuario(request);


        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getTenant().getId(),
                user.getNome(),
                user.getEmail(),
                user.getStatus().name()
        );


        return ResponseEntity.ok(response);
    }
}