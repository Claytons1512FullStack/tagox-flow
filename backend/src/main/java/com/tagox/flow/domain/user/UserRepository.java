package com.tagox.flow.domain.user;

import com.tagox.flow.domain.role.RoleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT u
            FROM User u
            WHERE u.id = :profissionalId
            AND u.tenant.id = :tenantId
            """)
    Optional<User> findByIdAndTenantIdForUpdate(
            @Param("profissionalId") UUID profissionalId,
            @Param("tenantId") UUID tenantId
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