package com.tagox.flow.dto.user;

import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserStatus;

import java.util.UUID;

public class UserResponse {

    private UUID id;

    private UUID tenantId;

    private String nome;

    private String email;

    private UserStatus status;


    public UserResponse(
            UUID id,
            UUID tenantId,
            String nome,
            String email,
            UserStatus status
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.nome = nome;
        this.email = email;
        this.status = status;
    }


    public static UserResponse from(User user) {

        return new UserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getNome(),
                user.getEmail(),
                user.getStatus()
        );
    }


    public UUID getId() {
        return id;
    }


    public UUID getTenantId() {
        return tenantId;
    }


    public String getNome() {
        return nome;
    }


    public String getEmail() {
        return email;
    }


    public UserStatus getStatus() {
        return status;
    }
}
