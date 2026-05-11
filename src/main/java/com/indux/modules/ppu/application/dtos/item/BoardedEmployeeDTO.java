package com.indux.modules.ppu.application.dtos.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para representação de funcionários embarcados na API.
 * <p>
 * Este DTO é utilizado no módulo PPU para expor os
 * dados de funcionários embarcados via API REST. Os campos são expostos em português
 * para manter a consistência com a interface do usuário.
 * <p>
 * Este DTO é utilizado principalmente pelo
 * {@link com.indux.modules.ppu.presentation.EmployeeBoardingController}
 * para retornar os dados dos funcionários embarcados.
 * @see BoardedEmployee
 * @see com.indux.modules.ppu.presentation.EmployeeBoardingController
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardedEmployeeDTO {
    @JsonProperty("matricula")
    private String registration;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("funcao")
    private String rolePosition;
    @JsonProperty("plataforma")
    private String platform;
}