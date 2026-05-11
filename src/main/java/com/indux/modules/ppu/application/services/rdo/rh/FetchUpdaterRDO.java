package com.indux.modules.ppu.application.services.rdo.rh;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.application.services.rdo.operation.FetchRDOUseCase;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.PPUResponseMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FetchUpdaterRDO {
    private final FetchRDOUseCase fetch;
    private final PPURepository ppuRepository;
    private final PPUResponseMapper ppuResponseMapper;

    public FetchUpdaterRDO(FetchRDOUseCase fetch, PPURepository ppuRepository, PPUResponseMapper ppuResponseMapper) {
        this.fetch = fetch;
        this.ppuRepository = ppuRepository;
        this.ppuResponseMapper = ppuResponseMapper;
    }

    public RDOUpdaterResponse execute(String id){
        var rdo = fetch.execute(id);
        var ppu = ppuRepository.findById(rdo.getPpuId()).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para a RDO."));
        var response = ppuResponseMapper.toResponse(ppu, rdo.getPlatform(), null, null, null, rdo.getSequentialId());
        removeEmployeesInAvailableServices(response);
        return new RDOUpdaterResponse(rdo, response);
    }

    private static void removeEmployeesInAvailableServices(PPUResponse response) {
        var services = response.getServices().stream().peek(e -> {
            if(e.getDisposicao()) {
            e.setEmployees(List.of());
            }
        }).toList();
        response.setServices(services);
    }
}
