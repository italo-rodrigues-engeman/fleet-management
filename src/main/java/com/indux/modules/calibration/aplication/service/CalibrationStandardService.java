package com.indux.modules.calibration.aplication.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.*;
import com.indux.modules.calibration.domain.entities.mongo.*;
import com.indux.modules.calibration.domain.repository.mongo.*;
import com.indux.modules.calibration.infra.mappers.CalibrationStandardMapper;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalibrationStandardService {
    
    private final CalibrationStandardRepository calibrationStandardRepository;
    private final CalibrationStandardMapper calibrationStandardMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationContractRepository contractRepository;
    private final OrganizationProjectRepository projectRepository;
    private final CounterService counterService;
    private final EquipamentRepository equipamentRepository;
    private final UnitRepository unitRepository;
    private final MeasuresRepository measuresRepository;
    private final PropertiesRepository propertiesRepository;
    private final ToleranceRepository toleranceRepository;
    private final RangeRepository rangeRepository;
    private final GeralMapper geralMapper;
    private final AttachmentService attachmentService;

    public CalibrationStandardService(
            CalibrationStandardRepository calibrationStandardRepository,
            CalibrationStandardMapper calibrationStandardMapper,
            OrganizationRepository organizationRepository,
            OrganizationContractRepository contractRepository,
            OrganizationProjectRepository projectRepository,
            CounterService counterService,
            EquipamentRepository equipamentRepository,
            UnitRepository unitRepository,
            MeasuresRepository measuresRepository,
            PropertiesRepository propertiesRepository,
            ToleranceRepository toleranceRepository,
            RangeRepository rangeRepository,
            GeralMapper geralMapper, AttachmentService attachmentService) {
        this.calibrationStandardRepository = calibrationStandardRepository;
        this.calibrationStandardMapper = calibrationStandardMapper;
        this.organizationRepository = organizationRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.counterService = counterService;
        this.equipamentRepository = equipamentRepository;
        this.unitRepository = unitRepository;
        this.measuresRepository = measuresRepository;
        this.propertiesRepository = propertiesRepository;
        this.toleranceRepository = toleranceRepository;
        this.rangeRepository = rangeRepository;
        this.geralMapper = geralMapper;
        this.attachmentService = attachmentService;
    }
    
    public void createCalibrationStandard(CalibrationStandardCreate createDto) {
        Long autoIncrementId = counterService.getNextSequence("calibration_standard_sequence");
        CalibrationStandardEntity entity = calibrationStandardMapper.toEntity(createDto);
        setEntities(createDto, entity);
        createFiles(createDto, entity);
        entity.setAutoIncrementId(autoIncrementId);
        calibrationStandardRepository.save(entity);
    }

    public Page<CalibrationStandardAll> getAllCalibrationStandards(Pageable pageable, CalibrationStandardFilter filter) {
        Page<CalibrationStandardEntity> findAll = calibrationStandardRepository.findAllFilter(pageable, filter);
        var standard = calibrationStandardMapper.toDTOAll(findAll.getContent());

        return new PageImpl<>(standard, pageable, findAll.getTotalElements());
    }

    public CalibrationStandardReturn getCalibrationStandardsById(String id) {
        CalibrationStandardEntity standard = calibrationStandardRepository.findById(id).orElseThrow(() -> new ModuleFailure("Padrão de calibração não existe."));

        CalibrationStandardReturn standardReturn = calibrationStandardMapper.toDTOReturn(standard);

        List<RangeReturn> updatedRanges = new ArrayList<>();
        if (standard != null && standard.getRange() != null) {
            for (RangeEntity range : standard.getRange()) {
                List<ToleranceReturn> updatedTolerances = new ArrayList<>();
                if (range != null && range.getTolerance() != null) {
                    setOrganizationTolerance(range.getTolerance(), updatedTolerances);
                }
                
                if (range != null) {
                    RangeReturn updatedRange = new RangeReturn(
                            range.getName(),
                            range.getAmount(),
                            geralMapper.measuresDTO(range.getMeasure()),
                            geralMapper.unitMeasuresDTO(range.getUnit()),
                            range.getRange1(),
                            range.getRange2(),
                            range.getResolution(),
                            range.getReference(),
                            updatedTolerances
                    );
                    updatedRanges.add(updatedRange);
                }
            }
        }

        return new CalibrationStandardReturn(
                standardReturn != null ? standardReturn.id() : null,
                standardReturn != null ? standardReturn.autoIncrementId() : null,
                standardReturn != null ? standardReturn.properties() : null,
                standardReturn != null ? standardReturn.equipament() : null,
                standardReturn != null ? standardReturn.model() : null,
                updatedRanges,
                standardReturn != null ? standardReturn.messageApproval() : null,
                standardReturn != null ? standardReturn.messageDisapproval() : null,
                standardReturn != null ? standardReturn.status() : null,
                standardReturn != null ? standardReturn.dataLog() : null
        );
    }

    private void setOrganizationTolerance(List<ToleranceEntity> tolerances, List<ToleranceReturn> updatedTolerances) {
        for (ToleranceEntity tolerance : tolerances) {
            List<OrganizationDTO> branchNames = new ArrayList<>();
            List<ContractDTO> contractNames = new ArrayList<>();
            List<ProjectDTO> projectNames = new ArrayList<>();


            if (tolerance.getBranchId() != null) {
                    List<OrganizationEntity> organizations =
                        organizationRepository.findAllById(tolerance.getBranchId());
                        for (OrganizationEntity org : organizations) {
                            if (org != null) {
                                OrganizationDTO orgDTO = new OrganizationDTO(
                                        org.getId(),
                                        org.getAcronym(),
                                        org.getCollaborator(),
                                        org.getObservation(),
                                        org.isActive(),
                                        org.getSubordinate(),
                                        org.getHierarchy(),
                                        org.getPosition(),
                                        org.getType(),
                                        org.getSubType()
                                );
                                branchNames.add(orgDTO);
                            }
                        }
            }

            if (tolerance.getContractId() != null) {
                    List<ContractEntity> contracts =
                        contractRepository.findAllById(tolerance.getContractId());
                        for (ContractEntity contract : contracts) {
                            if (contract != null) {
                                ContractDTO contractDTO = new ContractDTO(
                                        contract.getId(),
                                        contract.getName(),
                                        contract.getNickname(),
                                        contract.getOs(),
                                        contract.getType(),
                                        contract.getSubordinate(),
                                        contract.getHierarchy(),
                                        contract.getBranch()
                                );
                                contractNames.add(contractDTO);
                            }
                        }
            }

            if (tolerance.getProjectId() != null) {
                    List<ProjectEntity> projects =
                        projectRepository.findAllById(tolerance.getProjectId());
                        for (ProjectEntity project : projects) {
                            if (project != null) {
                                ProjectDTO projectDTO = new ProjectDTO(
                                        project.getId(),
                                        project.getMega(),
                                        project.getHcm(),
                                        project.getFilial(),
                                        project.getSubordinate(),
                                        project.getContract(),
                                        project.isAtivo(),
                                        project.getBranch(),
                                        project.getClass().getSimpleName()
                                );
                                projectNames.add(projectDTO);
                            }
                        }
            }
                
                ToleranceReturn updatedTolerance = new ToleranceReturn(
                        null,
                        branchNames,
                        contractNames,
                        projectNames,
                        tolerance.getValueTolerance()
                );
                updatedTolerances.add(updatedTolerance);
        }
    }

    public void updateCalibrationStandard(String id, CalibrationStandardCreate updateDto) {
        var oldCalibration =  calibrationStandardRepository.findById(id).orElseThrow(() -> new ModuleFailure("Padrão de calibração não existe."));
        CalibrationStandardEntity entity = calibrationStandardMapper.toEntity(updateDto);
        setEntities(updateDto, entity);
        updateFiles(updateDto, entity, oldCalibration);
        entity.setAutoIncrementId(oldCalibration.getAutoIncrementId());
        entity.setId(oldCalibration.getId());
        
        deleteOldRangeEntities(oldCalibration);
        
        calibrationStandardRepository.save(entity);
    }
    
    private void deleteOldToleranceEntities(CalibrationStandardEntity oldEntity) {
        if (oldEntity.getRange() != null && !oldEntity.getRange().isEmpty()) {
            for (RangeEntity range : oldEntity.getRange()) {
                if (range.getTolerance() != null && !range.getTolerance().isEmpty()) {
                    for (ToleranceEntity tolerance : range.getTolerance()) {
                        if (tolerance.getId() != null) {
                            toleranceRepository.deleteById(tolerance.getId());
                        }
                    }
                }
            }
        }
    }
    
    private void deleteOldRangeEntities(CalibrationStandardEntity oldEntity) {
        deleteOldToleranceEntities(oldEntity);
        
        if (oldEntity.getRange() != null && !oldEntity.getRange().isEmpty()) {
            for (RangeEntity range : oldEntity.getRange()) {
                if (range.getId() != null) {
                    rangeRepository.deleteById(range.getId());
                }
            }
        }
    }

    private void createFiles(CalibrationStandardCreate createDto, CalibrationStandardEntity entity) {
        if (createDto.getModels() != null) {
            List<ModelReturn> modelReturns = new ArrayList<>();

            for (ModelCreate model : createDto.getModels()) {
                AttachmentEntity manualAttachment = null;
                if (model.getManual() != null && !model.getManual().isEmpty()) {
                    List<MultipartFile> manualList = new ArrayList<>();
                    manualList.add(model.getManual());
                    List<AttachmentEntity> manualAttachments = attachmentService.createAttachmentsFromMultipartFiles(manualList,"standard/documents");
                    if (!manualAttachments.isEmpty()) {
                        manualAttachment = manualAttachments.getFirst();
                    }
                }

                List<AttachmentEntity> pictureAttachments = attachmentService.createAttachmentsFromMultipartFiles(model.getPicture(),"standard/documents");

                ModelReturn modelReturn =
                    new ModelReturn(
                        model.getModel(),
                        model.getCapacity(),
                        manualAttachment,
                        model.getLink(),
                        pictureAttachments,
                        model.getNiMega()
                    );

                modelReturns.add(modelReturn);
            }
            entity.setModel(modelReturns);
        }
    }

    private void setEntities(CalibrationStandardCreate createDto, CalibrationStandardEntity entity) {
        if (createDto.getProprietiesId() != null) {
            PropertiesEntity proprieties = propertiesRepository.findById(createDto.getProprietiesId())
                    .orElseThrow(() -> new ModuleFailure("Proprietario não existe."));
            entity.setProperties(proprieties);
        }

        if (createDto.getEquipmentId() != null) {
            EquipamentEntity equipment = equipamentRepository.findById(createDto.getEquipmentId())
                    .orElseThrow(() -> new ModuleFailure("Equipamento não existe."));
            entity.setEquipament(equipment);
        }
        
        if (createDto.getRange() != null && !createDto.getRange().isEmpty()) {
            List<RangeEntity> rangeEntities = createDto.getRange().stream()
                .map(this::createRangeEntity)
                .collect(Collectors.toList());
            entity.setRange(rangeEntities);
        }
    }

    private ToleranceEntity createToleranceEntity(ToleranceCreate toleranceCreate) {
        ToleranceEntity toleranceEntity = new ToleranceEntity();
        
        toleranceEntity.setBranchId(toleranceCreate.getBranchId());
        toleranceEntity.setContractId(toleranceCreate.getContractId());
        toleranceEntity.setProjectId(toleranceCreate.getProjectId());
        toleranceEntity.setValueTolerance(toleranceCreate.getValueTolerance());

        return toleranceEntity;
    }
    
    private RangeEntity createRangeEntity(RangeCreate rangeCreate) {
        RangeEntity rangeEntity = new RangeEntity();
        
        if (rangeCreate.getMeasureId() != null) {
            MeasuresEntity rangeMeasure = measuresRepository.findById(rangeCreate.getMeasureId())
                    .orElseThrow(() -> new ModuleFailure("Faixa não existe."));
            rangeEntity.setMeasure(rangeMeasure);
        }

        if (rangeCreate.getUnitId() != null) {
            UnitMeasuresEntity rangeMeasure = unitRepository.findById(rangeCreate.getUnitId())
                    .orElseThrow(() -> new ModuleFailure("Faixa não existe."));
            rangeEntity.setUnit(rangeMeasure);
        }

        rangeEntity.setName(rangeCreate.getName());
        rangeEntity.setAmount(rangeCreate.getAmount());
        rangeEntity.setRange1(rangeCreate.getRange1());
        rangeEntity.setRange2(rangeCreate.getRange2());
        rangeEntity.setResolution(rangeCreate.getResolution());
        rangeEntity.setReference(rangeCreate.getReference());
        
        if (rangeCreate.getTolerance() != null && !rangeCreate.getTolerance().isEmpty()) {
            List<ToleranceEntity> toleranceEntities = rangeCreate.getTolerance().stream()
                .map(this::createToleranceEntity)
                .collect(Collectors.toList());
            
            List<ToleranceEntity> savedToleranceEntities = toleranceEntities.stream()
                .map(toleranceRepository::save)
                .collect(Collectors.toList());
            
            rangeEntity.setTolerance(savedToleranceEntities);
        }
        
        return rangeRepository.save(rangeEntity);
    }
    
    private void updateFiles(CalibrationStandardCreate updateDto, CalibrationStandardEntity entity, CalibrationStandardEntity oldCalibration) {
        if (updateDto.getModels() != null) {
            List<ModelReturn> updatedModels = new ArrayList<>();
            
            List<ModelReturn> oldModels = oldCalibration.getModel();
            
            for (int i = 0; i < updateDto.getModels().size(); i++) {
                ModelCreate updateModel = updateDto.getModels().get(i);
                
                ModelReturn oldModel = (oldModels != null && i < oldModels.size()) ? oldModels.get(i) : null;
                
                ModelReturn updatedModel = createUpdatedModelWithFiles(updateModel, oldModel);
                updatedModels.add(updatedModel);
            }
            
            entity.setModel(updatedModels);
        }
    }
    
    private ModelReturn createUpdatedModelWithFiles(ModelCreate updateModel, ModelReturn oldModel) {
        AttachmentEntity manualAttachment = null;
        List<AttachmentEntity> pictureAttachments = new ArrayList<>();
        
        if (updateModel.getManual() != null) {
            if (updateModel.getManual().getOriginalFilename() != null && 
                updateModel.getManual().getOriginalFilename().equals("")) {
                manualAttachment = null; 
            } else {
                List<MultipartFile> manualList = new ArrayList<>();
                manualList.add(updateModel.getManual());
                List<AttachmentEntity> manualAttachments = attachmentService.createAttachmentsFromMultipartFiles(manualList,"standard/documents");
                manualAttachment = manualAttachments.isEmpty() ? null : manualAttachments.get(0);
            }
        } else {
            manualAttachment = (oldModel != null) ? oldModel.manual() : null;
        }
        
        List<AttachmentEntity> oldPictures = (oldModel != null && oldModel.picture() != null) ? 
            oldModel.picture() : new ArrayList<>();
        
        List<MultipartFile> newPictures = (updateModel.getPicture() != null) ? 
            updateModel.getPicture() : new ArrayList<>();
        
        int maxPictures = Math.max(oldPictures.size(), newPictures.size());
        
        for (int i = 0; i < maxPictures; i++) {
            AttachmentEntity oldPicture = (i < oldPictures.size()) ? oldPictures.get(i) : null;
            MultipartFile newPicture = (i < newPictures.size()) ? newPictures.get(i) : null;
            
            if (newPicture != null) {
                if (newPicture.getOriginalFilename() != null && 
                    newPicture.getOriginalFilename().equals("")) {
                    continue;
                } else {
                    List<MultipartFile> pictureList = new ArrayList<>();
                    pictureList.add(newPicture);
                    List<AttachmentEntity> pictureAttachmentsList = attachmentService.createAttachmentsFromMultipartFiles(pictureList,"standard/documents");
                    if (!pictureAttachmentsList.isEmpty()) {
                        pictureAttachments.add(pictureAttachmentsList.get(0));
                    }
                }
            } else {
                if (oldPicture != null) {
                    pictureAttachments.add(oldPicture);
                }
            }
        }
        
        return new ModelReturn(
            updateModel.getModel(),
            updateModel.getCapacity(),
            manualAttachment,
            updateModel.getLink(),
            pictureAttachments,
            updateModel.getNiMega()
        );
    }
}