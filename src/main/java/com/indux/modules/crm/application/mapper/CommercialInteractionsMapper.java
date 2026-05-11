package com.indux.modules.crm.application.mapper;

import com.indux.modules.crm.application.dto.request.CommercialInteractionsRequest;
import com.indux.modules.crm.application.dto.response.CommercialInteractionsResponse;
import com.indux.modules.crm.domain.entity.CommercialInteractions;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.persistence.model.CommercialInteractionsModel;
import java.util.stream.Collectors;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EngemanAgentMapper.class})
public interface CommercialInteractionsMapper {

    @Mapping(source = "company", target = "company")
    @Mapping(source = "unit", target = "unit")
    @Mapping(source = "lead", target = "lead")
    @Mapping(source = "alertModel", target = "alerts")
    CommercialInteractions toEntity(CommercialInteractionsModel model);

    @Mapping(source = "company", target = "company")
    @Mapping(source = "unit", target = "unit")
    @Mapping(source = "lead", target = "lead")
    @Mapping(source = "alerts", target = "alertModel")
    CommercialInteractionsModel toModel(CommercialInteractions entity);

    @Mapping(source = "company", target = "client")
    @Mapping(source = "engemanAgent", target = "engemanAgent")
    CommercialInteractionsResponse toResponse(CommercialInteractions entity);

    @Mapping(source = "data", target = "date")
    @Mapping(source = "descricao", target = "description")
    @Mapping(source = "atencao", target = "attention")
    @Mapping(source = "janela_de_oportunidade", target = "windowOfOpportunity")
    @Mapping(source = "unidade", target = "unit")
    @Mapping(source = "tipo_de_contato", target = "contactType")
    @Mapping(source = "cliente", target = "company")
    @Mapping(source = "contato", target = "lead")
    @Mapping(source = "alertas", target = "alerts")
    CommercialInteractions fromRequest(CommercialInteractionsRequest request);

    @Mapping(source = "engemanAgent", target = "engemanAgent.id")
    com.indux.modules.crm.domain.entity.Alert toAlert(com.indux.modules.crm.application.dto.request.AlertRequest request);


    @Mapping(source = "company", target = "client")
    @Mapping(source = "engemanAgent", target = "engemanAgent")
    @Mapping(source = "alertModel", target = "alerts")
    CommercialInteractionsResponse modelToResponse(CommercialInteractionsModel model);
}
