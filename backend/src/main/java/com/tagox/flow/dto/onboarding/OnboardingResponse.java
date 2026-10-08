package com.tagox.flow.dto.onboarding;

import com.tagox.flow.dto.tenant.TenantResponseDTO;
import com.tagox.flow.dto.user.UserResponse;

public record OnboardingResponse(
        TenantResponseDTO tenant,
        UserResponse administrador
) {
}
