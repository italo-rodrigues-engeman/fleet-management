package com.indux.modules.ppu.application.services.rdo.operation.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.bm.BMContext;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOEquipment;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BMEquipments {

    private final PPURepository ppuRepository;
    private final RDORepository rdoRepository;
    private final BMRepository bmRepository;
    private final BMMapper mapper;

    public BMEquipments(PPURepository ppuRepository, RDORepository rdoRepository, BMRepository bmRepository, BMMapper mapper) {
        this.ppuRepository = ppuRepository;
        this.rdoRepository = rdoRepository;
        this.bmRepository = bmRepository;
        this.mapper = mapper;
    }

    public List<EquipmentSummaryDTO> calculate(String bmID, String platform){
        var bm = bmRepository.findById(bmID).orElseThrow(()-> new ModuleNotFoundFailure("BM não encontrada"));
        var ppu = ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para esse contrato."));

        DateRange bmPeriod = bm.getPeriod();
        if (bmPeriod == null || bmPeriod.getStart() == null || bmPeriod.getEnd() == null) {
            throw new ModuleFailure("BM não possui período de medição definido.");
        }

        LocalDate actualStart = bmPeriod.getStart();
        LocalDate actualEnd = bmPeriod.getEnd();
        BMContext context = new BMContext(actualStart, actualEnd, platform, bm.getProjectId());

        List<RDOEntity> rdoList;
        if (context.isAllPlatforms()) {
            List<String> platforms = ppu.getPlatforms();
            rdoList = rdoRepository.findAllByPlatformInAndStatusOPAndDateBetween(platforms, RDOStatusOP.APPROVED, actualStart, actualEnd);
            context.setPlatforms(platforms);
        } else {
            rdoList = rdoRepository.findAllByPlatformAndStatusOPAndDateBetween(platform, RDOStatusOP.APPROVED, actualStart, actualEnd);
            context.setPlatforms(List.of(platform));
        }

        return calcEquipments(rdoList, ppu);
    }


    public List<EquipmentSummaryDTO> calcEquipments(List<RDOEntity> entities, PPUEntity ppu){
        Map<String, Integer> totalCheckersMap = entities.stream()
                .flatMap(rdo -> rdo.getEquipments().stream())
                .collect(Collectors.groupingBy(
                        RDOEquipment::getEquipmentPPUId,
                        Collectors.summingInt(eq ->
                                eq.getCheckers() == null ? 0 : eq.getCheckers().size()
                        )
                ));

        return ppu.getEquipments().stream()
                .map(ppuEq -> {
                    int totalCheckers = totalCheckersMap.getOrDefault(ppuEq.getId(), 0);

                    BigDecimal totalValue = BigDecimal.valueOf(ppuEq.getValue())
                            .multiply(BigDecimal.valueOf(totalCheckers));

                    return new EquipmentSummaryDTO(
                            ppuEq.getId(),
                            ppuEq.getName(),
                            totalCheckers,
                            BigDecimal.valueOf(ppuEq.getValue()),
                            totalValue
                    );
                })
                .toList();

    }

    public record EquipmentSummaryDTO(
            String equipmentPPUId,
            String name,
            int totalCheckers,
            BigDecimal unitValue,
            BigDecimal totalValue
    ) {}
}
