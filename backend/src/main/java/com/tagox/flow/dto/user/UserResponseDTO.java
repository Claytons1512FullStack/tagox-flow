package com.tagox.flow.dto.user;

import java.util.UUID;

public record UserResponseDTO(

        UUID id,

        UUID tenantId,

        String nome,

        String email,

        String status

) {
}