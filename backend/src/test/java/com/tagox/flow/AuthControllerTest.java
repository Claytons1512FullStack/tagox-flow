package com.tagox.flow;

import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.security.JwtService;
import com.tagox.flow.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    private Tenant tenant;
    private User usuario;

    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "88888888000188",
                "Empresa Auth Controller",
                "Empresa Auth Controller",
                "empresa-auth-controller",
                TenantStatus.ATIVO
        );

        tenantRepository.save(tenant);

        criarUsuario();
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() throws Exception {

        String json = """
            {
                "tenantSlug": "empresa-auth-controller",
                "email": "login@teste.com",
                "senha": "senha-login"
            }
            """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .isNotEmpty())
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper
                .builder()
                .build()
                .readTree(response)
                .get("token")
                .asText();

        org.assertj.core.api.Assertions.assertThat(
                jwtService.extrairUserId(token)
        ).isEqualTo(usuario.getId());

        org.assertj.core.api.Assertions.assertThat(
                jwtService.extrairTenantId(token)
        ).isEqualTo(tenant.getId());
    }

    @Test
    void deveRejeitarSenhaIncorreta() throws Exception {

        String json = """
            {
                "tenantSlug": "empresa-auth-controller",
                "email": "login@teste.com",
                "senha": "senha-errada"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Credenciais inválidas"));
    }

    @Test
    void deveRejeitarTenantInexistente() throws Exception {

        String json = """
            {
                "tenantSlug": "tenant-inexistente",
                "email": "login@teste.com",
                "senha": "senha-login"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Tenant não encontrado"));
    }

    @Test
    void deveValidarCamposObrigatorios() throws Exception {

        String json = """
            {
                "tenantSlug": "",
                "email": "",
                "senha": ""
            }
            """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"));
    }

    private void criarUsuario() {

        CreateUserRequest request = new CreateUserRequest();

        request.setTenantId(tenant.getId());
        request.setNome("Usuario Login");
        request.setEmail("login@teste.com");
        request.setSenha("senha-login");

        usuario = userService.criarUsuario(request);
    }
}

