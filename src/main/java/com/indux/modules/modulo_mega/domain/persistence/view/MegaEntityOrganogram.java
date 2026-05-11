package com.indux.modules.modulo_mega.domain.persistence.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import lombok.Data;

import java.io.Serializable;

@Entity
@Immutable
@Table(name = "v_organograma_hierarquia")
@Subselect(
    
    "SELECT DISTINCT " +
    "    COALESCE(proj.pro_in_reduzido, pro.mega)       AS id_projeto, " +
    "    proj.pro_st_descricao                         AS nome_projeto, " +
    "    cont.id                                       AS id_contrato, " +
    "    cont.nome                                     AS nome_contrato, " +
    "    cont.os                                       AS os, " +
    "    COALESCE( " +
    "        set.id, " +
    "        CASE WHEN org_base.tipo = 3 THEN org_base.id END " +
    "    )                                             AS id_setor, " +
    "    COALESCE( " +
    "        set.cargo, " +
    "        CASE WHEN org_base.tipo = 3 THEN org_base.cargo END " +
    "    )                                             AS nome_setor, " +
    "    COALESCE( " +
    "        set.sigla, " +
    "        CASE WHEN org_base.tipo = 3 THEN org_base.sigla END " +
    "    )                                             AS sigla_setor, " +
    "    COALESCE( " +
    "        reg1.id, " +
    "        reg2.id, " +
    "        CASE WHEN org_base.tipo = 2 THEN org_base.id END " +
    "    )                                             AS id_regional, " +
    "    COALESCE( " +
    "        reg1.cargo, " +
    "        reg2.cargo, " +
    "        CASE WHEN org_base.tipo = 2 THEN org_base.cargo END " +
    "    )                                             AS nome_regional, " +
    "    COALESCE( " +
    "        reg1.sigla, " +
    "        reg2.sigla, " +
    "        CASE WHEN org_base.tipo = 2 THEN org_base.sigla END " +
    "    )                                             AS sigla_regional, " +
    "    COALESCE( " +
    "        sup1.id, " +
    "        sup2.id, " +
    "        CASE WHEN org_base.tipo = 1 THEN org_base.id END " +
    "    )                                             AS id_superintendencia, " +
    "    COALESCE( " +
    "        sup1.cargo, " +
    "        sup2.cargo, " +
    "        CASE WHEN org_base.tipo = 1 THEN org_base.cargo END " +
    "    )                                             AS nome_superintendencia, " +
    "    COALESCE( " +
    "        sup1.sigla, " +
    "        sup2.sigla, " +
    "        CASE WHEN org_base.tipo = 1 THEN org_base.sigla END " +
    "    )                                             AS sigla_superintendencia, " +
    "    COALESCE( " +
    "        dir.id, " +
    "        CASE WHEN org_base.tipo = 0 THEN org_base.id END " +
    "    )                                             AS id_diretoria, " +
    "    COALESCE( " +
    "        dir.cargo, " +
    "        CASE WHEN org_base.tipo = 0 THEN org_base.cargo END " +
    "    )                                             AS nome_diretoria, " +
    "    COALESCE( " +
    "        dir.sigla, " +
    "        CASE WHEN org_base.tipo = 0 THEN org_base.sigla END " +
    "    )                                             AS sigla_diretoria " +
    "FROM tb_projetos_mega proj " +
    "LEFT JOIN tb_organograma_projeto pro " +
    "    ON proj.pro_in_reduzido = pro.mega " +
    "LEFT JOIN tb_organograma_contrato cont " +
    "    ON pro.contrato = cont.id " +
    "LEFT JOIN tb_organograma set " +
    "    ON cont.subordinado = set.id " +
    "   AND set.tipo = 3 " +
    "LEFT JOIN tb_organograma reg1 " +
    "    ON set.subordinado = reg1.id " +
    "   AND reg1.tipo = 2 " +
    "LEFT JOIN tb_organograma reg2 " +
    "    ON reg1.id IS NULL " +
    "   AND cont.subordinado = reg2.id " +
    "   AND reg2.tipo = 2 " +
    "LEFT JOIN tb_organograma sup1 " +
    "    ON (reg1.subordinado = sup1.id OR reg2.subordinado = sup1.id) " +
    "   AND sup1.tipo = 1 " +
    "LEFT JOIN tb_organograma sup2 " +
    "    ON sup1.id IS NULL " +
    "   AND set.subordinado = sup2.id " +
    "   AND sup2.tipo = 1 " +
    "LEFT JOIN tb_organograma dir " +
    "    ON (sup1.subordinado = dir.id OR sup2.subordinado = dir.id) " +
    "   AND dir.tipo = 0 " +
    "LEFT JOIN tb_organograma org_base " +
    "    ON pro.contrato IS NULL " +
    "   AND pro.subordinado = org_base.id"
)
@Data
public class MegaEntityOrganogram implements Serializable {

    // VARIÁVEL (Campo Java) em inglês (camelCase)
    // PROPERTY (Anotação de Coluna) em português (snake_case), conforme o SELECT
    @Id
    @Column(name = "id_projeto")
    private Integer projectCode;

    // Campos restantes (a projeção final do seu SELECT)
    
    @Column(name = "id_contrato")
    private Integer contractCode;

    @Column(name = "nome_contrato")
    private String contractName;

    @Column(name = "id_setor")
    private Integer sectorId;
    
    @Column(name = "nome_setor")
    private String sectorName;

    @Column(name = "sigla_setor")
    private String acronymSector;

    @Column(name = "id_regional")
    private Integer regionalCode;
    
    @Column(name = "nome_regional")
    private String regionalName;

    @Column(name = "sigla_regional")
    private String acronymRegional;

    @Column(name = "id_superintendencia")
    private Integer superId;
    
    @Column(name = "nome_superintendencia")
    private String superName;

    @Column(name = "sigla_superintendencia")
    private String acronymSuper;

    @Column(name = "id_diretoria")
    private Integer directoryId;
    
    @Column(name = "nome_diretoria")
    private String directoryName;

    @Column(name = "sigla_diretoria")
    private String acronymDirectory;

   
}