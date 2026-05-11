package com.indux.core.application.service.cbo;

import com.indux.core.application.dto.cbo.*;
import com.indux.core.application.mapper.FuncaoHCMMapper;
import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.repository.cbo.CourseSuperiorRepository;
import com.indux.core.domain.repository.cbo.CourseTechnicalRepository;
import com.indux.core.domain.repository.cbo.FuncaoHCMRepository;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.faq.domain.repository.mongo.PerguntaMongoRepository;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.TrainingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuncaoHCMService {
    private final FuncaoHCMRepository funcaoHCMRepository;
    private final FilialService filialService;
    private final FuncaoHCMMapper  funcaoHCMMapper;
    private final CounterService counterService;
    private final AttachmentService attachmentService;
    private final TrainingRepository trainingRepository;
    private final CourseSuperiorRepository courseSuperiorRepository;
    private final CourseTechnicalRepository courseTechnicalRepository;
    private final PerguntaMongoRepository  perguntaMongoRepository;


    public FuncaoHCMService(FuncaoHCMRepository funcaoHCMRepository, FilialService filialService, FuncaoHCMMapper funcaoHCMMapper, CounterService counterService, AttachmentService attachmentService, TrainingRepository trainingRepository, CourseSuperiorRepository courseSuperiorRepository, CourseTechnicalRepository courseTechnicalRepository, PerguntaMongoRepository perguntaMongoRepository) {
        this.funcaoHCMRepository = funcaoHCMRepository;
        this.filialService = filialService;
        this.funcaoHCMMapper = funcaoHCMMapper;
        this.counterService = counterService;
        this.attachmentService = attachmentService;
        this.trainingRepository = trainingRepository;
        this.courseSuperiorRepository = courseSuperiorRepository;
        this.courseTechnicalRepository = courseTechnicalRepository;
        this.perguntaMongoRepository = perguntaMongoRepository;
    }

    public void createFuncaoHCM(FuncaoDTO createFuncao) {
        List<AttachmentEntity> attachments = createAttachmentsFromMultipartFiles(createFuncao.getAnexo());
        Long autoIncrementId = counterService.getNextSequence("funcaoHCM_sequence");
        FuncaoHCM funcao = funcaoHCMMapper.toEntity(createFuncao, attachments);
        funcao.setAutoIncrementId(autoIncrementId);
        idForEntity(createFuncao, funcao);
        funcaoHCMRepository.save(funcao);
    }

    private void idForEntity(FuncaoDTO createFuncao, FuncaoHCM funcao) {
        if (createFuncao.getTrainingsId() != null) {
            List<TrainingEntity> trainings = trainingRepository.findAllById(createFuncao.getTrainingsId());
            if (!trainings.isEmpty()) {
                funcao.setTrainings(trainings);
            }
        }
        if (createFuncao.getCourseSuperiorId() != null) {
            List<CourseSuperior> courseSuperior = courseSuperiorRepository.findAllById(createFuncao.getCourseSuperiorId());
            if (!courseSuperior.isEmpty()) {
                funcao.setCourseSuperior(courseSuperior);
            }
        }
        if (createFuncao.getCourseTechnicalId() != null) {
            List<CourseTechnical> courseTechnical = courseTechnicalRepository.findAllById(createFuncao.getCourseTechnicalId());
            if (!courseTechnical.isEmpty()) {
                funcao.setCourseTechnical(courseTechnical);
            }
        }
        if(createFuncao.getObrigatoryId() != null) {
            List<PerguntaMongo> obrigatory = perguntaMongoRepository.findAllById(createFuncao.getObrigatoryId());
            if (!obrigatory.isEmpty()) {
                funcao.setObrigatory(obrigatory);
            }
        }
        if(createFuncao.getProactiveId() != null) {
            List<PerguntaMongo> proactive = perguntaMongoRepository.findAllById(createFuncao.getProactiveId());
            if (!proactive.isEmpty()) {
                funcao.setProactive(proactive);
            }
        }
    }

    private List<AttachmentEntity> createAttachmentsFromMultipartFiles(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "cbo/documents");
    }

    public FuncaoDTO GetByIdHCM(String idHcm) {
        var funcao = funcaoHCMRepository.findByHcmId(idHcm).orElseThrow(() -> new ModuleNotFoundFailure("Função HCM não encontrada"));
        FuncaoDTO dto = funcaoHCMMapper.toDto(funcao);
        populateProjectData(dto);
        return dto;
    }

    public FuncaoDTO GetById(String id) {
        var funcao = funcaoHCMRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Função HCM não encontrada"));
        FuncaoDTO dto = funcaoHCMMapper.toDto(funcao);
        populateProjectData(dto);
        return dto;
    }

    public void updateFuncaoHCM(String id, FuncaoDTO updateFuncao) {
        var funcao = funcaoHCMRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Função HCM não encontrada"));
        if (isLastActionApproved(updateFuncao.getDataLog())) {
            boolean idHcmAlreadyExists = funcaoHCMRepository.findByHcmId(updateFuncao.getHcmId())
                    .filter(existing -> !existing.getId().equals(funcao.getId()))
                    .isPresent();
            if (idHcmAlreadyExists) {
                throw new ModuleBadRequest("IdHCM já existente");
            }
        }
        FuncaoHCM newFuncao;
        if (updateFuncao.getAnexo() != null) {
            List<AttachmentEntity> attachments = createAttachmentsFromMultipartFiles(updateFuncao.getAnexo());
            newFuncao = funcaoHCMMapper.toEntity(updateFuncao, attachments);
        } else {
            newFuncao = funcaoHCMMapper.toEntity(updateFuncao, funcao.getAnexo());
        }
        newFuncao.setId(funcao.getId());
        idForEntity(updateFuncao, newFuncao);

        appendHistoryIfApproved(newFuncao, funcao.getHistory());

        funcaoHCMRepository.save(newFuncao);
    }

    private void appendHistoryIfApproved(FuncaoHCM funcao, List<HistoryFuncao> existingHistory) {
        if (!isLastActionApproved(funcao.getDataLog())) {
            funcao.setHistory(existingHistory);
            return;
        }

        DataLog lastLog = funcao.getDataLog().get(funcao.getDataLog().size() - 1);

        List<HistoryFuncao> history = existingHistory != null
                ? new ArrayList<>(existingHistory)
                : new ArrayList<>();

        if (!history.isEmpty()) {
            history.get(history.size() - 1).setDiscontinuationDate(lastLog.getDate());
        }

        HistoryFuncao snapshot = funcaoHCMMapper.toHistorySnapshot(funcao);
        history.add(snapshot);

        funcao.setHistory(history);
    }

    private boolean isLastActionApproved(List<DataLog> dataLog) {
        if (dataLog == null || dataLog.isEmpty()) {
            return false;
        }
        DataLog lastLog = dataLog.get(dataLog.size() - 1);
        return lastLog.getJustification() != null
                && lastLog.getJustification().toLowerCase().contains("aprovado");
    }

    public void updateTraining(UpdateTraining updateTraining,Object name) {
        FuncaoHCM funcao = funcaoHCMRepository.findByHcmId(updateTraining.hcmId()).orElse(null);
        List<TrainingEntity> trainingEntities = trainingRepository.findAllById(updateTraining.trainingId());
        if (funcao == null) {
            Long autoIncrementId = counterService.getNextSequence("funcaoHCM_sequence");
            var funcaoEntity = funcaoHCMMapper.updateEntity(updateTraining);
            funcaoEntity.setAutoIncrementId(autoIncrementId);
            DataLog dataLog = new DataLog(name.toString(), LocalDateTime.now(),"CRIADO","Criado a partir da adição de treinamento");
            funcaoEntity.setDataLog(List.of(dataLog));
            if (!trainingEntities.isEmpty()) {
                funcaoEntity.setTrainings(trainingEntities);
            }
            funcaoHCMRepository.save(funcaoEntity);
        }else {
            if (!trainingEntities.isEmpty()) {
                funcao.setTrainings(trainingEntities);
                funcao.setRequiredTrainings(updateTraining.requiredTrainings());
                funcaoHCMRepository.save(funcao);
            }
        }
    }

    public Page<AllFuncao> getAllFuncaoHCM(Pageable pageable, FilterFuncao filter) {
        var allFuncao = funcaoHCMRepository.getWithFilterFuncao(filter, pageable);

        var allFilialIds = allFuncao.getContent().stream()
                .map(FuncaoHCM::getFilialHCM)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .map(Math::toIntExact)
                .collect(Collectors.toList());

        var filialList = filialService.filialOrganization(allFilialIds);

        allFuncao.getContent().forEach(funcao -> {
            if (funcao.getFilialHCM() != null && !funcao.getFilialHCM().isEmpty()) {
                List<Integer> filialIds = funcao.getFilialHCM().stream()
                        .map(Math::toIntExact)
                        .collect(Collectors.toList());

                var associatedInfo = filialList.stream()
                        .filter(f -> f.get("idFilial") instanceof Number id && filialIds.contains(id.intValue()))
                        .collect(Collectors.toList());

                List<String> branches = associatedInfo.stream()
                        .map(f -> (String) f.get("nomeRegional"))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                List<String> contracts = associatedInfo.stream()
                        .map(f -> (String) f.get("nomeContrato"))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                funcao.setBranch(branches);
                funcao.setContract(contracts);
            }
        });

        var dto = funcaoHCMMapper.getAllFuncaoHCM(allFuncao.getContent());
        return new PageImpl<>(dto, pageable, allFuncao.getTotalElements());
    }

    private void populateProjectData(FuncaoDTO dto) {
        if (dto.getFilialHCM() != null && !dto.getFilialHCM().isEmpty()) {
            List<Integer> ids = dto.getFilialHCM().stream()
                    .map(Math::toIntExact)
                    .collect(Collectors.toList());
            List<Map<String, Object>> projectInfo = filialService.filialOrganization(ids);

            List<Long> projectIds = projectInfo.stream()
                    .map(m -> (Long) m.get("idProjeto"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<String> projectNames = projectInfo.stream()
                    .map(m -> (String) m.get("nomeProjeto"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            dto.setProjectIds(projectIds);
            dto.setProjectNames(projectNames);
        }
    }
}
