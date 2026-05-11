package com.indux.modules.calibration.infra.mappers;

import com.indux.modules.calibration.aplication.dtos.CalibrationAll;
import com.indux.modules.calibration.aplication.dtos.CalibrationCreate;
import com.indux.modules.calibration.aplication.dtos.Equipment;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CalibrationMapper {
    CalibrationEntity toEntity(CalibrationCreate dto);
    
    OrganizationDTO toOrganizationDTO(OrganizationEntity entity);
    
    ContractDTO toContractDTO(ContractEntity entity);
    
    ProjectDTO toProjectDTO(ProjectEntity entity);

    @Mapping(target = "id", source = "entity.id")
    CalibrationAll toCalibrationAll(NiOrganizationEntity entity,Equipment equipment, List<OrganizationDTO> branch, List<ContractDTO> contract, List<ProjectDTO> project, Boolean situation, LocalDate calibrationDate, String calibrationStatus);
}