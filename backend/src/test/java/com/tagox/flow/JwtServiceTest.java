package com.tagox.flow;

import com.tagox.flow.security.JwtService;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void deveGerarTokenComUserIdETenantId() {

        String secret = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";

        JwtService jwtService = new JwtService(
                secret,
                3600000
        );

        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        String token = jwtService.gerarToken(
                userId,
                tenantId
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        String[] partes = token.split("\\.");

        assertEquals(3, partes.length);

        assertEquals(
                userId,
                jwtService.extrairUserId(token)
        );

        assertEquals(
                tenantId,
                jwtService.extrairTenantId(token)
        );
    }

    @Test
    void deveRejeitarTokenComAssinaturaInvalida() {

        String secret = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";

        JwtService jwtService = new JwtService(
                secret,
                3600000
        );

        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        String token = jwtService.gerarToken(
                userId,
                tenantId
        );

        String tokenAlterado = token.substring(
                0,
                token.length() - 1
        ) + "x";

        assertThrows(
                RuntimeException.class,
                () -> jwtService.validarToken(tokenAlterado)
        );
    }
}