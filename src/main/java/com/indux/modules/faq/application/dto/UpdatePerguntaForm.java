package com.indux.modules.faq.application.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UpdatePerguntaForm {
    private Long setorId;
    private Long temaId;
    private String categoria;
    private String tipo;
    private LocalDateTime dataCriacao;
    private String titulo;
    private Long regionalId;
    private Long contratoId;
    private java.util.List<Long> contratos;
    private String publico;
    private String aprovador;
    private String observacoes;
    private List<AttachmentForm> anexos;
    private List<CreatePerguntaForm.RespostaForm> respostas;
    private Boolean situacao;
}


