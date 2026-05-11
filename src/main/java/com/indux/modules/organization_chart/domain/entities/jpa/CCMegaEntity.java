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
@Table(name="tb_centro_custos_mega")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CCMegaEntity {
    @Id
    @Column(name = "cus_in_reduzido")
    private Integer cusInReduzido;

    @Column(name = "cus_st_extenso")
    private String cusStExtenso;

    @Column(name = "cus_st_apelido")
    private String cusStApelido;

    @Column(name = "cus_st_descricao")
    private String cusStDescricao;

    @Column(name = "cus_ch_ativado")
    private char cusChativado;

    @Column(name = "cus_dt_ativado",nullable = true)
    private Date cusDtImplantacao;
}
