package com.indux.modules.ppu.application.services.rdo.rh.consolidation.events;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.filestorage.impl.ByteArrayResourceMultipartFile;
import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import com.indux.modules.ppu.application.services.rdo.helper.RDODurationHelper;
import com.indux.modules.ppu.application.services.rdo.rh.competence.CompetenceFacade;
import com.indux.modules.ppu.domain.entities.jpa.PayrollEventVariable;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventVariableRepository;
import com.indux.modules.ppu.domain.repositories.jpa.PayrollEventsRepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.exporters.ReportExcelGenerator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ConsolidationEventAbstract extends RDODurationHelper implements ConsolidationEvent {

    private final PayrollEventVariableRepository eventVariableRepository;
    private  final PayrollEventsRepository eventsRepository;
    private final StorageService storageService;
    private final CompetenceFacade competenceService;
    private final RDORepository rdoRepository;
    private final ReportExcelGenerator reportExcelGenerator;

    protected ConsolidationEventAbstract(PayrollEventVariableRepository eventVariableRepository, PayrollEventsRepository eventsRepository, StorageService storageService, CompetenceFacade competenceService, RDORepository rdoRepository, ReportExcelGenerator reportExcelGenerator) {
        this.eventVariableRepository = eventVariableRepository;
        this.eventsRepository = eventsRepository;
        this.storageService = storageService;
        this.competenceService = competenceService;
        this.rdoRepository = rdoRepository;
        this.reportExcelGenerator = reportExcelGenerator;
    }

    protected String choiceEvent(Long reference, YearMonth competence){
        PayrollEventVariable event = eventVariableRepository.findByCompetenceAndPayrollEvent_Id(competence, reference).orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrado nenhum status de folha com o id: " + reference + " e competência: " + competence));
        return event.getCode().toString();
    }

    protected String choiceEventNotVariable(Long reference){
        return eventVariableRepository.findByPayrollEvent_Id(reference)
                .orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrado nenhum status de folha com o id: " + reference))
                .getCode()
                .toString();
    }

    protected Long fetchEventId(Long eventId) {
        return eventsRepository.findById(eventId).orElseThrow(() -> new ModuleNotFoundFailure("Evento de hora extra não encontrado")).getId();
    }

    protected List<RDOEntity> getRDOByCompetence(YearMonth competencePeriod, Long project, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return rdoRepository.findAllByProjectIdAndDateBetween(project, startDate, endDate);
        }
        var competence = competenceService.get(competencePeriod, project);
        if (competence == null)
            throw new ModuleNotFoundFailure("Não foi encontrado nenhuma competência fechada para o período informado: " + competencePeriod);
        return rdoRepository.findAllById(competence.getRdosClosed());
    }
    protected String storeConsolidator(String competencePeriod, MultipartFile file, String eventCode, Long project) {
        var path = storageService.store(file, "consolidacao/rdo/" + competencePeriod, "consolidacao-event-" + eventCode + "-" + competencePeriod + "project-" + project + ".xlsx");
        String uri = storageService.getRootLocation().relativize(path).toString();
        return uri.replace("\\", "/");
    }
    protected MultipartFile createMultipartFile(Map<String, ConsolidationRecord> totalRegistration, String fileName, String contentType) {
        var resource = reportExcelGenerator.generate(totalRegistration.values().stream().toList());
        return new ByteArrayResourceMultipartFile(
                resource,
                "file",
                fileName,
                contentType
        );
    }
}
