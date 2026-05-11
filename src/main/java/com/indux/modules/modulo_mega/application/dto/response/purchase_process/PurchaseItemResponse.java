package com.indux.modules.modulo_mega.application.dto.response.purchase_process;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PurchaseItemResponse {

    @JsonProperty("id_item")
    private Integer idItem;

    @JsonProperty("descricao")
    private String description;

    @JsonProperty("unidade_medida")
    private String unitMeasurement;

    @JsonProperty("codigo_grupo")
    private Integer groupCode;

    @JsonProperty("nome_grupo")
    private String groupName;

    @JsonProperty("tipo")
    private String type;

    @JsonProperty("valor")
    private BigDecimal value;

    @JsonProperty("fornecedor")
    private SupplierResponse supplier;

    @JsonProperty("tipo_pedido")
    private String orderType;

    @JsonProperty("status_pedido")
    private String orderStatus;

    @JsonProperty("tipo_compra")
    private String typePurchase;

    @JsonProperty("categoria")
    private String category;

    @JsonProperty("data_aprovacao_pedido")
    private Instant orderApprovalDate;

    @JsonProperty("notas_fiscais")
    private List<InvoiceQuantityResponse> invoices;

    @JsonProperty("pedido")
    private OrderItemResponse order;

    @JsonProperty("nome_projeto")
    private String projectName;

    @JsonProperty("codigo_filial")
    private Integer branchCode;

    @JsonProperty("nome_filial")
    private String branchName;

    @JsonProperty("id_regional")
    private Integer regionalId;

    @JsonProperty("nome_regional")
    private String regionalName;

    @JsonProperty("id_contrato")
    private Integer contractId;

    @JsonProperty("nome_contrato")
    private String contractName;
}
