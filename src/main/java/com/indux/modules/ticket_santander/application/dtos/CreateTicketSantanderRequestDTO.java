package com.indux.modules.ticket_santander.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketSantanderRequestDTO {
    
    @NotNull(message = "ID do funcionário é obrigatório")
    private UUID funcionarioId;
    
    @NotBlank(message = "Nome do funcionário é obrigatório")
    private String nomeFuncionario;
    
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;
    
    private String matricula;
    
    private String email;
    
    private String telefone;
    
    @NotBlank(message = "Tipo do ticket é obrigatório")
    private String tipoTicket;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    private String prioridade;
    
    private String categoria;
    
    private String observacoes;
    
    @NotBlank(message = "Número da agência é obrigatório")
    private String numeroAgencia;
    
    @NotBlank(message = "Número da conta é obrigatório")
    private String numeroConta;
    
    @NotNull(message = "Foto da carteirinha é obrigatória")
    private List<MultipartFile> carteirinhaAnexos;
}
