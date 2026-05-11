package com.indux.modules.training.application.service;

import com.indux.core.application.dto.generic.EmployeeFilter;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.application.dto.cbo.RequiredTrainingStraring;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.repository.cbo.FuncaoHCMRepository;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.training.application.dto.*;
import com.indux.modules.training.domain.entity.ClassEntity;
import com.indux.modules.training.domain.entity.InstituinEntity;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.ClassRepository;
import com.indux.modules.training.domain.repository.InstituinRepository;
import com.indux.modules.training.domain.repository.TrainingRepository;
import com.indux.modules.training.infra.mappers.ClassMapper;
import com.indux.modules.training.infra.mappers.DossierMapper;
import com.indux.modules.training.infra.mappers.EmployeeTrainingMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.indux.core.domain.model.modules.AttachmentEntity;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.util.stream.Stream;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final DossierMapper dossierMapper;
    private final EmployeeTrainingMapper employeeTrainingMapper;
    private final TrainingRepository trainingRepository;
    private final AttachmentService attachmentService;
    private final FilialService filialService;
    private final GetEmployeeUseCase employeeUseCase;
    private final FuncaoHCMRepository funcaoHCMRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final EmployeeRepository employeeRepository;
    private final CounterService  counterService;
    private final InstituinRepository instituinRepository;


    public ClassService(ClassRepository classRepository, ClassMapper classMapper, DossierMapper dossierMapper, EmployeeTrainingMapper employeeTrainingMapper, TrainingRepository trainingRepository,
                        AttachmentService attachmentService, FilialService filialService, GetEmployeeUseCase employeeUseCase,
                        FuncaoHCMRepository funcaoHCMRepository, EmployeePositionRepository employeePositionRepository, 
                        EmployeeRepository employeeRepository, CounterService counterService, InstituinRepository instituinRepository) {
        this.classRepository = classRepository;
        this.classMapper = classMapper;
        this.dossierMapper = dossierMapper;
        this.employeeTrainingMapper = employeeTrainingMapper;
        this.trainingRepository = trainingRepository;
        this.attachmentService = attachmentService;
        this.filialService = filialService;
        this.employeeUseCase = employeeUseCase;
        this.funcaoHCMRepository = funcaoHCMRepository;
        this.employeePositionRepository = employeePositionRepository;
        this.employeeRepository = employeeRepository;
        this.counterService = counterService;
        this.instituinRepository = instituinRepository;
    }

    public void createClass(ClassRequest classRequest){
        TrainingEntity training = trainingRepository.findById(classRequest.trainingId()).orElseThrow(() -> new ModuleNotFoundFailure("Treinamento não encontrado"));
        InstituinEntity instituin = null;
        if (classRequest.instituinId() != null){
            instituin = instituinRepository.findById(classRequest.instituinId()).orElseThrow(() -> new ModuleNotFoundFailure("Instituição não encontrado"));
        }
        ClassEntity classEntity = classMapper.toEntity(classRequest);
        var couter = counterService.getNextSequence("class");
        classEntity.setTraining(training);
        classEntity.setInstituin(instituin);
        classEntity.setStatus(false);
        saveFile(classRequest, classEntity);
        saveCollaboratorsFiles(classRequest, classEntity);
        classEntity.setAutoId(couter);
        classRepository.save(classEntity);
    }

    public ClassResponse findClassById(String id){
        ClassEntity classEntity = classRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Turma não encontrado"));
        var training = trainingRepository.findById(classEntity.getTraining().getId());
        classEntity.setTraining(training.get());
        ClassResponse response = classMapper.toResponseDTO(classEntity);
        return enrichWithOrganizationData(response);
    }

    public Page<ClassResponse> findAllClasses(Pageable pageable){
        Page<ClassEntity> entities = classRepository.findAll(pageable);
        List<ClassResponse> responses = entities.getContent().stream()
                .map(classMapper::toResponseDTO)
                .map(this::enrichWithOrganizationData)
                .toList();
        
        return new PageImpl<>(responses, pageable, entities.getTotalElements());
    }

    public void updateClass(String id, ClassRequest classRequest){
        InstituinEntity instituin = null;
        ClassEntity classEntity = classRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Turma não encontrado"));
        TrainingEntity training = trainingRepository.findById(classRequest.trainingId()).orElseThrow(() -> new ModuleNotFoundFailure("Treinamento não encontrado"));
        if (classRequest.instituinId() != null){
             instituin = instituinRepository.findById(classRequest.instituinId()).orElseThrow(() -> new ModuleNotFoundFailure("Instituição não encontrado"));
        }
        ClassEntity classUpdated = classMapper.toEntity(classRequest);
        classUpdated.setId(id);
        classUpdated.setAutoId(classEntity.getAutoId());
        classUpdated.setTraining(training);
        classUpdated.setInstituin(instituin);
        classUpdated.setStatus(true);
        setOldAttachment(classEntity, classUpdated);
        saveFile(classRequest, classUpdated);
        saveCollaboratorsFiles(classRequest, classUpdated, classEntity);
        
        classRepository.save(classUpdated);
    }
    
    private void saveFile(ClassRequest dto, ClassEntity entity) {
        if (dto.file() != null) {
            List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                List.of(dto.file()),
                "training/class/documents"
            );
            
            if (!attachments.isEmpty()) {
                entity.setAttachment(attachments.getFirst());
            }
        }
    }
    
    private void setOldAttachment(ClassEntity entity, ClassEntity updateEntity) {
        if (entity.getAttachment() != null) {
            updateEntity.setAttachment(entity.getAttachment());
        }
    }
    
    private void saveCollaboratorsFiles(ClassRequest dto, ClassEntity entity, ClassEntity originalEntity) {
        if (dto.collaborators() != null && !dto.collaborators().isEmpty()) {
            List<EmployeeResponseDTO> updatedCollaborators = dto.collaborators().stream()
                .map(collaborator -> {
                    AttachmentEntity attachment = null;
                    if (collaborator.file() != null) {
                        List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                            List.of(collaborator.file()),
                            "training/class/collaborators"
                        );
                        
                        if (!attachments.isEmpty()) {
                            attachment = attachments.getFirst();
                        }
                    } else {
                        if (originalEntity != null && originalEntity.getCollaborators() != null) {
                            Optional<EmployeeResponseDTO> existingCollaborator = originalEntity.getCollaborators().stream()
                                .filter(existing -> existing.registration() != null && 
                                                  existing.registration().equals(collaborator.registration()))
                                .findFirst();
                            
                            if (existingCollaborator.isPresent() && existingCollaborator.get().attachment() != null) {
                                attachment = existingCollaborator.get().attachment();
                            }
                        }
                    }
                    
                    return new EmployeeResponseDTO(
                        collaborator.id(),
                        collaborator.name(),
                        collaborator.registration(),
                        collaborator.position(),
                        collaborator.situation(),
                        collaborator.score(),
                        attachment
                    );
                })
                .collect(Collectors.toList());
            
            entity.setCollaborators(updatedCollaborators);
        }
    }
    
    private void saveCollaboratorsFiles(ClassRequest dto, ClassEntity entity) {
        saveCollaboratorsFiles(dto, entity, null);
    }
    
    private ClassResponse enrichWithOrganizationData(ClassResponse response) {
        if (response.filialHCM() != null) {
            Integer filialId = Integer.valueOf(response.filialHCM());
            List<Map<String, Object>> filialList = filialService.filialOrganization(List.of(filialId));
            
            if (!filialList.isEmpty()) {
                Map<String, Object> orgData = filialList.getFirst();
                
                return classMapper.enrichWithOrganizationData(response, orgData);
            }
        }
        
        return response;
    }

    public List<Dossier> getDossiers(List<String> trainingIds, String registration){
        List<ClassEntity> classes = classRepository.findClassesFilter(trainingIds, registration);
        return dossierMapper.toDossierListWithFilter(classes, registration);
    }

    public Page<DueDate> getDueDates(EmployeeFilter filter, Pageable pageable) {
        Page<EmployeeSummaryDTO> employees = employeeUseCase.filterEmployeesWithFilter(filter, pageable);
        
        List<DueDate> dueDates = employees.getContent().stream()
            .map(employee -> {
                LocalDate hiringDate = getEmployeeHiringDate(employee.getMatricula());
                List<String> trainingIds = getFilteredTrainingIdsByPosition(employee.getCargo(), hiringDate);
                int trainingCount = trainingIds.size();
                List<Dossier> dossiers = getDossiers(trainingIds, employee.getMatricula());

                dossiers = dossiers.stream()
                            .filter(dossier -> {
                                if (dossier.dossierClasses() == null || dossier.dossierClasses().isEmpty()) {
                                    return false;
                                }

                                Optional<DossierClass> latestClass = dossier.dossierClasses().stream()
                                        .filter(dc -> dc.dateEnd() != null)
                                        .max(Comparator.comparing(DossierClass::dateEnd));

                                return latestClass.isPresent() &&
                                        "APROVADO".equalsIgnoreCase(latestClass.get().situation());
                            })
                            .collect(Collectors.toList());
                
                int dossierCount = dossiers.size();
                
                return employeeTrainingMapper.toDueDate(employee, trainingCount, dossierCount);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(dueDates, pageable, employees.getTotalElements());
    }

    public Page<DueDetail> getDueDetail(EmployeeFilter filter, Pageable pageable){
        Page<EmployeeSummaryDTO> employees = employeeUseCase.filterEmployeesWithFilter(filter, pageable);

        List<DueDetail> dueDetails = employees.getContent().stream()
                .flatMap(employee -> {
                    LocalDate hiringDate = getEmployeeHiringDate(employee.getMatricula());
                    List<String> trainingIds = getFilteredTrainingIdsByPosition(employee.getCargo(), hiringDate);
                    List<Dossier> dossiers = getDossiers(trainingIds, employee.getMatricula());
                    
                    Map<String, String> trainingIdToName = trainingRepository.findAllById(trainingIds)
                            .stream()
                            .collect(Collectors.toMap(TrainingEntity::getId, TrainingEntity::getName));
                    
                    String regional = employee.getRegional() != null ? employee.getRegional() : "";
                    String project = employee.getNomeProjeto() != null ? employee.getNomeProjeto() : "";
                    
                    // If employee has no trainings, create a single record with "Sem Treinamentos" status
                    if (trainingIds.isEmpty()) {
                        return Stream.of(employeeTrainingMapper.toDueDetailWithoutTrainings(employee));
                    }
                    
                    return trainingIds.stream()
                            .map(trainingId -> {
                                String trainingName = trainingIdToName.getOrDefault(trainingId, "Unknown Training");
                                
                                List<Dossier> trainingDossiers = dossiers.stream()
                                        .filter(dossier -> dossier.trainingName().equals(trainingName))
                                        .toList();
                                
                                String status = calculateStatusForTraining(trainingDossiers);
                                
                                return employeeTrainingMapper.toDueDetail(employee, trainingName, trainingDossiers, status);
                            });
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dueDetails, pageable, employees.getTotalElements());
    }

    private String calculateStatusForTraining(List<Dossier> dossiers, Clock clock) {
        if (dossiers == null || dossiers.isEmpty()) {
            return "NAO_AGENDADO";
        }

        var latestClassOpt = dossiers.stream()
                .filter(Objects::nonNull)
                .flatMap(d -> Optional.ofNullable(d.dossierClasses()).orElse(List.of()).stream())
                .filter(Objects::nonNull)
                .max(Comparator
                        .comparing(DossierClass::dateEnd, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(DossierClass::dateStrart, Comparator.nullsFirst(Comparator.naturalOrder()))
                );

        if (latestClassOpt.isEmpty()) {
            return "NAO_AGENDADO";
        }

        var latestClass = latestClassOpt.get();
        var today = LocalDate.now(clock);

        var validityDate = latestClass.validity();
        if (validityDate != null && today.isAfter(validityDate)) {
            return "VENCIDO";
        }

        var startDate = latestClass.dateStrart();
        var endDate = latestClass.dateEnd();
        if (startDate == null || endDate == null) {
            return "NAO_AGENDADO";
        }

        var situation = latestClass.situation();
        var isApproved = situation != null && situation.trim().equalsIgnoreCase("APROVADO");
        var hasNoResult = situation == null || situation.trim().isEmpty();

        if (today.isAfter(endDate)) {
            if (isApproved) return "CONCLUIDO";
            if (hasNoResult) return "AGUARDANDO_RESULTADO";
            return "REPROVADO";
        }

        var isInProgress = !today.isBefore(startDate) && !today.isAfter(endDate);
        if (isInProgress) {
            return "EM_ANDAMENTO";
        }

        return "AGENDADO";
    }

    private String calculateStatusForTraining(List<Dossier> dossiers) {
        return calculateStatusForTraining(dossiers, Clock.systemDefaultZone());
    }
    
    private List<String> getTrainingIdsByPosition(String positionId) {
        if (positionId == null || positionId.isEmpty()) {
            return List.of();
        }
        
            EmployeePosition position = employeePositionRepository.findByIdHCM(positionId);
            if (position == null) {
                return List.of();
            }
            
            FuncaoHCM funcao = funcaoHCMRepository.findByHcmId(positionId).orElse(null);
            if (funcao != null && funcao.getTrainings() != null) {
                return funcao.getTrainings().stream()
                    .map(TrainingEntity::getId)
                    .collect(Collectors.toList());
            }
        
        return List.of();
    }

    private List<String> getFilteredTrainingIdsByPosition(String positionId, LocalDate hiringDate) {
        if (positionId == null || positionId.isEmpty() || hiringDate == null) {
            return List.of();
        }
        
        EmployeePosition position = employeePositionRepository.findByIdHCM(positionId);
        if (position == null) {
            return List.of();
        }
        
        FuncaoHCM funcao = funcaoHCMRepository.findByHcmId(positionId).orElse(null);
        if (funcao == null) {
            return List.of();
        }
        
        List<String> allTrainingIds = new ArrayList<>();
        
        if (funcao.getTrainings() != null) {
            allTrainingIds.addAll(funcao.getTrainings().stream()
                .map(TrainingEntity::getId)
                .collect(Collectors.toList()));
        }
        
        if (funcao.getRequiredTrainings() != null) {
            List<String> requiredTrainingIds = funcao.getRequiredTrainings().stream()
                .filter(requiredTraining -> requiredTraining.starting() != null && 
                                          requiredTraining.starting().isAfter(hiringDate))
                .map(RequiredTrainingStraring::trainingId)
                .collect(Collectors.toList());
            
            allTrainingIds.removeAll(requiredTrainingIds);
        }

        return allTrainingIds.stream().distinct().collect(Collectors.toList());
    }

    private LocalDate getEmployeeHiringDate(String matricula) {
        if (matricula == null || matricula.isEmpty()) {
            return null;
        }
        
        List<Employee> employees = employeeRepository.findAllByRegistration(matricula);
        
        if (!employees.isEmpty()) {
            return employees.stream()
                .filter(emp -> emp.getAdmissionDate() != null && 
                              !emp.getAdmissionDate().equals(java.time.LocalDate.of(1900, 12, 31)))
                .max(java.util.Comparator.comparing(Employee::getAdmissionDate))
                .map(Employee::getAdmissionDate)
                .orElse(null);
        }

        return null;
    }


}
