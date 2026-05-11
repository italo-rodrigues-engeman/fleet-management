package com.indux.modules.crm.application.mapper;

import com.indux.modules.crm.application.dto.request.LeadRequest;
import com.indux.modules.crm.application.dto.response.LeadResponse;
import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.persistence.model.LeadModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeadMapper {

    Lead fromModel(LeadModel model);

    LeadModel fromEntity(Lead entity);

    Lead fromRequest(LeadRequest request);

    LeadResponse toResponse(Lead entity);

    LeadResponse toResponseFromModel(LeadModel model);

    LeadRequest toRequest(Lead entity);
}
