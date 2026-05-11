package com.indux.modules.crm.application.mapper;

import com.indux.modules.crm.application.dto.request.UnitRequest;
import com.indux.modules.crm.application.dto.response.UnitResponse;
import com.indux.modules.crm.domain.entity.Unit;
import com.indux.modules.crm.persistence.model.UnitModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UnitMapper {

    UnitModel fromEntity(Unit entity);

    Unit fromModel(UnitModel model);

    Unit fromRequest(UnitRequest request);

    UnitResponse toResponse (Unit entity);

    UnitResponse fromModelToResponse (UnitModel model);

    UnitRequest toRequest (Unit entity);
}
