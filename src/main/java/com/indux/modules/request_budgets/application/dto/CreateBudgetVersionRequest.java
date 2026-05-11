package com.indux.modules.request_budgets.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBudgetVersionRequest {
    // Documentos da oportunidade
    private List<AttachmentEntity> anexoMd;
    private String mdInfo;
    private List<AttachmentEntity> anexoPpu;
    private String ppuInfo;
    private List<AttachmentEntity> anexoSms;
    private String smsInfo;
    private List<AttachmentEntity> anexoGerais;
    private String geraisInfo;
    private List<AttachmentEntity> habilitacaoAnexo;
    private String habilitacaoInfo;

    // Proposta e entrega
    private String tipoProposta;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataEntrega;

    private String metodoEntrega;
    private Boolean propostaLocal;
    private List<String> cidades;
    private List<String> estados;

    // Atividades e serviços
    private String manutencao;
    private String operacao;
    private String atividadesDiversas;
    private String construcaoMontagem;
    private String fabricacao;
    private String projetos;
    private String diversos;
    private String detalhes;
    private String outros;

    // Outros campos
    private BigDecimal comissao;
    private String modalidadeConcorrencia;
    private String tipoOportunidade;
    private String caracteristicasOportunidade;
    private String outroEmail;
    private String portal;
    private String acessoInfo;
}



