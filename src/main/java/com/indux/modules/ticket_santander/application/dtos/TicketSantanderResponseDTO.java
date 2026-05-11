package com.indux.modules.ticket_santander.application.dtos;

import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSantanderResponseDTO {
    
    private String id;
    private UUID funcionarioId;
    private String nomeFuncionario;
    private String cpf;
    private String matricula;
    private String email;
    private String telefone;
    private String tipoTicket;
    private String descricao;
    private String status;
    private String prioridade;
    private String categoria;
    private String observacoes;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private String numeroAgencia;
    private String numeroConta;
    private String contrato;
    private String regional;
    private String cargo;
    private String cargoNome;
    private Integer filialHcm;
    private List<AttachmentEntity> carteirinhaAnexos;
}
