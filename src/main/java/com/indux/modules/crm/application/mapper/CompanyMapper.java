package com.indux.modules.crm.application.mapper;

import com.indux.modules.crm.application.dto.request.CompanyRequest;
import com.indux.modules.crm.application.dto.response.CompanyResponse;
import com.indux.modules.crm.application.dto.response.CompanySummary;
import com.indux.modules.crm.domain.entity.Company;
import com.indux.modules.crm.persistence.model.CompanyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    @Mapping(source = "responsibleUser", target = "responsibleUser")
    CompanySummary companyEntityToCompanySummaryDTO(Company company);

    Company fromRequest(CompanyRequest request);

    CompanyModel fromEntity(Company entity);

    Company fromModel(CompanyModel model);

    CompanyResponse toResponse(Company entity);
}
