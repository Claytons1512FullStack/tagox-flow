package com.tagox.flow.domain.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface RoleRepository extends JpaRepository<Role, UUID> {


    Optional<Role> findByTipo(RoleType tipo);

}
