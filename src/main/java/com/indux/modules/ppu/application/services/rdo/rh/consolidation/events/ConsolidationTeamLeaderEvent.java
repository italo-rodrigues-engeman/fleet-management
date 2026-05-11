package com.indux.modules.ppu.application.services.rdo.rh.consolidation.events;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.application.services.rdo.rh.competence.CompetenceFacade;
import com.indux.modules.ppu.domain.entities.item.TeamLeader;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventVariableRepository;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventsRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.exporters.ReportExcelGenerator;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.*;

@Component
public class ConsolidationTeamLeaderEvent extends ConsolidationEventAbstract {
    private final PPURepository ppuRepository;
    private static final long TEAM_LEADER_EVENT_CODE = 5L;

    protected ConsolidationTeamLeaderEvent(PayrollEventVariableRepository eventVariableRepository,
            PayrollEventsRepository eventsRepository, StorageService storageService, CompetenceFacade competenceService,
            RDORepository rdoRepository, ReportExcelGenerator reportExcelGenerator, PPURepository ppuRepository) {
        super(eventVariableRepository, eventsRepository, storageService, competenceService, rdoRepository,
                reportExcelGenerator);
        this.ppuRepository = ppuRepository;
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence) {
        return execute(rdos, competence, false);
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence, Boolean excelReport) {
        final Long event = fetchEventId(TEAM_LEADER_EVENT_CODE);
        Map<String, ConsolidationRecord> result = new HashMap<>();
        Map<String, PPUEntity> ppuCache = new HashMap<>();
        for (RDOEntity rdo : rdos) {
            if (rdo.getServices().isEmpty())
                continue;

            String ppuId = rdo.getPpuId();
            PPUEntity ppu = ppuCache.computeIfAbsent(ppuId, id -> ppuRepository.findById(id)
                    .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada: " + id)));

            for (RDOServiceEntity svc : rdo.getServices()) {
                if (!svc.isTeamLeader())
                    continue;
                String turno = svc.getSchedule().getTurno().toUpperCase();
                Double value = ppu.getTeamLeader().stream()
                        .filter(tl -> tl.getShift().toUpperCase().equalsIgnoreCase(turno))
                        .map(TeamLeader::getValue)
                        .findFirst()
                        .orElse(0.0);
                ConsolidationRecord novo = createRecord(rdo, svc, event, value, excelReport);
                result.merge(novo.matricula(), novo, this::mergeRecord);
            }
        }

        return result;
    }

    private ConsolidationRecord createRecord(RDOEntity rdo, RDOServiceEntity svc, Long teamLeaderEvent,
            Double valueTeamLeader, Boolean excelReport) {
        String event = "-";
        if (excelReport) {
            event = choiceEventNotVariable(teamLeaderEvent);
        } else {
            event = choiceEventNotVariable(teamLeaderEvent) + " CABO DE TURMA";
        }
        Set<String> platforms = Set.of(rdo.getPlatform());
        String referenceInTeamLeaderEvent = "0";
        String competencia = Optional.ofNullable(rdo.getCompetence()).orElse("-");
        final String value = valueTeamLeader.toString();

        return new ConsolidationRecord(
                svc.getRegistration(),
                event,
                referenceInTeamLeaderEvent,
                value,
                svc.getName(),
                platforms,
                competencia);
    }

    private ConsolidationRecord mergeRecord(ConsolidationRecord a, ConsolidationRecord b) {
        Double total = Double.parseDouble(a.valor()) + Double.parseDouble(b.valor());
        Set<String> plataformas = new HashSet<>(a.plataformas());
        plataformas.addAll(b.plataformas());

        return new ConsolidationRecord(
                a.matricula(),
                a.evento(),
                a.referencia(),
                total.toString(),
                a.nome(),
                plataformas,
                b.competencia());
    }

    @Override
    public String consolidate(YearMonth competence, Long project, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        final Long event = fetchEventId(TEAM_LEADER_EVENT_CODE);

        var rdos = getRDOByCompetence(competence, project, startDate, endDate);
        if (rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado para a competência: " + competence);
        }
        var competencePeriod = competence;
        var totalByRegistration = execute(rdos, competencePeriod, true);
        String fileName = "consolidacao-team-leader-" + choiceEventNotVariable(event) + "-"
                + competencePeriod.toString() + ".xlsx";
        var resource = createMultipartFile(totalByRegistration, fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return storeConsolidator(competencePeriod.toString(), resource, choiceEventNotVariable(event), project);
    }
}
