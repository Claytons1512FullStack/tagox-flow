package com.tagox.flow.service;

import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.dto.user.CreateUserRequest;
import com.tagox.flow.exception.DuplicateResourceException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class UserService {


    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;


    public UserService(
            UserRepository userRepository,
            TenantRepository tenantRepository
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }



    @Transactional
    public User criarUsuario(
            CreateUserRequest request
    ) {


        Tenant tenant = tenantRepository.findById(
                        request.getTenantId()
                )
                .orElseThrow(() ->
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



        User user = new User(
                UUID.randomUUID(),
                tenant,
                request.getNome(),
                emailNormalizado,
                request.getSenhaHash(),
                UserStatus.ATIVO
        );


        return userRepository.save(user);

    }

}