package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.application.dto.groups.MegaGroupResponse;
import com.indux.modules.modulo_mega.domain.entities.jpa.MegaGroupCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MegaGroupMapper {
    @Mapping(target = "statusGroup", source = "status")
    MegaGroupResponse toResponse(MegaGroupCode entity);
    List<MegaGroupResponse> toResponse(List<MegaGroupCode> entity);


}
