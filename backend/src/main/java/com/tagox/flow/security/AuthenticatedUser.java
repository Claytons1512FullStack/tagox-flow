package com.tagox.flow.security;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.util.UUID;

public class AuthenticatedUser implements AuthenticatedPrincipal {


    private final UUID userId;
    private final UUID tenantId;

    public AuthenticatedUser(
            UUID userId,
            UUID tenantId
    ) {
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public String getName() {
        return userId.toString();
    }

}
