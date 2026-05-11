package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientEmployee {
    @JsonProperty("nome")
    private String name;
    @JsonProperty("funcao")
    private String role;
    @JsonProperty("matricula")
    private String registration;
}