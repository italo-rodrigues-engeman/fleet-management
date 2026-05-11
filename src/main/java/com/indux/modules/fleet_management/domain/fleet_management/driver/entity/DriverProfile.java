package com.indux.modules.fleet_management.domain.fleet_management.driver.entity;

import com.indux.modules.fleet_management.domain.fleet_management.driver.enums.CnhCategory;
import com.indux.modules.fleet_management.domain.fleet_management.driver.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor


public class DriverProfile {

    private String id;
    private String employeeId;
    private String employeeNumber;
    private String cnh;
    private CnhCategory cnhCategory;
    private LocalDate cnhExpirationDate;
    private DriverStatus status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void activate() {
        this.status = DriverStatus.ACTIVE;
        this.active = true;
    }
    public void block() {
        this.status = DriverStatus.BLOCKED;
    }
    public void suspend() {
        this.status = DriverStatus.SUSPENDED;
    }
    public void inactivate() {
        this.status = DriverStatus.INACTIVE;
        this.active = false;
    }


}
