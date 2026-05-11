package com.indux.modules.modulo_mega.domain.persistence.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Subselect(
    "SELECT * FROM ( " +
"    SELECT DISTINCT " +
"        COALESCE(nt.id, pd.id) AS id, " +
"        COALESCE(nt.id_item, pd.id_item) AS id_item, " +
"        it.item AS descricao_item, " +
"        COALESCE(nt.uni_med_item, pd.uni_med_item, it.uni_med_item) AS unidade_medida, " +
"        pd.num_solicitacao AS numero_solicitacao, " +
"        pd.num_pedido AS numero_pedido, " +
"        CAST(nt.num_nota AS text) AS numero_nota, " +
"        CAST(nt.ap AS text) AS numero_ap, " +
"        pd.tipo_pedido AS tipo_pedido, " +
"        COALESCE(nt.cod_agente, pd.cod_agente) AS codigo_fornecedor, " +
"        fnc.nome_agente AS fornecedor, " +
"        fnc.cnpj AS cnpj, " +
"        it.id_grupo AS codigo_grupo, " +
"        ig.grupo AS nome_grupo, " +
"        CASE " +
"            WHEN COALESCE(nt.uni_med_item, pd.uni_med_item, it.uni_med_item) = 'SE' THEN 'Serviço' " +
"            ELSE 'Produto' " +
"        END AS tipo, " +
"        pd.dt_emissao_soli AS data_solicitacao, " +
"        pd.dt_emissao_ped AS data_pedido, " +
"        nt.dt_nota AS data_nota, " +
"        pd.dt_apv_pedido AS data_aprovacao_pedido, " +
"        pd.dt_prev_entrega AS data_entrega, " +
"        pd.situacao_pedido AS status_pedido, " +
"        it.status_item AS status_item, " +
"        pd.quantidade AS quantidade_pedido, " +
"        pd.valor_unit AS valor_unitario_pedido, " +
"        ROUND(CAST((pd.valor_unit * pd.quantidade) AS numeric), 2) AS valor_total_pedido, " +
"        nt.quantidade AS quantidade_nota, " +
"        nt.valor_unit AS valor_unitario_nota, " +
"        ROUND(CAST((nt.valor_unit * nt.quantidade) AS numeric), 2) AS valor_total_nota, " +
"        pd.nome_comprador AS comprador, " +
"        pd.nome_solicitante AS solicitante, " +
"        COALESCE(nt.id_filial, pd.id_filial) AS codigo_filial_mega, " +
"        fil.nome_filial AS filial_mega, " +
"        COALESCE(nt.id_projeto, pd.id_projeto) AS codigo_projeto, " +
"        proj.pro_st_descricao AS projeto_mega, " +
"        it.dt_inclusao AS data_cadastro, " +
"        it.usuario_inclusao AS usuario_inclusao, " +
"        it.usuario_alteracao AS usuario_alteracao, " +
"        it.previsao_inativacao AS previsao_inativacao, " +
"        COALESCE(nt.quantidade, pd.quantidade) AS total_quantidade_compras, " +
"        COALESCE(nt.valor_unit, pd.valor_unit) AS total_valor_unitario, " +
"        COALESCE(nt.valor_item_proj, pd.valor_total_item) AS total_valor_total_compras " +
"    FROM tb_pedidos_mega pd " +
"    FULL JOIN tb_notas_mega nt " +
"        ON  pd.id_projeto = nt.id_projeto " +
"        AND pd.cod_agente = nt.cod_agente " +
"        AND pd.id_item = nt.id_item " +
"        AND pd.num_pedido = nt.num_pedido " +
"        AND pd.itp_sequencia = nt.itp_sequencia " +
"    LEFT JOIN tb_itens_mega it " +
"        ON COALESCE(nt.id_item, pd.id_item) = it.id_item " +
"    LEFT JOIN tb_itens_grupos_mega ig " +
"        ON ig.id_grupo = it.id_grupo " +
"    LEFT JOIN tb_projetos_mega proj " +
"        ON COALESCE(nt.id_projeto, pd.id_projeto) = proj.pro_in_reduzido " +
"    LEFT JOIN tb_fornecedores_mega fnc " +
"        ON COALESCE(nt.cod_agente, pd.cod_agente) = fnc.id_agente " +
"    LEFT JOIN tb_filiais fil " +
"        ON COALESCE(nt.id_filial, pd.id_filial) = fil.codigo_filial " +
"    WHERE ( " +
"        pd.situacao_pedido IN ('Pedido Atendido', 'Pedido Encerrado', 'Pedido em Aberto', 'Pedido em Aprovação') " +
"        OR nt.num_nota IS NOT NULL " +
"    ) " +
"    GROUP BY " +
"        COALESCE(nt.id, pd.id), " +
"        COALESCE(nt.id_item, pd.id_item), " +
"        it.item, " +
"        COALESCE(nt.uni_med_item, pd.uni_med_item, it.uni_med_item), " +
"        pd.num_solicitacao, " +
"        pd.num_pedido, " +
"        nt.num_nota, " +
"        nt.ap, " +
"        pd.tipo_pedido, " +
"        nt.dt_nota, " +
"        COALESCE(nt.cod_agente, pd.cod_agente), " +
"        pd.quantidade, " +
"        pd.valor_unit, " +
"        ROUND(CAST((pd.valor_unit * pd.quantidade) AS numeric), 2), " +
"        nt.quantidade, " +
"        nt.valor_unit, " +
"        ROUND(CAST((nt.valor_unit * nt.quantidade) AS numeric), 2), " +
"        fnc.nome_agente, " +
"        fnc.cnpj, " +
"        it.id_grupo, " +
"        ig.grupo, " +
"        pd.dt_emissao_soli, " +
"        pd.dt_emissao_ped, " +
"        pd.dt_apv_pedido, " +
"        pd.dt_prev_entrega, " +
"        pd.situacao_pedido, " +
"        it.status_item, " +
"        pd.nome_comprador, " +
"        pd.nome_solicitante, " +
"        COALESCE(nt.id_filial, pd.id_filial), " +
"        fil.nome_filial, " +
"        COALESCE(nt.id_projeto, pd.id_projeto), " +
"        proj.pro_st_descricao, " +
"        it.dt_inclusao, " +
"        it.usuario_inclusao, " +
"        it.usuario_alteracao, " +
"        it.previsao_inativacao, " +
"        nt.valor_item_proj, " +
"        pd.valor_total_item " +
") AS resultado"
)
@Synchronize({
    "tb_pedidos_mega", "tb_itens_mega", "tb_itens_grupos_mega", 
    "tb_projetos_mega", " tb_fornecedores_mega", "tb_filiais", "tb_notas_mega"
})
@Data
@Deprecated
public class MegaEntityOrder implements Serializable {

