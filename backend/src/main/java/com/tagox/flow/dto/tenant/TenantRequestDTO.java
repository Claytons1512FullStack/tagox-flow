package com.tagox.flow.dto.tenant;

import com.tagox.flow.domain.tenant.TipoPessoa;

public record TenantRequestDTO(

        TipoPessoa tipoPessoa,

        String documento,

        String nome,

        String nomeFantasia,

        String slug

) {
}