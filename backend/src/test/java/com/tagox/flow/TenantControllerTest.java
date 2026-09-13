package com.tagox.flow;

import com.tagox.flow.domain.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void limparBanco() {
        tenantRepository.deleteAll();
    }


    @Test
    void deveCriarTenantViaApi() throws Exception {

        String json = """
                {
                    "tipoPessoa": "JURIDICA",
                    "documento": "11111111000111",
                    "nome": "TAGOX Flow Tecnologia LTDA",
                    "nomeFantasia": "TAGOX Flow",
                    "slug": "tagox-flow-api"
                }
                """;


        mockMvc.perform(
                        post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("TAGOX Flow Tecnologia LTDA"))
                .andExpect(jsonPath("$.status")
                        .value("TRIAL"));
    }
}