package com.indux.core.application.dto.generic;

import lombok.Builder;

@Builder
public record BankDetailDTO(
        String numero_banco,
        String agencia,
        String conta_bancaria,
        String tipo_conta,
        String nome_banco
) {
}
