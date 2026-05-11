package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.enums.UnitType;

public record UnitRequest(
        @JsonProperty("tipo") String type,
        @JsonProperty("nome") String name,
        @JsonProperty("cep") String cep,
        @JsonProperty("endereco") String address,
        @JsonProperty("cidade") String city,
        @JsonProperty("estado") String state,
        @JsonProperty("link_do_googlemaps") String googleMapsLink,
        @JsonProperty("telefone") String number,
        @JsonProperty("observacoes") String observations,
        @JsonProperty("status") Boolean status
) {
}
