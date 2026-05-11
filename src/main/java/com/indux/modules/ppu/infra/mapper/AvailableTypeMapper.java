package com.indux.modules.ppu.infra.mapper;

import com.indux.modules.ppu.application.dtos.requests.AvailableTypeRequest;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AvailableTypeMapper {

    AvailableType fromRequest(AvailableTypeRequest request);

    AvailableTypeRequest toRequest(AvailableType availableType);

    List<AvailableType> fromListRequest(List<AvailableTypeRequest> requests);
}
