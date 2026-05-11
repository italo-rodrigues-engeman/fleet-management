package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDetailedDTO {

    @JsonProperty("ap")
    private String approve;

    @JsonProperty("NF")
    private String noteNumber;

    @JsonProperty("id")
    private String id;

    @JsonProperty("codigo_item")
    private Integer idItem;

    @JsonProperty("descricao_item")
    private String itemName;

    

    @JsonProperty("numero_pedido")
    private Integer orderNumber;

   

    @JsonProperty("tipo_pedido")
    private String orderType;

    @JsonProperty("codigo_fornecedor")
    private Integer supplierCode;

    @JsonProperty("nome_fornecedor")
    private String supplierName;

    @JsonProperty("codigo_grupo")
    private Integer groupCode;

    @JsonProperty("nome_grupo")
    private String groupName;

    // --- Localização ---
    @JsonProperty("codigo_filial")
    private Integer branchId;

    @JsonProperty("nome_filial")
    private String branchName;

    @JsonProperty("codigo_projeto")
    private Integer projectCode;

    @JsonProperty("nome_projeto")
    private String projectName;

    @JsonProperty("codigo_regional")
    private Integer regionalCode;

    @JsonProperty("nome_regional")
    private String regionalName;

    @JsonProperty("nome_diretoria")
    private String directoryName;

    @JsonProperty("nome_superintendencia")
    private String superName;

    // --- Datas e Usuários ---
    @JsonProperty("data_criacao")
    private LocalDate creationDate;

    @JsonProperty("usuario_cadastro")
    private String registerUser;

    @JsonProperty("usuario_edicao")
    private String registerUserEdition;

    @JsonProperty("data_solicitacao")
    private LocalDate solDate;

    @JsonProperty("data_pedido") 
    private LocalDate orderDate;

    @JsonProperty("data_entrega")
    private LocalDate deliveryDate;

    @JsonProperty("status_item")
    private String orderStatus;

    // --- Valores ---
    @JsonProperty("preco_medio_fornecedor")
    private BigDecimal averagePriceFromSupplier;

    @JsonProperty("valor_total_item")
    private BigDecimal totalItemValue;

    // --- Pessoas ---
    @JsonProperty("nome_solicitante")
    private String requesterName;

    @JsonProperty("nome_comprador")
    private String buyerName;

   

    // --- Documentação Extra ---
   

    @JsonProperty("nome_contrato")
    private String contractName;

     @JsonProperty("quantidade_itens_total")
    private BigDecimal qtdItensTotal;



    
}