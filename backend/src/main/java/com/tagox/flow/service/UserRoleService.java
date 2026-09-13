package com.tagox.flow.service;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.role.RoleRepository;
import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.userrole.UserRole;
import com.tagox.flow.domain.userrole.UserRoleRepository;
import com.tagox.flow.exception.DuplicateResourceException;
import com.tagox.flow.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class UserRoleService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;



    public UserRoleService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;

    }



    @Transactional
    public UserRole adicionarRole(
            UUID userId,
            RoleType roleType
    ) {


        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado"
                        )
                );


        Role role = roleRepository.findByTipo(roleType)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role não encontrada"
                        )
                );


        if (userRoleRepository.existsByIdUsuarioIdAndIdRoleId(
                user.getId(),
                role.getId()
        )) {

            throw new DuplicateResourceException(
                    "Usuário já possui esta role"
            );

        }


        UserRole userRole = new UserRole(
                user,
                role
        );


        return userRoleRepository.save(userRole);

    }

}