    @Id 
    @Column(name = "id") 
    private String id;

    @Column(name = "id_item")
    private Integer idItem; 

    @Column(name = "descricao_item")
    private String itemName;

    @Column(name = "unidade_medida")
    private String unitOfMeasure;

    @Column(name = "numero_pedido")
    private Integer orderNumber;

    @Column(name = "tipo_pedido")
    private String orderType;

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
    private String itemType; 

    @Column(name = "numero_nota")
    private String noteNumber;

    @Column(name = "numero_solicitacao")
    private Integer solicitation;

    @Column(name = "data_pedido")
    private LocalDate orderDate;

    

    @Column(name = "data_aprovacao_pedido")
    private LocalDate approvalDate;

    @Column(name = "data_entrega")
    private LocalDate deliveryDate;

    @Column(name = "data_nota")
    private LocalDate invoiceDate;

    @Column(name = "status_pedido")
    private String orderStatus; 

    @Column(name = "status_item")
    private String itemStatus;

    @Column(name = "total_quantidade_compras")
    private BigDecimal qtdItensTotal;

    @Column(name = "total_valor_unitario")
    private BigDecimal averagePriceFromSupplier;

    @Column(name = "total_valor_total_compras")
    private BigDecimal totalItemValue;

    @Column(name = "comprador")
    private String buyerName;

    @Column(name = "codigo_filial_mega")
    private Integer branchId; 

    @Column(name = "filial_mega")
    private String branchName; 

    @Column(name = "codigo_projeto")
    private Integer projectCode;

    @Column(name = "projeto_mega")
    private String projectName;

    @Column(name = "data_cadastro")
    private LocalDate creationDate; 
    
    @Column(name = "data_solicitacao")
    private LocalDate solDate;

    @Column(name = "numero_ap")
    private String approve;

    @Column(name = "solicitante")
    private String requesterName;

    @Column(name = "usuario_inclusao")
    private String registerUser;

    @Column(name = "usuario_alteracao")
    private String registerUserEdition;

    @Column(name = "previsao_inativacao")
    private LocalDate previsaoInativacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_projeto", referencedColumnName = "id_projeto", insertable = false, updatable = false)
    private MegaEntityOrganogram organogram;
}