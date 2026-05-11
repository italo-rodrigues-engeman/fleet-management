package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdersGroupedDTO(
    @JsonProperty("numero_ap") String approve,
    @JsonProperty("numero_nf") String noteNumber,
    @JsonProperty("data_nota") LocalDate invoiceDate,
    @JsonProperty("id") String id,
    @JsonProperty("codigo_item") Integer idItem,
    @JsonProperty("descricao_item") String itemName,
    @JsonProperty("numero_pedido") Integer orderNumber,
    @JsonProperty("tipo_pedido") String orderType,
    @JsonProperty("codigo_fornecedor") Integer supplierCode,
    @JsonProperty("nome_fornecedor") String supplierName,
    @JsonProperty("codigo_grupo") Integer groupCode,
    @JsonProperty("nome_grupo") String groupName,
    @JsonProperty("codigo_filial") Integer branchId,
    @JsonProperty("nome_filial") String branchName,
    @JsonProperty("codigo_projeto") Integer projectCode,
    @JsonProperty("nome_projeto") String projectName,
    @JsonProperty("codigo_regional") Integer regionalCode,
    @JsonProperty("nome_regional") String regionalName,
    @JsonProperty("nome_diretoria") String directoryName,
    @JsonProperty("nome_superintendencia") String superName,
    @JsonProperty("data_criacao") LocalDate creationDate,
    @JsonProperty("usuario_cadastro") String registerUser,
    @JsonProperty("usuario_edicao") String registerUserEdition,
    @JsonProperty("data_solicitacao") LocalDate solDate,
    @JsonProperty("data_pedido") LocalDate orderDate,
    
    @JsonProperty("data_entrega") LocalDate deliveryDate,
    @JsonProperty("status_do_pedido") String orderStatus,
    @JsonProperty("status_do_item") String itemStatus,
    @JsonProperty("preco_medio_fornecedor") BigDecimal averagePriceFromSupplier,
    @JsonProperty("valor_total") BigDecimal totalItemValue,
    @JsonProperty("nome_solicitante") String requesterName,
    @JsonProperty("nome_comprador") String buyerName,
    @JsonProperty("nome_contrato") String contractName,
    @JsonProperty("quantidade_itens_total") BigDecimal qtdItensTotal,
    @JsonProperty("tipo_item") String itemType,
    @JsonProperty("unidade_medida") String unitOfMeasure,
    @JsonProperty("solicitacao") Integer solicitation
   
) {
    public OrdersGroupedDTO(
            String approve, String noteNumber,LocalDate invoiceDate, String id, Number idItem, String itemName,
            Integer orderNumber, String orderType, Number supplierCode, String supplierName,
            Number groupCode, String groupName, Number branchId, String branchName,
            Number projectCode, String projectName, Number regionalCode, String regionalName,
            String directoryName, String superName, LocalDate creationDate, String registerUser,
            String registerUserEdition, LocalDate solDate, LocalDate orderDate, LocalDate deliveryDate,
            String orderStatus, String itemStatus, Number averagePriceFromSupplier,
            Number totalItemValue, String requesterName, String buyerName, String contractName,
            Number qtdItensTotal, String itemType, String unitOfMeasure, Number solicitation
    ) {
        this(
            approve, noteNumber,invoiceDate, id, toInteger(idItem), itemName, orderNumber, orderType,
            toInteger(supplierCode), supplierName, toInteger(groupCode), groupName,
            toInteger(branchId), branchName, toInteger(projectCode), projectName,
            toInteger(regionalCode), regionalName, directoryName, superName, creationDate,
            registerUser, registerUserEdition, solDate, orderDate, deliveryDate, orderStatus,
            itemStatus, toBigDecimal(averagePriceFromSupplier), toBigDecimal(totalItemValue),
            requesterName, buyerName, contractName, toBigDecimal(qtdItensTotal), itemType,
            unitOfMeasure, toInteger(solicitation)
        );
    }

    private static Integer toInteger(Number number) {
        return (number != null) ? number.intValue() : null;
    }

    private static BigDecimal toBigDecimal(Number number) {
        return (number != null) ? new BigDecimal(number.toString()) : BigDecimal.ZERO;
    }
}