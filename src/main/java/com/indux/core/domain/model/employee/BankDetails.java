package com.indux.core.domain.model.employee;

import com.indux.core.application.dto.generic.BankDetailDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_dadosbancarios")
public class BankDetails {
    @Id
    private String matricula;
    private String numero_banco;
    private String agencia;
    private String conta_bancaria;
    private int tipo_conta;
    private String nome_banco;

    public String tipoContaString() {
        return switch (tipo_conta) {
            case 1 -> "Conta Corrente";
            case 2 -> "Conta Poupança";
            case 3 -> "Conta Salário";
            default -> "Outros";
        };
    }

    public static int tipoContaCodigo(String tipoConta) {
        return switch (tipoConta) {
            case "Conta Corrente" -> 1;
            case "Conta Poupança" -> 2;
            case "Conta Salário" -> 3;
            default -> 0;
        };
    }

    public BankDetailDTO toDTO() {
        return BankDetailDTO.builder()
                .agencia(this.agencia)
                .conta_bancaria(this.conta_bancaria)
                .numero_banco(this.numero_banco)
                .tipo_conta(this.tipoContaString())
                .build();
    }
}
