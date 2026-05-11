package com.indux.modules.ticket_santander.domain.entities.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_ticket_santander")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketSantanderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;
    
    @Column(name = "nome_funcionario", nullable = false, length = 255)
    private String nomeFuncionario;
    
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;
    
    @Column(name = "matricula", length = 50)
    private String matricula;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "telefone", length = 20)
    private String telefone;
    
    @Column(name = "tipo_ticket", nullable = false, length = 100)
    private String tipoTicket;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "prioridade", length = 20)
    private String prioridade;
    
    @Column(name = "categoria", length = 100)
    private String categoria;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    @Column(name = "numero_ticket", unique = true, length = 50)
    private String numeroTicket;
    
    @Column(name = "numero_agencia", length = 20)
    private String numeroAgencia;
    
    @Column(name = "numero_conta", length = 20)
    private String numeroConta;
    
    @ElementCollection
    @CollectionTable(name = "tb_ticket_santander_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "anexo_id")
    private List<String> carteirinhaAnexoIds;
    
    @ElementCollection
    @CollectionTable(name = "tb_ticket_santander_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "anexo_nome")
    private List<String> carteirinhaAnexoNomes;
    
    @ElementCollection
    @CollectionTable(name = "tb_ticket_santander_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "file_path")
    private List<String> carteirinhaAnexoPaths;
    
    @ElementCollection
    @CollectionTable(name = "tb_ticket_santander_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "file_extension")
    private List<String> carteirinhaAnexoExtensions;
    
    @ElementCollection
    @CollectionTable(name = "tb_ticket_santander_anexos", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "file_mime_type")
    private List<String> carteirinhaAnexoMimeTypes;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null) {
            status = "ABERTO";
        }
        if (numeroTicket == null) {
            numeroTicket = generateTicketNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    private String generateTicketNumber() {
        return "TS-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}
