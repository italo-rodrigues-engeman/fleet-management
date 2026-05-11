package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.enums.UnitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tipo")
    private String type;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("cep")
    private String cep;

    @JsonProperty("endereco")
    private String address;

    @JsonProperty("cidade")
    private String city;

    @JsonProperty("estado")
    private String state;

    @JsonProperty("link_do_googlemaps")
    private String googleMapsLink;

    @JsonProperty("telefone")
    private String number;

    @JsonProperty("observacoes")
    private String observations;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("responsavel_cadastro")
    private String responsibleUser;

    @JsonProperty("data_cadastro")
    private LocalDate registrationDate;

    @JsonProperty("empresa")
    private String company;
}
