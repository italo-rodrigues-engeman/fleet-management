package com.indux.modules.request_budgets.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.indux.core.domain.model.modules.AttachmentEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "budget_versions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetVersion {
    @Id
    private String id;

    @Indexed
    private String budgetId;

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
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraProposta;
    
    private BigDecimal valorFinalTotal;
    private String ResponsavelProposta;
    private String anexoTipo;
    private String comporvanteProposta;

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

    // Comercial
    private List<BudgetComercial> comercial;

    // Engenharia
    private List<BudgetEngenharia> engenharia;

    // SMS
    private List<BudgetSms> sms;

    // Operações
    private List<BudgetOperacao> operacoes;

    // Filtro 1
    private String filtro1;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datahoraFiltro1;
    
    private String responsavelFiltro1;
    private List<AttachmentEntity> anexoFiltro1;
    private String motivoFiltro1;
    private String responsavelFilrtro1;
    
    // Outros campos adicionais
    private String numeroAc;
    private String oracamentista;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataDesignacao;
    
    private String justificativaEngeman;
    private String justificativaSolicitante;

    // Filtro 2
    private String filtro2;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datahoraFiltro2;
    
    private String responsavelFiltro2;
    private String justificativaFiltro2;
    private String motivoFiltro2;
    private List<AttachmentEntity> anexoFiltro2;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFiltro2;
    
    private String justificativaSolicitanteFiltro2;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID updatedBy;
}



