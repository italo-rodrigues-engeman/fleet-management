package com.indux.modules.flash_fuel.domain.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record ApplicantRequest(
                 String matricula,
                 @NotNull(message = "Item obrigatório.") String nome,
                 @NotNull(message = "Item obrigatório.") String cpf,
                 @NotNull(message = "Item obrigatório.") String regional,
                 @NotNull(message = "Item obrigatório.") Integer regionalId,
                 @NotNull(message = "Item obrigatório.") Boolean colaborador,
                 @NotNull(message = "Item obrigatório.") Integer filialID,
                 String tetoValor
) {
}
