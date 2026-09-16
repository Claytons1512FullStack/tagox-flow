package com.tagox.flow.dto.tenant;

import com.tagox.flow.domain.tenant.TipoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TenantRequestDTO(

        @NotNull(message = "Tipo de pessoa é obrigatório.")
        TipoPessoa tipoPessoa,

        @NotBlank(message = "Documento é obrigatório.")
        String documento,

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String nome,

        @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres.")
        String nomeFantasia,

        @NotBlank(message = "Slug é obrigatório.")
        @Size(max = 100, message = "Slug deve ter no máximo 100 caracteres.")
        String slug

) {
}