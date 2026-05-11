package com.indux.modules.ticket_santander.domain.entities.mongo;

import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ticket_santander_collection")
public class TicketSantanderMongoEntity {
    
    @Id
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
    private List<AttachmentEntity> carteirinhaAnexos;
}
