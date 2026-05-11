package com.indux.modules.ocf.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ticket_alodp", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAlodp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long protocolo;
    
    @Column(name = "data_hora_inicial")
    private LocalDateTime dataHoraInicial;
    
    @Column(name = "data_final_funil")
    private LocalDateTime dataFinalFunil;
    
    @Column(name = "data_final_atendimento")
    private LocalDateTime dataFinalAtendimento;
    
    @Column(name = "colaborador_nome")
    private String colaboradorNome;
    
    @Column(name = "colaborador_email")
    private String colaboradorEmail;
    
    @Column(name = "id_regional")
    private Integer idRegional;
    
    @Column(name = "id_contrato")
    private Integer idContrato;
    
    @Column(name = "etapa")
    private String etapa;
    
    @Column(name = "avaliacao")
    private Integer avaliacao;
    
    @Column(name = "id_tipo_atendimento")
    private Integer idTipoAtendimento;
    
    @Column(name = "id_assunto")
    private Integer idAssunto;
    
    @Column(name = "id_atendente")
    private Integer idAtendente;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "prioridade")
    private String prioridade;
    
    @Column(name = "origem")
    private String origem;
    
    @Column(name = "id_tema")
    private Integer idTema;
    
    @Column(name = "id_pergunta")
    private Integer idPergunta;
    
    @Column(name = "nota_atendimento")
    private String notaAtendimento;
    
    @Column(name = "matricula")
    private String matricula;
    
    @Column(name = "id_ocorrencia")
    private Long idOcorrencia;
    
    @Column(name = "status_chatwoot")
    private Integer statusChatwoot;
} 