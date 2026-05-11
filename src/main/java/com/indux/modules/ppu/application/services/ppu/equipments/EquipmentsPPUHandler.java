package com.indux.modules.ppu.application.services.ppu.equipments;

import com.indux.modules.ppu.application.dtos.response.lines.EquipmentLineResponse;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.response.EquipmentLineResponseMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipmentsPPUHandler {
    private final EquipmentLineResponseMapper mapper;
    private final PPURepository ppuRepository;

    public EquipmentsPPUHandler(EquipmentLineResponseMapper mapper, PPURepository ppuRepository) {
        this.mapper = mapper;
        this.ppuRepository = ppuRepository;
    }

    public EquipmentResult fetchEquipments(String ppuID){
        var ppu = ppuRepository.findById(ppuID).orElseThrow(() -> new RuntimeException("PPU não encontrada no sistema."));
        var response = mapper.toResponses(ppu.getEquipments());
        return new EquipmentResult(response, ppu.getPlatforms());
    }

    public void updateEquipments(List<EquipmentLine> equipments, String ppuID) {
        var ppu = ppuRepository.findById(ppuID).orElseThrow(() -> new RuntimeException("PPU não encontrada no sistema."));
        ppu.setEquipments(equipments);
        ppuRepository.save(ppu);
    }

    public record EquipmentResult(List<EquipmentLineResponse> equipments, List<String> platforms){}
}
