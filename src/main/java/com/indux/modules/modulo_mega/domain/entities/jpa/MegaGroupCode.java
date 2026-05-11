package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_itens_grupos_mega")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MegaGroupCode {
    @Id
    @Column(name = "id_grupo")
    Integer id;

    @Column(name = "grupo")
    String name;

    @Column(name = "id_grupo_extenso")
    Integer extendedGroupId;

    @Column(name = "identificador")
    Integer identifier;

    @Column(name = "status_grupo")
    Boolean status;

}
