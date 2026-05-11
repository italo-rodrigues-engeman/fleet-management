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

@Entity
@Immutable
@Table(name = "mega_entity_itens")
@Subselect(
    "SELECT * " +
"FROM " +
"( " +
"    SELECT " +
"        it.id_item, " +
"        MAX(it.item) AS nome_item, " +
"        MAX(COALESCE(dados.nt_uni_med, dados.pd_uni_med, it.uni_med_item)) AS unidade_medida, " +
"        it.id_grupo AS id_grupo, " +
"        MAX(ig.grupo) AS nome_grupo, " +
"        MAX(CASE WHEN COALESCE(dados.nt_uni_med, dados.pd_uni_med, it.uni_med_item) = 'SE' THEN 'Serviço' ELSE 'Produto' END) AS tipo, " +
"        MAX(it.dt_inclusao) AS data_cadastro, " +
"        MAX(it.status_item) AS status_item, " +
"        COALESCE(SUM(dados.quantidade), 0) AS quantidade_pedido, " +
"        ROUND(CAST(COALESCE(SUM(dados.valor_unit * dados.quantidade), 0) AS numeric), 2) AS valor_total_pedido, " +
"        ROUND(CAST((COALESCE(SUM(dados.valor_unit * dados.quantidade), 0) / NULLIF(SUM(dados.quantidade), 0)) AS numeric), 2) AS preco_medio_pedido, " +
"        COUNT(dados.num_pedido) AS numero_de_pedidos, " +
"        COALESCE(SUM(dados.nt_quantidade), 0) AS quantidade_compras, " +
"        ROUND(CAST(COALESCE(SUM(dados.valor_item_proj), 0) AS numeric), 2) AS valor_total_compras, " +
"        ROUND(CAST((COALESCE(SUM(dados.valor_item_proj), 0) / NULLIF(SUM(dados.nt_quantidade), 0)) AS numeric), 2) AS preco_medio_compra, " +
"        COUNT(dados.num_nota) AS numero_de_compras, " +
"        SUM(COALESCE(dados.nt_quantidade, dados.quantidade)) AS total_quantidade_compras, " +
"        ROUND(CAST(SUM(COALESCE(dados.valor_item_proj, dados.valor_unit * dados.quantidade)) AS numeric), 2) AS total_valor_total_compras, " +
"        ROUND( " +
"            CAST((SUM(COALESCE(dados.valor_item_proj, dados.valor_unit * dados.quantidade)) / " +
"             NULLIF(SUM(COALESCE(dados.nt_quantidade, dados.quantidade)), 0)) AS numeric) " +
"        , 2) AS preco_medio, " +
"        COUNT(COALESCE(dados.nt_id, dados.id)) AS total_numero_de_compras " +
"    FROM tb_itens_mega it " +
"    LEFT JOIN ( " +
"        SELECT " +
"            COALESCE(pd.id_item, nt.id_item) AS id_item, " +
"            pd.id, " +
"            pd.quantidade, " +
"            pd.valor_unit, " +
"            pd.num_pedido, " +
"            pd.situacao_pedido, " +
"            pd.uni_med_item AS pd_uni_med, " +
"            nt.id AS nt_id, " +
"            nt.quantidade AS nt_quantidade, " +
"            nt.valor_unit AS nt_valor_unit, " +
"            nt.valor_item_proj, " +
"            nt.num_nota, " +
"            nt.uni_med_item AS nt_uni_med, " +
"            COALESCE(nt.id_projeto, pd.id_projeto) AS id_projeto, " +
"            COALESCE(nt.cod_agente, pd.cod_agente) AS cod_agente, " +
"            COALESCE(nt.id_filial, pd.id_filial) AS id_filial " +
"        FROM tb_pedidos_mega pd " +
"        FULL JOIN tb_notas_mega nt " +
"            ON  pd.id_projeto = nt.id_projeto " +
"            AND pd.cod_agente = nt.cod_agente " +
"            AND pd.id_item = nt.id_item " +
"            AND pd.num_pedido = nt.num_pedido " +
"            AND pd.itp_sequencia = nt.itp_sequencia " +
"        WHERE ( " +
"            pd.situacao_pedido IN ('Pedido Atendido', 'Pedido Encerrado', 'Pedido em Aberto', 'Pedido em Aprovação') " +
"            OR nt.num_nota IS NOT NULL " +
"        ) " +
"    ) dados ON it.id_item = dados.id_item " +
"    LEFT JOIN tb_itens_grupos_mega ig " +
"        ON ig.id_grupo = it.id_grupo " +
"    LEFT JOIN tb_projetos_mega proj " +
"        ON dados.id_projeto = proj.pro_in_reduzido " +
"    LEFT JOIN tb_fornecedores_mega fnc " +
"        ON dados.cod_agente = fnc.id_agente " +
"    LEFT JOIN tb_filiais fil " +
"        ON dados.id_filial = fil.codigo_filial " +
"    GROUP BY it.id_item " +
") AS resultado"
)
@Synchronize({"tb_pedidos_mega", "tb_itens_mega", "tb_itens_grupos_mega", "tb_notas_mega"})
@Data
public class MegaEntityItens implements Serializable {

    @Id
    @Column(name = "id_item")
    private Integer idItem; 

    @Column(name = "nome_item")
    private String itemName; 

    @Column(name = "id_grupo")
    private Integer groupCode; 

    @Column(name = "nome_grupo")
    private String groupName; 
        
    @Column(name = "total_quantidade_compras") 
    private BigDecimal totalQuantity; 
        
    @Column(name = "total_valor_total_compras")
    private BigDecimal totalValue; 

    @Column(name = "preco_medio")
    private BigDecimal averagePrice; 

    @Column(name = "total_numero_de_compras")
    private Long orderCount; 

    @Column(name = "status_item")
    private String status; 

    @Column(name = "data_cadastro")
    private LocalDate creationDate; 

    @Column(name = "unidade_medida")
    private String unitOfMeasure; 

    @Column(name = "tipo")
    private String itemType; 

}