package com.indux.modules.modulo_mega.domain.persistence.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

@Entity
@Immutable
@Table(name = "mega_entity_itens")
@Subselect(
    "SELECT " +
    "    CAST(nt.id AS VARCHAR) || '-' || ROW_NUMBER() OVER () AS id, " + 
    "    nt.id_item AS codigo_item, " +
    "    it.item AS descricao_item, " +
    "    nt.uni_med_item AS unidade_medida, " +
    "    nt.num_pedido AS numero_pedido, " +
    "    nt.num_nota AS numero_nota, " +
    "    nt.ap AS numero_ap, " +
    "    nt.tipo_nota AS tipo_documento, " +
    "    nt.cod_agente AS codigo_fornecedor, " +
    "    fnc.nome_agente AS fornecedor, " +
    "    fnc.cnpj AS cnpj, " +
    "    it.id_grupo AS codigo_grupo, " +
    "    ig.grupo AS nome_grupo, " +
    "    CASE " +
    "        WHEN nt.uni_med_item = 'SE' THEN 'Serviço' " +
    "        ELSE 'Produto' " +
    "    END AS tipo, " +
    "    nt.dt_nota AS data_nota, " +
    "    (nt.valor_unit)::numeric AS valor_unitario, " +
    "    ROUND((nt.valor_unit * nt.quantidade)::numeric, 2) AS valor_total, " +
    "    nt.quantidade AS quantidade, " +
    "    nt.id_filial AS codigo_filial_mega, " +
    "    fil.nome_filial AS filial_mega, " +
    "    nt.id_projeto AS codigo_projeto, " +
    "    proj.pro_st_descricao AS projeto_mega, " +
    "    nt.valor_item_proj AS valor_item_proj, " +
    "    it.dt_inclusao AS data_cadastro, " +
    "    it.usuario_inclusao AS usuario_inclusao, " +
    "    it.usuario_alteracao AS usuario_alteracao " +
    "FROM tb_notas_mega nt " +
    "LEFT JOIN tb_itens_mega it ON nt.id_item = it.id_item " +
    "LEFT JOIN tb_itens_grupos_mega ig ON it.id_grupo = ig.id_grupo " +
    "LEFT JOIN tb_projetos_mega proj ON nt.id_projeto = proj.pro_in_reduzido " +
    "LEFT JOIN tb_fornecedores_mega fnc ON nt.cod_agente = fnc.id_agente " +
    "LEFT JOIN tb_filiais fil ON nt.id_filial = fil.codigo_filial " +
    "WHERE nt.ap IS NOT NULL"
    
)
@Synchronize({"tb_pedidos_mega", "tb_itens_mega", "tb_itens_grupos_mega"})
@Data
public class MegaEntityInvoice implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "codigo_item")
    private Integer idItem;

    @Column(name = "descricao_item")
    private String itemName;

    @Column(name = "unidade_medida")
    private String unitOfMeasure;

    @Column(name = "numero_pedido")
    private Integer orderNumber;

    @Column(name = "numero_nota")
    private String noteNumber;

    @Column(name = "numero_ap")
    private String approve;

    @Column(name = "tipo_documento")
    private String docType;

    @Column(name = "codigo_fornecedor")
    private Integer supplierCode;

    @Column(name = "fornecedor")
    private String supplierName;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "codigo_grupo") 
    private Integer groupCode;

    @Column(name = "nome_grupo")
    private String groupName;

    @Column(name = "tipo")
    private String invoiceType;
    
    @Column(name = "data_nota")
    private LocalDate invoiceDate;

    @Column(name = "valor_unitario")
    private BigDecimal averagePriceFromSupplier;

    @Column(name = "valor_total")
    private BigDecimal totalItemValue;

    @Column(name = "quantidade")
    private BigDecimal qtdItensTotal;

    @Column(name = "codigo_filial_mega")
    private Integer branchId;

    @Column(name = "filial_mega")
    private String branchName;

    @Column(name = "codigo_projeto")
    private Integer projectCode;

    @Column(name = "projeto_mega")
    private String projectName;

    @Column(name = "valor_item_proj")
    private String projectValue;

    @Column(name = "data_cadastro")
    private LocalDate creationDate;

    @Column(name = "usuario_inclusao")
    private String registerUser;

    @Column(name = "usuario_alteracao")
    private String registerUserEdition;

    
}