package com.indux.modules.ppu.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipmentEntityResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("numeroPatrimonio")
    private String assetNumber;

    @JsonProperty("fabricante")
    private String manufacturer;

    @JsonProperty("modelo")
    private String model;

    @JsonProperty("plataforma")
    private String platform;
}

