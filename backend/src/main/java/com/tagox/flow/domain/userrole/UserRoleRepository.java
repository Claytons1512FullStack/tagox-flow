package com.tagox.flow.domain.userrole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByIdUsuarioIdAndIdRoleId(
            UUID usuarioId,
            UUID roleId
    );
}