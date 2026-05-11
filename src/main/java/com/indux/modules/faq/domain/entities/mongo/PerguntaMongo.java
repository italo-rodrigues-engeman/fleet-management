package com.indux.modules.faq.domain.entities.mongo;

import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "faq_perguntas")
@Data
public class PerguntaMongo {
    @Id
    private String id;
    private Long codigoSequencial; // vindo da sequência Postgres
    private Long setorId;
    private Long temaId;
    private String categoria;
    private String tipo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFim;
    private String titulo;
    private Long regionalId;
    private Long contratoId;
    private List<Long> contratos; // múltiplos contratos
    private String publico;
    private String aprovador;
    private String observacoes;
    private List<AttachmentEntity> anexos;
    private List<RespostaEmbedded> respostas;
    private String status; // PENDENTE, APROVADO, REJEITADO, etc.
    private Integer currentStep; // etapa atual (0,1,2)
    private List<StepLogEmbedded> stepLog;
    private Boolean situacao; // campo para controle de ativação/desativação da pergunta
    // novos campos solicitados
    private Long diretoriaId;
    private Long superintendenciaId;
    private Long projetoId;
    // listas (suporte a múltiplos)
    private List<Long> diretoriaIds;
    private List<Long> superintendenciaIds;
    private List<Long> regionalIds;
    private List<Long> setorOrganizationIds;
    private List<Long> projetoIds;
    private List<Long> filialHcmId; // Changed to List<Long>
    private Long setorOrganizationId;
    // Regras de acesso por filial/organograma
    private Boolean restrito; // true => valor contém filialHcmId; false => valor contém id do organograma
    private java.util.List<Long> valor;
    private String tipoValor; // DIRETORIA, SUPERINTENDENCIA, REGIONAL, SETOR, CONTRATO, PROJETO, FILIAL_HCM

    @Transient
    private String temaNome;

    @Data
    public static class RespostaEmbedded {
        private String conteudo;
        private Long regional;
        private Long contrato;
        private List<Long> contratos; // múltiplos contratos
        private String publico;
        private List<AttachmentEntity> anexo;
        // novos campos para resposta
        private Long diretoriaId;
        private Long superintendenciaId;
        private Long projetoId;
        // listas (suporte a múltiplos)
        private List<Long> diretoriaIds;
        private List<Long> superintendenciaIds;
        private List<Long> regionalIds;
        private List<Long> setorOrganizationIds;
        private List<Long> projetoIds;
        private List<Long> filialHcmId; // Changed to List<Long>
        private Long setorOrganizationId;
        private Boolean restrito; // true => valor contém filialHcmId; false => valor contém id do organograma
        private java.util.List<Long> valor;
        private String tipoValor; // idem ao da pergunta
    }

    @Data
    public static class StepLogEmbedded {
        private String id;
        private String name;
        private Integer step;
        private Date created_at;
        private String user; // opcional
        private String observation; // opcional
        private Integer stepCounter; // Contador sequencial global para todos os step logs
    }
}