package com.tagox.flow.dto.tenant;

import com.tagox.flow.domain.tenant.Tenant;

import java.util.UUID;

public record TenantResponseDTO(

        UUID id,
        String nome,
        String nomeFantasia,
        String slug,
        String status

) {

    public static TenantResponseDTO from(Tenant tenant) {

        return new TenantResponseDTO(
                tenant.getId(),
                tenant.getNome(),
                tenant.getNomeFantasia(),
                tenant.getSlug(),
                tenant.getStatus().name()
        );
    }
}