package com.indux.modules.modulo_mega.domain.entities.mongo;

import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "item_solicitacao_collection")
public class ItemSolicitationEntity {
    
    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("sequentialId")
    private Long sequentialId;
    
    @JsonProperty("nomePrincipal")
    private String mainName;

    @JsonProperty("urlReferencia")
    private String urlReferency;

    @JsonProperty("descricao")
    private String description;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("unidadeMedida")
    private String unidadeMedida;

    @JsonProperty("regional")
    private String regional;
    
    // Campos de cadastro do item
    @JsonProperty("codigoItem")
    private Integer itemCode;

    @JsonProperty("grupoItem")
    private Integer itemGroup;

    @JsonProperty("nomeGrupo")
    private String itemGroupName;

    @JsonProperty("nomeItem")
    private String itemName;

    @JsonProperty("observacaoCadastro")
    private String observationRegistration;
    
    // Campos de aprovação tributária
    @JsonProperty("aplicacao")
    private String aplicacao;
    
    @JsonProperty("servico")
    private String servico;
    
    @JsonProperty("ncm")
    private String ncm;
    
    @JsonProperty("definicaoFiscal")
    private String definicaoFiscal;
    
    @JsonProperty("codigoSituacaoTributaria")
    private String codigoSituacaoTributaria;
    
    @JsonProperty("codigoTratamentoIcms")
    private String codigoTratamentoIcms;
    
    @JsonProperty("codigoRegraPisCofins")
    private String codigoRegraPisCofins;
    
    // Campo de rejeição
    @JsonProperty("justificativaRejeicao")
    private String rejectionMessage;
    
    @Builder.Default
    @JsonProperty("status")
    private ItemSolicitationStatus status = ItemSolicitationStatus.TECHNICAL_VALIDATION;
    
    @Builder.Default
    @JsonProperty("etapa")
    private Integer etapa = 1;
    
    @CreatedBy
    @JsonProperty("createdBy")
    private String createdBy;
    
    @CreatedDate
    @JsonProperty("dataCriacao")
    private LocalDateTime creationDate;
    
    @LastModifiedDate
    @JsonProperty("dataAtualizacao")
    private LocalDateTime atualizationDate;
    
    @Builder.Default
    @JsonProperty("stepLog")
    private List<StepLog> stepLog = new ArrayList<>();
}

