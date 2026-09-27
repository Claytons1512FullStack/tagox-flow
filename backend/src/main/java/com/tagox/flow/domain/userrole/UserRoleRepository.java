package com.tagox.flow.domain.userrole;

import com.tagox.flow.domain.role.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByIdUsuarioIdAndIdRoleId(
            UUID usuarioId,
            UUID roleId
    );

    @Query("""
            SELECT ur.role.tipo
            FROM UserRole ur
            WHERE ur.usuario.id = :usuarioId
            """)
    List<RoleType> findRoleTypesByUsuarioId(
            @Param("usuarioId") UUID usuarioId
    );
}