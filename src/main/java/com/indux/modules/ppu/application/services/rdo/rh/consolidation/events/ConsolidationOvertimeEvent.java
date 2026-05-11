package com.indux.modules.ppu.application.services.rdo.rh.consolidation.events;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.application.services.rdo.rh.competence.CompetenceFacade;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventVariableRepository;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventsRepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.exporters.ReportExcelGenerator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.YearMonth;
import java.util.*;

@Component
public class ConsolidationOvertimeEvent extends ConsolidationEventAbstract {

    private static final long OVERTIME_EVENT_CODE = 1L;

    protected ConsolidationOvertimeEvent(PayrollEventVariableRepository eventVariableRepository,
            PayrollEventsRepository eventsRepository,
            StorageService storageService,
            CompetenceFacade competenceService,
            RDORepository rdoRepository,
            ReportExcelGenerator reportExcelGenerator) {
        super(eventVariableRepository, eventsRepository, storageService, competenceService, rdoRepository,
                reportExcelGenerator);
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence, Boolean excelReport) {
        final Long overtimeEvent = fetchEventId(OVERTIME_EVENT_CODE);
        Map<String, ConsolidationRecord> result = new HashMap<>();

        for (RDOEntity rdo : Optional.ofNullable(rdos).orElse(List.of())) {
            if (rdo.getServices() == null || rdo.getServices().isEmpty())
                continue;

            for (RDOServiceEntity svc : rdo.getServices()) {
                if (svc == null)
                    continue;
                if (Boolean.TRUE.equals(svc.getDisposicao()))
                    continue;

                Duration ot = Optional.ofNullable(svc.getOvertimeHourTotais()).orElse(Duration.ZERO);
                if (ot.isZero())
                    continue;

                ConsolidationRecord rec = createRecord(rdo, svc, ot, competence, overtimeEvent, excelReport);
                result.merge(rec.matricula(), rec, this::mergeRecord);
            }
        }
        return result;
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence) {
        return execute(rdos, competence, false);
    }

    private ConsolidationRecord createRecord(RDOEntity rdo,
            RDOServiceEntity svc,
            Duration overtimeDur,
            YearMonth competence,
            Long overtimeEventId,
            Boolean excelReport) {

        final String event = (excelReport != null && excelReport)
                ? (competence != null ? choiceEvent(overtimeEventId, competence) : "")
                : (competence != null ? (choiceEvent(overtimeEventId, competence) + " Hora Extra") : "Hora Extra");

        Set<String> platforms = Set.of(rdo.getPlatform());
        String reference = formatDuration(overtimeDur);
        String competencia = Optional.ofNullable(rdo.getCompetence()).orElse("-");
        final String valueInOvertimeEvent = "0";

        return new ConsolidationRecord(
                svc.getRegistration(),
                event,
                reference,
                valueInOvertimeEvent,
                svc.getName(),
                platforms,
                competencia);
    }

    private ConsolidationRecord mergeRecord(ConsolidationRecord a, ConsolidationRecord b) {
        Duration total = parseDuration(a.referencia()).plus(parseDuration(b.referencia()));
        Set<String> plataformas = new HashSet<>(a.plataformas());
        plataformas.addAll(b.plataformas());

        return new ConsolidationRecord(
                a.matricula(),
                a.evento(),
                formatDuration(total),
                a.valor(),
                a.nome(),
                plataformas,
                b.competencia());
    }

    @Override
    public String consolidate(YearMonth competence, Long project, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        final Long overtimeEvent = fetchEventId(OVERTIME_EVENT_CODE);
        var rdos = getRDOByCompetence(competence, project, startDate, endDate);
        if (rdos == null || rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado para a competência: " + competence);
        }
        var totalByRegistration = execute(rdos, competence, true);
        String fileName = "consolidacao-overtime-" + choiceEvent(overtimeEvent, competence) + "-" + competence
                + ".xlsx";
        var resource = createMultipartFile(totalByRegistration, fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return storeConsolidator(competence.toString(), resource, choiceEvent(overtimeEvent, competence), project);
    }
}
