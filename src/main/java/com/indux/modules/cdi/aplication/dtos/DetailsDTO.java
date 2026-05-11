package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record DetailsDTO(
        @JsonProperty("pessoa") @Nullable String person,
        @JsonProperty("equipamento") @Nullable String equipament,
        @JsonProperty("materiais") @Nullable String materials,
        @JsonProperty("tecnologia")  @Nullable String technology,
        @JsonProperty("infraestrutura") @Nullable String infrastructure,
        @JsonProperty("servicos") @Nullable String services,
        @JsonProperty("outros") @Nullable String outhers
) {

}
