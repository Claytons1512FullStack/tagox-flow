package com.tagox.flow.domain.role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;


@Entity
@Table(name = "role")
public class Role {


    @Id
    private UUID id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleType tipo;


    @Column(nullable = false, length = 150)
    private String descricao;



    protected Role() {
        // Construtor protegido exigido pelo Hibernate
    }



    public Role(
            UUID id,
            RoleType tipo,
            String descricao
    ) {

        this.id = id;
        this.tipo = tipo;
        this.descricao = descricao;

    }



    @PrePersist
    private void antesDePersistir(){

        if(this.id == null){
            this.id = UUID.randomUUID();
        }

    }



    public UUID getId() {
        return id;
    }


    public RoleType getTipo() {
        return tipo;
    }


    public String getDescricao() {
        return descricao;
    }

}