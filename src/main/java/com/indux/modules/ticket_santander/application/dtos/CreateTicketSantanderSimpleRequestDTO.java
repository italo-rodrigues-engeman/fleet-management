package com.indux.modules.ticket_santander.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketSantanderSimpleRequestDTO {
    
    @NotBlank(message = "Número da agência é obrigatório")
    private String numeroAgencia;
    
    @NotBlank(message = "Número da conta é obrigatório")
    private String numeroConta;
    
    @NotNull(message = "Foto da carteirinha é obrigatória")
    private List<MultipartFile> carteirinhaAnexos;
}
