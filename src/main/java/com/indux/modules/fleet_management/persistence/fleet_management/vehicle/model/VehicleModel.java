package com.indux.modules.fleet_management.persistence.fleet_management.vehicle.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Como o veículo é salvo no MongoDB

@Getter
@Setter
@Document(collection = "vehicles")
public class VehicleModel {

    @Id
    private String id;

    private String plate;
    private String chassis;
    private String renavam;

    private Integer manufactureYear;
    private Integer modelYear;

    private String category;
    private String detailedDescription;

    private String cpfCnpj;

    private String contract;
    private String project;
    private String regional;

    private String uf;

    private Boolean activeEngeman;
    private Boolean insurance;


    private String status;

    private String managerEmployeeId;
    private String currentDriverId;
}