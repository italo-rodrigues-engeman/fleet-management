package com.indux.modules.crm.domain.entity;


import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Unit {

    private String id;

    private String type;

    private String name;

    private String cep;

    private String state;

    private String address;

    private String city;

    private String googleMapsLink;

    private String number;

    private String observations;

    private String responsibleUser;

    private Date registrationDate;

    private Boolean status;

    //interacao_comercial
    private List<CommercialInteractions> commercialInteractions;

    private String company;

    public Unit createGeneric () {
        Unit unit = new Unit();

        unit.setName("Matriz");
        unit.setType("MATRIZ");
        unit.setCep("00000000");
        unit.setState("PE");
        unit.setAddress("Genérico");
        unit.setCity("Genérica");
        unit.setGoogleMapsLink("");
        unit.setNumber("00000000000");
        unit.setObservations("");
        unit.setStatus(true);

        return unit;
    }
}
