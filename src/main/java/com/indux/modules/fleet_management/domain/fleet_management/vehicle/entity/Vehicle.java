package com.indux.modules.fleet_management.domain.fleet_management.vehicle.entity;

import com.indux.modules.fleet_management.domain.fleet_management.vehicle.enums.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

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

    private VehicleStatus status;

    private String managerEmployeeId;
    private String currentDriverId;

    public void inactivate(){
        this.status = VehicleStatus.INACTIVE;
    }

    public void activate(){
        this.status = VehicleStatus.ACTIVE;
    }
}