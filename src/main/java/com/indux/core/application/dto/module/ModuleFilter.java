package com.indux.core.application.dto.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleFilter {
    @JsonProperty("etapa")
    Integer step;
    @JsonProperty("regional")
    Integer regional;
    @JsonProperty("contrato")
    Integer contract;
    @JsonProperty("projeto")
    Integer project;
    @JsonProperty("novaRegional")
    Integer newRegional;
}
