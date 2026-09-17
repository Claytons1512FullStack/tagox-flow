package com.tagox.flow.service;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.dto.auth.LoginRequest;
import com.tagox.flow.security.JwtService;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public String autenticar(LoginRequest request) {

        Tenant tenant = tenantRepository.findBySlug(
                request.getTenantSlug()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Tenant não encontrado"
                )
        );

        if (tenant.getStatus() != TenantStatus.ATIVO) {
            throw new ResourceNotFoundException(
                    "Tenant não está disponível para autenticação"
            );
        }

        String emailNormalizado = request.getEmail()
                .toLowerCase();

        User user = userRepository.findByTenantIdAndEmail(
                tenant.getId(),
                emailNormalizado
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Usuário não encontrado"
                )
        );

        if (!passwordEncoder.matches(
                request.getSenha(),
                user.getSenhaHash()
        )) {
            throw new ResourceNotFoundException(
                    "Credenciais inválidas"
            );
        }

        return jwtService.gerarToken(
                user.getId(),
                tenant.getId()
        );
    }
}