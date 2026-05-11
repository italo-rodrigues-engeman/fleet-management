package com.indux.modules.faq.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class CreatePerguntaForm {
    private Long setorId;
    private Long temaId;
    private String categoria;
    private String tipo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFim;
    private String titulo;
    private Long regionalId;
    private Long contratoId;
    private List<Long> contratos;
    private String publico;
    private String aprovador;
    private String observacoes;
    private List<AttachmentForm> anexos;
    private List<RespostaForm> respostas;
    private Boolean situacao;
    // novos campos solicitados
    private Long diretoriaId;
    private Long superintendenciaId;
    private Long projetoId;
    // suporte a múltiplos
    private List<Long> diretoriaIds;
    private List<Long> superintendenciaIds;
    private List<Long> regionalIds;
    private List<Long> setorOrganizationIds;
    private List<Long> projetoIds;
    private List<Long> filialHcmId;  // Changed to List<Long>
    private Long setorOrganizationId;
    
    @Getter
    @Setter
    public static class RespostaForm {
        private String conteudo;
        private Long regional;
        private Long contrato;
        private List<Long> contratos;
        private String publico;
        private List<AttachmentForm> anexo;
        // novos campos para resposta
        private Long diretoriaId;
        private Long superintendenciaId;
        private Long projetoId;
        // suporte a múltiplos
        private List<Long> diretoriaIds;
        private List<Long> superintendenciaIds;
        private List<Long> regionalIds;
        private List<Long> setorOrganizationIds;
        private List<Long> projetoIds;
        private List<Long> filialHcmId;  // Changed to List<Long>
        private Long setorOrganizationId;
    }
}