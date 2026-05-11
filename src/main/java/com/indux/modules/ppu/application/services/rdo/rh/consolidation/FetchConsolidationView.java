package com.indux.modules.ppu.application.services.rdo.rh.consolidation;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.application.dtos.response.competence.RDOConsolidationResponse;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationOvertimeEvent;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationPremiumNightEvent;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationTeamLeaderEvent;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOConsolidationMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FetchConsolidationView {
    private final ConsolidationOvertimeEvent overtimeEvent;
    private final ConsolidationTeamLeaderEvent teamLeaderEvent;
    private final ConsolidationPremiumNightEvent premiumNightEvent;
    private final ClosedCompetenceRDORepository closedRepository;
    private final RDORepository repository;
    private final RDOConsolidationMapper mapper;
    @Qualifier("consolidationExecutor")
    private final Executor executor;

    public FetchConsolidationView(
            ConsolidationOvertimeEvent overtimeEvent,
            ConsolidationTeamLeaderEvent teamLeaderEvent,
            ConsolidationPremiumNightEvent premiumNightEvent,
            ClosedCompetenceRDORepository closedRepository,
            RDORepository repository,
            RDOConsolidationMapper mapper,
            @Qualifier("consolidationExecutor") Executor executor
    ) {
        this.overtimeEvent = overtimeEvent;
        this.teamLeaderEvent = teamLeaderEvent;
        this.premiumNightEvent = premiumNightEvent;
        this.closedRepository = closedRepository;
        this.repository = repository;
        this.mapper = mapper;
        this.executor = executor;
    }

    public Page<RDOConsolidationResponse> execute(YearMonth competence, List<String> competenceItens, Long project) {
        List<RDOEntity> rdos;
        if (!competenceItens.isEmpty() && competence != null) {
            rdos = repository.findByIdIn(competenceItens);
        } else if (project != null) {
            rdos = repository.findAllByStatusAndCompetenceNullOrDashAndProject(
                    RDOStatusDP.APPROVED, "-", project
            );

        } else {
            rdos = repository.findAllByStatusAndCompetenceNullOrDash(
                    RDOStatusDP.APPROVED, "-"
            );
        }
        Result result = fetchEventsConsolidation(competence, rdos);
        return new PageImpl<>(result.pageContent(), Pageable.unpaged(), result.finalList().size());
    }

    @NotNull
    private Result fetchEventsConsolidation(YearMonth competence, List<RDOEntity> rdoContent) {
        CompletableFuture<Map<String, ConsolidationRecord>> overtimeFuture =
                CompletableFuture.supplyAsync(() -> overtimeEvent.execute(rdoContent, competence), executor);

        CompletableFuture<Map<String, ConsolidationRecord>> teamLeaderFuture =
                CompletableFuture.supplyAsync(() -> teamLeaderEvent.execute(rdoContent, competence), executor);

        CompletableFuture<Map<String, ConsolidationRecord>> premiumNightFuture =
                CompletableFuture.supplyAsync(() -> premiumNightEvent.execute(rdoContent, competence), executor);

        CompletableFuture.allOf(overtimeFuture, teamLeaderFuture, premiumNightFuture).join();

        var overtime = overtimeFuture.join();
        var teamLeader = teamLeaderFuture.join();
        var premiumNight = premiumNightFuture.join();

        List<ConsolidationRecord> allRecords = Stream.of(overtime, teamLeader, premiumNight)
                .flatMap(map -> map.values().stream())
                .toList();

        List<RDOConsolidationResponse> responses = mapper.toResponseList(allRecords);

        Map<String, List<RDOConsolidationResponse>> agrupado = responses.stream()
                .collect(Collectors.groupingBy(RDOConsolidationResponse::matricula));

        List<RDOConsolidationResponse> finalList = agrupado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(e -> e.getValue().stream())
                .toList();

        return new Result(finalList, finalList);
    }

    private record Result(List<RDOConsolidationResponse> finalList, List<RDOConsolidationResponse> pageContent) {
    }

    public Page<RDOConsolidationResponse> execute(YearMonth competence, Long project) {
        return execute(competence, Collections.emptyList(), project);
    }

    public Page<RDOConsolidationResponse> fetchConsolidationViewByCompetence(
            YearMonth competence,
            Long project
    ) {
        var rdosItens = closedRepository.findByCompetenceAndProject(competence.toString(), project).orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrado nenhuma competência nesse período e nesse projeto."));
        return execute(competence, rdosItens.getRdosClosed(), project);
    }

    public Page<RDOConsolidationResponse> filterConsolidation(Pageable page, RDOFilter filter, YearMonth competence) {
        var rdos = repository.findEntitiesByFilter(filter, Pageable.unpaged(), true);
        var rdosContent = rdos.getContent();

        if (filter.colaborador() != null) {
            rdosContent = rdosContent.stream()
                    .peek(rdo -> rdo.setServices(
                            rdo.getServices().stream()
                                    .filter(service -> service.getName().equals(filter.colaborador()))
                                    .toList()
                    ))
                    .toList();
        }

        Result result = fetchEventsConsolidation(competence, rdosContent);
        return new PageImpl<>(result.pageContent(), Pageable.unpaged(), result.finalList().size());
    }
}
