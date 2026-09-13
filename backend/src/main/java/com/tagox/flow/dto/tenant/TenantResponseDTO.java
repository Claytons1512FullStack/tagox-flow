package com.tagox.flow.dto.tenant;

import java.util.UUID;

public record TenantResponseDTO(

        UUID id,

        String nome,

        String nomeFantasia,

        String slug,

        String status

) {
}