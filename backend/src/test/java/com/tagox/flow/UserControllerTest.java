package com.tagox.flow;

import com.tagox.flow.domain.tenant.TipoPessoa;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.UserRepository;

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
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private TenantRepository tenantRepository;


    @Autowired
    private UserRepository userRepository;


    private Tenant tenant;



    @BeforeEach
    void prepararBanco() {

        userRepository.deleteAll();

        tenantRepository.deleteAll();


        tenant = new Tenant(
                UUID.randomUUID(),
                TipoPessoa.JURIDICA,
                "22222222000122",
                "Empresa Teste",
                "Empresa Teste",
                "empresa-teste",
                TenantStatus.TRIAL
        );


        tenantRepository.save(tenant);
    }



    @Test
    void deveCriarUsuarioVinculadoAoTenant() throws Exception {


        String json = """
                {
                    "tenantId": "%s",
                    "nome": "Clayton Usuario",
                    "email": "clayton@teste.com",
                    "senha": "senha-teste"
                }
                """.formatted(tenant.getId());



        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome")
                        .value("Clayton Usuario"))
                .andExpect(jsonPath("$.email")
                        .value("clayton@teste.com"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$.tenantId")
                        .value(tenant.getId().toString()));
    }




    @Test
    void naoDevePermitirEmailDuplicadoNoMesmoTenant() throws Exception {


        String json = """
                {
                    "tenantId": "%s",
                    "nome": "Primeiro Usuario",
                    "email": "usuario@teste.com",
                    "senha": "senha-teste"
                }
                """.formatted(tenant.getId());



        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());



        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Já existe um usuário com este email neste tenant"
                        ));
    }




    @Test
    void deveFalharQuandoTenantNaoExiste() throws Exception {


        String tenantInexistente = UUID.randomUUID().toString();



        String json = """
                {
                    "tenantId": "%s",
                    "nome": "Usuario Invalido",
                    "email": "usuario@teste.com",
                    "senha": "senha-teste"
                }
                """.formatted(tenantInexistente);



        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Tenant não encontrado"));
    }

}