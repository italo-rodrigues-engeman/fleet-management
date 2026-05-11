package com.indux.core.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleLoginResponseDTO {
    
    private boolean success;
    private String message;
    private UUID funcionarioId;
    private String nome;
    private String matricula;
    private String cpf;
    private String status;
    private String cargo;
    private String cargoNome;
    private String contrato;
    private String centroCustos;
    private String regional;
    private String regionalOG;
    private String contratoOG;
    private String token; // Token JWT se necessário
    
    public static SimpleLoginResponseDTO success(UUID funcionarioId, String nome, String matricula, String cpf, String status, String cargo, String cargoNome, String contrato, String centroCustos, String regional, String regionalOG, String contratoOG) {
        return SimpleLoginResponseDTO.builder()
                .success(true)
                .message("Login realizado com sucesso")
                .funcionarioId(funcionarioId)
                .nome(nome)
                .matricula(matricula)
                .cpf(cpf)
                .status(status)
                .cargo(cargo)
                .cargoNome(cargoNome)
                .contrato(contrato)
                .centroCustos(centroCustos)
                .regional(regional)
                .regionalOG(regionalOG)
                .contratoOG(contratoOG)
                .build();
    }
    
    public static SimpleLoginResponseDTO failure(String message) {
        return SimpleLoginResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }
}
