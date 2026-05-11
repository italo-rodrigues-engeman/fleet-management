package com.indux.modules.organization_chart.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name="tb_projetos_mega")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MegaEntity {
    @Id
    @Column(name = "pro_in_reduzido")
    private Integer cusInReduzido;

    @Column(name = "pro_st_extenso")
    private String cusStExtenso;

    @Column(name = "pro_st_apelido")
    private String cusStApelido;

    @Column(name = "pro_st_descricao")
    private String cusStDescricao;

    @Column(name = "pro_bo_ativosaldo")
    private char cusChativado;

    @Column(name = "pro_dt_implantacao",nullable = true)
    private Date cusDtImplantacao;

    @Column(name = "tipo")
    private String type;
}
