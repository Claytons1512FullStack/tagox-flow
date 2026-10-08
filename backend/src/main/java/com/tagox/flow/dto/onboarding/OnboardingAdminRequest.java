package com.tagox.flow.dto.onboarding;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingAdminRequest(

        @NotBlank(message = "Nome do administrador é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String nome,

        @NotBlank(message = "E-mail do administrador é obrigatório.")
        @Email(message = "E-mail do administrador inválido.")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "Senha do administrador é obrigatória.")
        String senha

) {
}
