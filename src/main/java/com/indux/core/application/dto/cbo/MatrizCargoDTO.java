package com.indux.core.application.dto.cbo;

import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatrizCargoDTO {
    private Integer filialId;
    private List<String> cargosId;
}
