package com.tagox.flow.domain.user;

import com.tagox.flow.domain.role.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByTenantIdAndEmail(
            UUID tenantId,
            String email
    );

    Optional<User> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<User> findByTenantIdAndEmail(
            UUID tenantId,
            String email
    );

    @Query("""
            SELECT ur.usuario
            FROM UserRole ur
            WHERE ur.usuario.tenant.id = :tenantId
            AND ur.role.tipo = :roleType
            ORDER BY ur.usuario.nome
            """)
    List<User> findUsersByTenantAndRole(
            @Param("tenantId") UUID tenantId,
            @Param("roleType") RoleType roleType
    );
}
