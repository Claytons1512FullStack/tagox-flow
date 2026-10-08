package com.tagox.flow.dto.onboarding;

import com.tagox.flow.dto.tenant.TenantRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OnboardingRequest(

        @NotNull(message = "Os dados da empresa são obrigatórios.")
        @Valid
        TenantRequestDTO tenant,

        @NotNull(message = "Os dados do administrador são obrigatórios.")
        @Valid
        OnboardingAdminRequest administrador

) {
}