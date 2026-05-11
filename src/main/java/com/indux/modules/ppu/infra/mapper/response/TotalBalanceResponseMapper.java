package com.indux.modules.ppu.infra.mapper.response;

import com.indux.modules.ppu.application.dtos.response.TotalBalanceResponse;
import com.indux.modules.ppu.domain.entities.ppu.TotalBalance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TotalBalanceResponseMapper {
    TotalBalanceResponse toResponse(TotalBalance entity);
}

