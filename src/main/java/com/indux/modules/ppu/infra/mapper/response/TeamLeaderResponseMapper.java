package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.TeamLeaderResponse;
import com.indux.modules.ppu.domain.entities.item.TeamLeader;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamLeaderResponseMapper {
    TeamLeaderResponse toResponse(TeamLeader entity);
}

