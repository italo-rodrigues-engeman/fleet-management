package com.indux.modules.crm.application.mapper;

import com.indux.modules.crm.application.dto.request.EngemanAgentRequest;
import com.indux.modules.crm.application.dto.response.EngemanAgentResponse;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EngemanAgentMapper {

    EngemanAgent fromModel(EngemanAgentModel model);

    EngemanAgentModel fromEntity(EngemanAgent entity);

    @Mapping(source = "nome", target = "name")
    @Mapping(source = "endereco", target = "address")
    @Mapping(source = "estado", target = "state")
    @Mapping(source = "cidade", target = "city")
    @Mapping(source = "email_principal", target = "mainEmail")
    @Mapping(source = "email_alternativo", target = "alternativeEmail")
    @Mapping(source = "telefone_principal", target = "mainNumber")
    @Mapping(source = "telefone_alternativo", target = "alternativeNumber")
    @Mapping(source = "comissoes", target = "commissions")
    EngemanAgent fromRequest(EngemanAgentRequest request);

    EngemanAgentResponse toResponse(EngemanAgent entity);

    EngemanAgentResponse modelToResponse(EngemanAgentModel model);
}
