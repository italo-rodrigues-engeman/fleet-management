package com.indux.modules.cdi.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PointsDTO (
        String id,
        @JsonProperty("pontosAlto") Integer highPoint,
        @JsonProperty("pontosMedio") Integer medioPoint,
        @JsonProperty("pontosBaixo") Integer lowPoint
){


}
