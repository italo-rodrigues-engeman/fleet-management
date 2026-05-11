package com.indux.modules.ppu.application.services.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.item.RDOFrequency;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.missing.MissingRDO;
import com.indux.modules.ppu.domain.services.rdo.RDOFrequencyEvaluator;
import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.presentation.dtos.MissingRDOResponse;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.exporters.ReportMissingRDO;
import com.indux.modules.ppu.infra.mapper.MissingRDOMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetMissingRDOsUseCase {
    private final RDORepository repository;
    private final PPURepository ppuRepository;
    private final MissingRDOMapper missingMapper;
    private final ReportMissingRDO reportGenerator;
    private final RDOFrequencyEvaluator frequencyEvaluator;

    public GetMissingRDOsUseCase(
            RDORepository repository,
            PPURepository ppuRepository,
            ReportMissingRDO reportGenerator,
            MissingRDOMapper missingMapper,
            RDOFrequencyEvaluator frequencyEvaluator
    ) {
        this.repository = repository;
        this.ppuRepository = ppuRepository;
        this.reportGenerator = reportGenerator;
        this.missingMapper = missingMapper;
        this.frequencyEvaluator = frequencyEvaluator;
    }

    public ResponseMissingDTO execute(MissingRDORequest request) {
        if (request.project() != null) {
            var specific = getSpecificRDODetails(request);
            List<MissingRDOResponse> responses = specific.missing().stream()
                    .map(missingMapper::toResponse)
                    .toList();
            return new ResponseMissingDTO(specific.onDemands(), responses);
        }

        List<PPUEntity> ppus = ppuRepository.findAllByStatus(DocumentStatus.ABERTO);

        Set<String> requestedPlatforms = Optional.ofNullable(request.platforms())
                .filter(l -> !l.isEmpty())
                .map(HashSet::new)
                .orElse(null);

        Set<String> onDemandGlobal = new HashSet<>();
        List<MissingRDO> missingGlobal = new ArrayList<>();

        for (PPUEntity ppu : ppus) {
            Map<String, RDOFrequency> freqs = Optional.ofNullable(ppu.getPlatformFrequencies())
                    .orElse(Collections.emptyMap());

            freqs.entrySet().stream()
                    .filter(e -> e.getValue() == RDOFrequency.ON_DEMAND)
                    .map(Map.Entry::getKey)
                    .forEach(onDemandGlobal::add);

            String regional = ppu.getRegionalNome();
            Map<String, Object> contractSnapshot = ppu.getContract();

            List<String> platforms = Optional.ofNullable(ppu.getPlatforms()).orElse(List.of());
            if (requestedPlatforms != null) {
                platforms = platforms.stream().filter(requestedPlatforms::contains).toList();
                if (platforms.isEmpty()) continue;
            }

            missingGlobal.addAll(processPlatforms(
                    ppu,
                    platforms,
                    regional,
                    contractSnapshot,
                    request.startDate(),
                    request.endDate()
            ));
        }

        List<MissingRDOResponse> responses = missingGlobal.stream()
                .map(missingMapper::toResponse)
                .toList();

        return new ResponseMissingDTO(onDemandGlobal.stream().sorted().toList(), responses);
    }

    public byte[] generateReport(MissingRDORequest request, String userName) {
        ResponseMissingDTO dto = execute(request);
        return reportGenerator.generate(dto.missing(), request, userName);
    }

    private SpecificResult getSpecificRDODetails(MissingRDORequest request) {
        PPUEntity ppu = ppuRepository.findByProjectId(request.project())
                .orElseThrow(() -> new ModuleNotFoundFailure("Não existe PPU para esse projeto."));

        Map<String, RDOFrequency> platformFrequencies = Optional.ofNullable(ppu.getPlatformFrequencies())
                .orElse(Collections.emptyMap());

        List<String> onDemands = platformFrequencies.entrySet().stream()
                .filter(e -> e.getValue() == RDOFrequency.ON_DEMAND)
                .map(Map.Entry::getKey)
                .toList();

        List<String> available = Optional.ofNullable(ppu.getPlatforms()).orElse(List.of());
        List<String> requested = Optional.ofNullable(request.platforms()).filter(l -> !l.isEmpty()).orElse(available);

        Set<String> notFound = requested.stream()
                .filter(p -> !available.contains(p))
                .collect(Collectors.toSet());

        if (!notFound.isEmpty()) {
            throw new ModuleNotFoundFailure("Plataformas não encontradas na PPU: " + String.join(", ", notFound));
        }

        List<MissingRDO> missing = processPlatforms(ppu, requested, ppu.getRegionalNome(), ppu.getContract(), request.startDate(), request.endDate());
        return new SpecificResult(onDemands, missing);
    }

    private List<MissingRDO> processPlatforms(
            PPUEntity ppu,
            Collection<String> platforms,
            String regional,
            Map<String, Object> contractSnapshot,
            LocalDate startDate,
            LocalDate endDate
    ) {
        final LocalDate adjustedEndDate = endDate.isEqual(LocalDate.now()) ? endDate.minusDays(1) : endDate;

        LocalDate searchStartDate = startDate.minusDays(1);
        LocalDate searchEndDate = adjustedEndDate.plusDays(1);
        List<RDOEntity> rdos = repository.findByPlatformInAndPpuIdAndDateBetween(platforms, ppu.getId(), searchStartDate, searchEndDate);

        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = adjustedEndDate;

        Map<String, RDOFrequency> platformFrequencies = Optional.ofNullable(ppu.getPlatformFrequencies())
                .orElse(Collections.emptyMap());

        List<MissingRDO> result = new ArrayList<>();

        for (String platform : platforms) {
            RDOFrequency frequency = platformFrequencies.getOrDefault(platform, RDOFrequency.DAILY);

            List<RDOEntity> platformRdos = rdos.stream()
                    .filter(r -> platform.equals(r.getPlatform()))
                    .filter(r -> !r.getDate().isBefore(finalStartDate) && !r.getDate().isAfter(finalEndDate))
                    .toList();

            List<LocalDate> missingDates = frequencyEvaluator.findMissingDates(
                    finalStartDate,
                    finalEndDate,
                    platform,
                    frequency,
                    platformRdos
            );

            for (LocalDate missingDate : missingDates) {
                result.add(buildMissingRDO(missingDate, null, platform, regional, contractSnapshot));
            }
        }

        return result;
    }

    private MissingRDO buildMissingRDO(
            LocalDate date,
            String rdoId,
            String platform,
            String regional,
            Map<String, Object> contract
    ) {
        final String contractName = "projectName";
        return MissingRDO.builder()
                .date(date)
                .rdo(rdoId)
                .platform(platform)
                .regional(regional)
                .contract(contract.get(contractName) != null ? contract.get(contractName).toString()
                        : contract.get("nome_projeto") != null ? contract.get("nome_projeto").toString() : "-")
                .contractId(contract.get("id") != null ? Long.parseLong(contract.get("id").toString()) : 0L)
                .build();
    }

    public record ResponseMissingDTO(
            @JsonProperty("sobDemanda") List<String> onDemands,
            @JsonProperty("faltantes") List<MissingRDOResponse> missing
    ) {}

    private record SpecificResult(List<String> onDemands, List<MissingRDO> missing) {}
}
