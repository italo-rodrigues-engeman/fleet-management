package com.indux.modules.calibration.infra.mappers;

import com.indux.modules.calibration.aplication.dtos.*;
import com.indux.modules.calibration.domain.entities.mongo.*;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GeralMapper {
    ManufacturerEntity manufacturerEntity(Manufacturer manufacturer);
    List<Manufacturer> manufacturerPageable(List<ManufacturerEntity> entity);
    
    EquipamentEntity equipmentEntity(Equipment equipment);
    List<Equipment> equipmentPageable(List<EquipamentEntity> entity);
    Equipment equipmentDTO(EquipamentEntity equipment);
    
    MeasuresEntity measuresEntity(Measures measures);
    Measures measuresDTO(MeasuresEntity measures);
    List<Measures> measuresPageable(List<MeasuresEntity> entity);
    
    PropertiesEntity propertiesEntity(Properties properties);
    List<Properties> propertiesPageable(List<PropertiesEntity> entity);
    
    UnitMeasuresEntity unitMeasuresEntity(UnitMeasures unitMeasures);
    UnitMeasures unitMeasuresDTO(UnitMeasuresEntity unitMeasures);
    List<UnitMeasures> unitMeasuresPageable(List<UnitMeasuresEntity> entity);

    NiOrganizationEntity niOrganizationEntity(NiOrganizationCreate niOrganizationCreate);
    List<NiOrganizationReturn>  niOrganizationReturnPageable(List<NiOrganizationEntity> niOrganizationReturn);
    NiOrganizationReturn niOrganizationReturn(NiOrganizationEntity niOrganizationEntity);
}