package com.tagox.flow.domain.userrole;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

@Embeddable
public class UserRoleId implements Serializable {

    private UUID usuarioId;

    private UUID roleId;


    protected UserRoleId() {
        // Construtor protegido exigido pelo Hibernate
    }


    public UserRoleId(
            UUID usuarioId,
            UUID roleId
    ) {
        this.usuarioId = usuarioId;
        this.roleId = roleId;
    }


    public UUID getUsuarioId() {
        return usuarioId;
    }


    public UUID getRoleId() {
        return roleId;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof UserRoleId)) {
            return false;
        }

        UserRoleId that = (UserRoleId) o;

        return Objects.equals(usuarioId, that.usuarioId)
                && Objects.equals(roleId, that.roleId);
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                usuarioId,
                roleId
        );
    }
}