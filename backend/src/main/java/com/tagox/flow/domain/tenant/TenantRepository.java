package com.tagox.flow.domain.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsByDocumento(String documento);

    boolean existsBySlug(String slug);
}