package com.tagox.flow.domain.userrole;

import com.tagox.flow.domain.role.Role;
import com.tagox.flow.domain.user.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;


@Entity
@Table(name = "usuario_role")
public class UserRole {


    @EmbeddedId
    private UserRoleId id;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(
            name = "usuario_id",
            nullable = false
    )
    private User usuario;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;



    protected UserRole() {
        // Construtor protegido exigido pelo Hibernate
    }



    public UserRole(
            User usuario,
            Role role
    ) {

        this.usuario = usuario;
        this.role = role;

        this.id = new UserRoleId(
                usuario.getId(),
                role.getId()
        );
    }



    public UserRoleId getId() {
        return id;
    }


    public User getUsuario() {
        return usuario;
    }


    public Role getRole() {
        return role;
    }
}
