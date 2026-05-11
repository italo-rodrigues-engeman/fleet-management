package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.crm.domain.entity.Alert;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public record CommercialInteractionsRequest(
        @JsonProperty("tipo_de_contato") String tipo_de_contato,
        @JsonProperty("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @JsonProperty("descricao") String descricao,
        @JsonProperty("atencao") String atencao,
        @JsonProperty("janela_de_oportunidade") String janela_de_oportunidade,
        @JsonProperty("contato") String contato,
        @JsonProperty("cliente") String cliente,
        @JsonProperty("unidade") String unidade,
        @JsonProperty("representantes_engeman") List<String> representantes_engeman,
        @JsonProperty("status") String status,
        @JsonProperty("alertas") List<AlertRequest> alertas
) {}
