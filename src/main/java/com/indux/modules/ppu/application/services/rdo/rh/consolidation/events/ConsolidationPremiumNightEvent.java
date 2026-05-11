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
public class ConsolidationPremiumNightEvent extends ConsolidationEventAbstract {
    private static final long PREMIUM_NIGHT_EVENT_CODE = 4L;

    protected ConsolidationPremiumNightEvent(PayrollEventVariableRepository eventVariableRepository,
            PayrollEventsRepository eventsRepository, StorageService storageService, CompetenceFacade competenceService,
            RDORepository rdoRepository, ReportExcelGenerator reportExcelGenerator) {
        super(eventVariableRepository, eventsRepository, storageService, competenceService, rdoRepository,
                reportExcelGenerator);
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence) {
        return execute(rdos, competence, false);
    }

    @Override
    public Map<String, ConsolidationRecord> execute(List<RDOEntity> rdos, YearMonth competence, Boolean excelReport) {
        final Long premiumNightEvent = fetchEventId(PREMIUM_NIGHT_EVENT_CODE);
        Map<String, ConsolidationRecord> result = new HashMap<>();

        for (RDOEntity rdo : rdos) {
            if (rdo.getServices().isEmpty())
                continue;
            for (RDOServiceEntity svc : rdo.getServices()) {
                if (svc.getNightShiftPremium() == null || svc.getNightShiftPremium().isZero())
                    continue;
                ConsolidationRecord novo = createRecord(rdo, svc, premiumNightEvent, excelReport);
                result.merge(novo.matricula(), novo, this::mergeRecord);
            }
        }
        return result;
    }

    private ConsolidationRecord createRecord(RDOEntity rdo, RDOServiceEntity svc, Long premiumNightEvent,
            Boolean excelReport) {
        String event = "-";
        if (excelReport) {
            event = choiceEventNotVariable(premiumNightEvent);
        } else {
            event = choiceEventNotVariable(premiumNightEvent) + " Adicional Noturno";
        }
        Set<String> platforms = Set.of(rdo.getPlatform());
        String reference = formatDuration(svc.getNightShiftPremium());
        String competencia = Optional.ofNullable(rdo.getCompetence()).orElse("-");
        final String valueInPremiumNight = "0";

        return new ConsolidationRecord(
                svc.getRegistration(),
                event,
                reference,
                valueInPremiumNight,
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
        final Long event = fetchEventId(PREMIUM_NIGHT_EVENT_CODE);
        var rdos = getRDOByCompetence(competence, project, startDate, endDate);
        if (rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado para a competência: " + competence);
        }
        var competencePeriod = competence;
        var totalByRegistration = execute(rdos, competencePeriod, true);
        String fileName = "consolidacao-premium-night-" + choiceEventNotVariable(event) + "-"
                + competencePeriod.toString() + ".xlsx";
        var resource = createMultipartFile(totalByRegistration, fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return storeConsolidator(competencePeriod.toString(), resource, choiceEventNotVariable(event), project);
    }
}
