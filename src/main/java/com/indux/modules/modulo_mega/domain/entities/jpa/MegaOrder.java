package com.indux.modules.modulo_mega.domain.entities.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "vw_mega_orders")
@Getter
public class MegaOrder {
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
}
