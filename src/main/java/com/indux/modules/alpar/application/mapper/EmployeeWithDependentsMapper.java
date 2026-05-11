package com.indux.modules.alpar.application.mapper;

import com.indux.modules.alpar.application.dto.response.EmployeeWithDependentsResponse;
import com.indux.modules.alpar.persistence.model.EmployeeWithDependentsView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeWithDependentsMapper {

    @Mapping(target = "dependents", ignore = true)
    @Mapping(target = "sex", source = "sex", qualifiedByName = "mapSex")
    @Mapping(target = "motherName", source = "motherName", qualifiedByName = "mapMotherName")
    EmployeeWithDependentsResponse toResponse(EmployeeWithDependentsView view);

    @Named("mapSex")
    default String mapSex(String sex) {
        if ("F".equalsIgnoreCase(sex))
            return "Feminino";
        if ("M".equalsIgnoreCase(sex))
            return "Masculino";
        return "Não informado";
    }

    @Named("mapMotherName")
    default String mapMotherName(String motherName) {
        return motherName != null ? motherName : "Não informado";
    }
}
