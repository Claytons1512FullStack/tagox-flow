package com.tagox.flow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateUserRequest {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senhaHash;


    public UUID getTenantId() {
        return tenantId;
    }


    public String getNome() {
        return nome;
    }


    public String getEmail() {
        return email;
    }


    public String getSenhaHash() {
        return senhaHash;
    }


    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}