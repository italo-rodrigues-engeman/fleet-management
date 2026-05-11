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
@Subselect(
    "SELECT " +
    "    mp.id AS id, " +
    "    mp.id_item AS codigo_item, " +
    "    it.item AS descricao_item, " +
    "    mp.uni_med_item AS unidade_medida, " +
    "    mp.num_cotacao AS numero_cotacao, " +
    "    mp.num_pedido AS numero_pedido, " +
    "    mp.dt_emissao_cotacao AS data_cotacao, " +
    "    mp.dt_apv_cotacao AS data_aprovacao_cotacao, " +
    "    mp.desc_cotacao AS descricao_cotacao, " +
    "    mp.status_cotacao AS status_cotacao, " +
    "    it.id_grupo AS codigo_grupo, " +
    "    ig.grupo AS nome_grupo, " +
    "    CASE " +
    "        WHEN COALESCE(mp.uni_med_item, '') = 'SE' THEN 'Serviço' " +
    "        ELSE 'Produto' " +
    "    END AS tipo, " +
    "    mp.cod_agente AS codigo_fornecedor, " +
    "    fnc.nome_agente AS fornecedor, " +
    "    fnc.cnpj AS cnpj, " +
    "    mp.quantidade AS quantidade_cotacao, " +
    "    mp.valor_unit AS valor_unitario, " +
    "    mp.valor_total_item AS valor_total, " +
    "    mp.custo_ultima_compra AS custo_ultima_compra, " +
    "    mp.pedido_gerado AS pedido_gerado, " +
    "    mp.nome_usu_cotacao AS nome_usuario_cotacao, " +
    "    mp.id_filial AS codigo_filial_mega, " +
    "    fil.nome_filial AS filial_mega " +
    "FROM tb_mapa_mega mp " +
    "LEFT JOIN tb_itens_mega it ON mp.id_item = it.id_item " +
    "LEFT JOIN tb_itens_grupos_mega ig ON ig.id_grupo = it.id_grupo " +
    "LEFT JOIN tb_filiais fil ON mp.id_filial = fil.codigo_filial " +
    "LEFT JOIN tb_fornecedores_mega fnc ON mp.cod_agente = fnc.id_agente " +
    "WHERE mp.pedido_gerado = 'S'"
)
@Synchronize({"tb_mapa_mega", "tb_itens_mega", "tb_itens_grupos_mega", "tb_filiais", "tb_fornecedores_mega"})
@Data
public class MegaEntityQuotation implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "codigo_item")
    private Integer itemCode;

    @Column(name = "descricao_item")
    private String itemName;

    @Column(name = "unidade_medida")
    private String unitOfMeasure;

    @Column(name = "numero_cotacao")
    private Integer quotationNumber;

    @Column(name = "numero_pedido")
    private Integer orderNumber;

    @Column(name = "data_cotacao")
    private LocalDate quotationDate;

    @Column(name = "data_aprovacao_cotacao")
    private LocalDate quotationDateApprove;

    @Column(name = "descricao_cotacao")
    private String quotationDescription;

    @Column(name = "status_cotacao")
    private String quotationStatus;

    @Column(name = "codigo_grupo") 
    private Integer groupCode;

    @Column(name = "nome_grupo")
    private String groupName;

    @Column(name = "tipo")
    private String quotationType;

    @Column(name = "codigo_fornecedor")
    private Integer supplierCode;

    @Column(name = "fornecedor")
    private String supplierName;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "quantidade_cotacao")
    private BigDecimal quotationQuantity;

    @Column(name = "valor_unitario")
    private BigDecimal unitValue;

    @Column(name = "valor_total")
    private BigDecimal totalValue;

    @Column(name = "custo_ultima_compra")
    private BigDecimal costOfLastPurchase;

    @Column(name = "pedido_gerado")
    private String orderGenerated;

    @Column(name = "nome_usuario_cotacao")
    private String quotationUserName;

    @Column(name = "codigo_filial_mega")
    private Integer branchId;

    @Column(name = "filial_mega")
    private String branchName;

}