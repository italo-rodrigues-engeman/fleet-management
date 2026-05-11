package com.indux.modules.modulo_mega.domain.persistence.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
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
    "    CAST(sl.id AS VARCHAR) || '-' || ROW_NUMBER() OVER () AS id, " + 
    "    sl.id_item AS codigo_item, " +
    "    it.item AS descricao_item, " +
    "    sl.uni_med_item AS unidade_medida, " +
    "    sl.num_pedido AS numero_pedido, " +
    "    sl.num_solicitacao AS numero_solicitacao, " +
    "    sl.dt_emissao AS data_solicitacao, " +
    "    sl.dt_apv_solicitacao AS data_aprovacao_solicitacao, " +
    "    it.id_grupo AS codigo_grupo, " +
    "    ig.grupo AS nome_grupo, " +
    "    CASE " +
    "        WHEN COALESCE(sl.uni_med_item, '') = 'SE' THEN 'Serviço' " +
    "        ELSE 'Produto' " +
    "    END AS tipo, " +
    "    it.status_item AS status_item, " +
    "    sl.qtd_solicitada AS quantidade_solicitada, " +
    "    sl.nome_solicitante AS solicitante, " +
    "    sl.id_filial AS codigo_filial_mega, " +
    "    fil.nome_filial AS filial_mega, " +
    "    sl.id_projeto AS codigo_projeto, " +
    "    proj.pro_st_descricao AS projeto_mega, " +
    "    it.dt_inclusao AS data_cadastro, " +
    "    it.usuario_inclusao AS usuario_inclusao, " +
    "    it.usuario_alteracao AS usuario_alteracao, " +
    "    sl.cod_cotacao AS numero_cotacao " +
    "FROM tb_solicitacoes_mega sl " +
    "LEFT JOIN tb_itens_mega it ON sl.id_item = it.id_item " +
    "LEFT JOIN tb_itens_grupos_mega ig ON ig.id_grupo = it.id_grupo " +
    "LEFT JOIN tb_projetos_mega proj ON sl.id_projeto = proj.pro_in_reduzido " +
    "LEFT JOIN tb_filiais fil ON sl.id_filial = fil.codigo_filial"
)
@Synchronize({"tb_solicitacoes_mega", "tb_itens_mega", "tb_itens_grupos_mega", "tb_projetos_mega", "tb_filiais"})
@Data
public class MegaEntitySolicitation implements Serializable {

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

    @Column(name = "numero_solicitacao")
    private Integer solicitation;

    @Column(name = "data_solicitacao")
    private LocalDate solDate;

    @Column(name = "data_aprovacao_solicitacao")
    private LocalDate approvalDate;

    @Column(name = "codigo_grupo")
    private Integer groupCode;

    @Column(name = "nome_grupo")
    private String groupName;

    @Column(name = "tipo")
    private String itemType; // Serviço ou Produto

    @Column(name = "status_item")
    private String itemStatus;

    @Column(name = "quantidade_solicitada")
    private BigDecimal requestedQuantity;

    @Column(name = "solicitante")
    private String requesterName;

    @Column(name = "codigo_filial_mega")
    private Integer branchId;

    @Column(name = "filial_mega")
    private String branchName;

    @Column(name = "codigo_projeto")
    private Integer projectId;

    @Column(name = "projeto_mega")
    private String projectName;

    @Column(name = "data_cadastro")
    private LocalDate creationDate;

    @Column(name = "usuario_inclusao")
    private String registerUser;

    @Column(name = "usuario_alteracao")
    private String registerUserEdition;

    @Column(name = "numero_cotacao")
    private Integer quotationNumber;
}