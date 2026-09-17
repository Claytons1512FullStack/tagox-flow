package com.tagox.flow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${tagox.jwt.secret}") String secret,
            @Value("${tagox.jwt.expiration}") long expirationMillis
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMillis = expirationMillis;
    }

    public String gerarToken(UUID userId, UUID tenantId) {

        Instant agora = Instant.now();
        Instant expiracao = agora.plusMillis(expirationMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(signingKey)
                .compact();
    }

    public Claims validarToken(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extrairUserId(String token) {

        Claims claims = validarToken(token);

        return UUID.fromString(
                claims.getSubject()
        );
    }

    public UUID extrairTenantId(String token) {

        Claims claims = validarToken(token);

        return UUID.fromString(
                claims.get("tenantId", String.class)
        );
    }
}