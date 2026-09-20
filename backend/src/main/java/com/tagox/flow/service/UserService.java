package com.tagox.flow.service;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.DuplicateResourceException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User criarUsuario(
            CreateUserRequest request,
            UUID tenantId
    ) {

        Tenant tenant = tenantRepository.findById(
                tenantId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Tenant não encontrado"
                )
        );

        String emailNormalizado = request.getEmail()
                .toLowerCase();

        if (userRepository.existsByTenantIdAndEmail(
                tenant.getId(),
                emailNormalizado
        )) {

            throw new DuplicateResourceException(
                    "Já existe um usuário com este email neste tenant"
            );
        }

        String senhaHash = passwordEncoder.encode(
                request.getSenha()
        );

        User user = new User(
                UUID.randomUUID(),
                tenant,
                request.getNome(),
                emailNormalizado,
                senhaHash,
                UserStatus.ATIVO
        );

        return userRepository.save(user);
    }
}