package com.indux.modules.calibration.aplication.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.calibration.aplication.dtos.*;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.calibration.domain.repository.mongo.CalibrationRepository;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepository;
import com.indux.modules.calibration.infra.mappers.CalibrationMapper;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalibrationService {

    private final CalibrationRepository calibrationRepository;
    private final CalibrationMapper calibrationMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationContractRepository contractRepository;
    private final OrganizationProjectRepository projectRepository;
    private final NiOrganizationRepository niOrganizationRepository;
    private final GeralMapper geralMapper;
    private final AttachmentService attachmentService;

    public CalibrationService(CalibrationRepository calibrationRepository, CalibrationMapper calibrationMapper, OrganizationRepository organizationRepository, OrganizationContractRepository contractRepository, OrganizationProjectRepository projectRepository, NiOrganizationRepository niOrganizationRepository, GeralMapper geralMapper1, AttachmentService attachmentService) {
        this.calibrationRepository = calibrationRepository;
        this.calibrationMapper = calibrationMapper;
        this.organizationRepository = organizationRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.niOrganizationRepository = niOrganizationRepository;
        this.geralMapper = geralMapper1;
        this.attachmentService = attachmentService;
    }


    public void createCalibration(CalibrationCreate calibrationCreate){
        var heritage =  niOrganizationRepository.findById(calibrationCreate.getIdOrganization()).orElseThrow(() -> new ModuleFailure("Patrimônio não existe."));;
        CalibrationEntity entity = calibrationMapper.toEntity(calibrationCreate);
        List<AttachmentEntity> certification = attachmentService.createAttachmentsFromMultipartFiles(calibrationCreate.getCertification(),"calibration/documents");
        List<AttachmentEntity> pictures = attachmentService.createAttachmentsFromMultipartFiles(calibrationCreate.getPicture(),"calibration/documents");
        List<AttachmentEntity> standardCertification = attachmentService.createAttachmentsFromMultipartFiles(calibrationCreate.getCertificationStandard(),"calibration/documents");
        entity.setCertification(certification);
        entity.setPicture(pictures);
        entity.setCertificationStandard(standardCertification);
        CalibrationEntity calibration = calibrationRepository.save(entity);
        saveLastCalibration(heritage, calibration);
    }

    private void saveLastCalibration(NiOrganizationEntity standard, CalibrationEntity calibration) {
        List<CalibrationEntity> listCalibration = standard.getCalibration();
        //todo: com a inicialização na entidade, não há possibilidades de ficar null.
        if(listCalibration == null){
            List<CalibrationEntity> listCalibrationEntity = new ArrayList<>();
            listCalibrationEntity.add(calibration);
            standard.setCalibration(listCalibrationEntity);
        }else{
            listCalibration.add(calibration);
            standard.setCalibration(listCalibration);
        }

        LocalDate nextCalibrationDate = calculateNextCalibrationDate(calibration);
        if (nextCalibrationDate != null) {
            standard.setNextCalibration(nextCalibrationDate);
        }

        niOrganizationRepository.save(standard);
    }

    private LocalDate calculateNextCalibrationDate(CalibrationEntity calibration) {
        if (calibration != null && calibration.getCalibrationDate() != null ) {
            Integer minMonths = calibration.getPeriodicity().getMonths();
            return calibration.getCalibrationDate().plusMonths(minMonths);
        }
        return null;
    }

    public Page<CalibrationAll> getAllCalibration(Pageable pageable, CalibrationFilter filter){
        var standardPage = niOrganizationRepository.findAllFilter(pageable, filter);
        List<CalibrationAll> calibrationAllList = new ArrayList<>();
        setCalibrationAll(standardPage, calibrationAllList);
        return new PageImpl<>(calibrationAllList, pageable, standardPage.getTotalElements());
    }

    public void updateCalibration(String id,CalibrationCreate calibrationUpdate){
        var oldCalibration = calibrationRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Calibração não encontrado"));
        var niOrganization = niOrganizationRepository.findByCalibrationId(id).orElseThrow(() -> new ModuleNotFoundFailure("Patrimônio não encontrado"));
        CalibrationEntity entity = calibrationMapper.toEntity(calibrationUpdate);
        setFile(calibrationUpdate, entity, oldCalibration);
        entity.setId(oldCalibration.getId());
        calibrationRepository.save(entity);

        setNextCalibration(id, entity, niOrganization);
    }

    private void setNextCalibration(String id, CalibrationEntity entity, NiOrganizationEntity niOrganization) {
        LocalDate nextCalibrationDate = calculateNextCalibrationDate(entity);
        if (nextCalibrationDate != null && niOrganization.getCalibration() != null && !niOrganization.getCalibration().isEmpty()) {
            CalibrationEntity lastNonDeletedCalibration = null;
            for (int i = niOrganization.getCalibration().size() - 1; i >= 0; i--) {
                CalibrationEntity calibration = niOrganization.getCalibration().get(i);
                boolean isNotDeleted = true;
                if (calibration != null && calibration.getDataLogs() != null && !calibration.getDataLogs().isEmpty()) {
                    DataLog lastDataLog = calibration.getDataLogs().getLast();
                    isNotDeleted = !("Deletado".equals(lastDataLog.getAction()));
                }
                if (isNotDeleted) {
                    lastNonDeletedCalibration = calibration;
                    break;
                }
            }
            if (lastNonDeletedCalibration != null && lastNonDeletedCalibration.getId().equals(id)) {
                niOrganization.setNextCalibration(nextCalibrationDate);
                niOrganizationRepository.save(niOrganization);
            }
        }
    }

    public void deleteCalibration(String id, Object name){
        var calibration = calibrationRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Calibração não encontrado"));
        DataLog dataLog = new DataLog();
        dataLog.setName(name.toString());
        dataLog.setDate(LocalDateTime.now());
        dataLog.setAction("Deletado");
            
        if (calibration.getDataLogs() == null) {
            calibration.setDataLogs(new java.util.ArrayList<>());
        }
        calibration.getDataLogs().add(dataLog);
        
        calibrationRepository.save(calibration);
    }

    private void setFile(CalibrationCreate calibrationUpdate, CalibrationEntity entity, CalibrationEntity oldCalibration) {
        if (calibrationUpdate.getCertification() != null) {
            List<AttachmentEntity> certification = attachmentService.createAttachmentsFromMultipartFiles(calibrationUpdate.getCertification(),"calibration/documents");
            entity.setCertification(certification);
        }else {
            entity.setCertification(oldCalibration.getCertification());
        }
        if (calibrationUpdate.getPicture() != null) {
            List<AttachmentEntity> picture = attachmentService.createAttachmentsFromMultipartFiles(calibrationUpdate.getPicture(),"calibration/documents");
            entity.setPicture(picture);
        }else {
            entity.setPicture(oldCalibration.getPicture());
        }
        if(calibrationUpdate.getCertificationStandard() != null){
            List<AttachmentEntity>  certificationStandard = attachmentService.createAttachmentsFromMultipartFiles(calibrationUpdate.getCertificationStandard(),"calibration/documents");
            entity.setCertificationStandard(certificationStandard);
        }else{
            entity.setCertificationStandard(oldCalibration.getCertificationStandard());
        }
    }

    private void setCalibrationAll(Page<NiOrganizationEntity> standardPage, List<CalibrationAll> calibrationAllList) {
        for (NiOrganizationEntity standard : standardPage.getContent()) {
            CalibrationEntity lastCalibration = null;
            if (standard.getCalibration() != null && !standard.getCalibration().isEmpty()) {
                List<CalibrationEntity> calibrations = standard.getCalibration();
                for (int i = calibrations.size() - 1; i >= 0; i--) {
                    CalibrationEntity calibration = calibrations.get(i);
                    boolean isNotDeleted = true;
                    
                    if (calibration != null && calibration.getDataLogs() != null && !calibration.getDataLogs().isEmpty()) {
                        DataLog lastDataLog = calibration.getDataLogs().getLast();
                        isNotDeleted = !("Deletado".equals(lastDataLog.getAction()));
                    }
                    
                    if (isNotDeleted) {
                        lastCalibration = calibration;
                        break;
                    }
                }
            }

            List<OrganizationDTO> branchNames = new ArrayList<>();
            List<ContractDTO> contractNames = new ArrayList<>();
            List<ProjectDTO> projectNames = new ArrayList<>();

                if (standard.getBranchIds() != null && !standard.getBranchIds().isEmpty()) {
                    List<OrganizationEntity> organizations = organizationRepository.findAllById(standard.getBranchIds());
                    branchNames = organizations.stream()
                            .map(calibrationMapper::toOrganizationDTO)
                            .collect(Collectors.toList());
                }

                if (standard.getContractIds() != null && !standard.getContractIds().isEmpty()) {
                    List<ContractEntity> contracts = contractRepository.findAllById(standard.getContractIds());
                    contractNames = contracts.stream()
                            .map(calibrationMapper::toContractDTO)
                            .collect(Collectors.toList());
                }

                if (standard.getProjectIds() != null && !standard.getProjectIds().isEmpty()) {
                    List<ProjectEntity> projects = projectRepository.findAllById(standard.getProjectIds());
                    projectNames = projects.stream()
                            .map(calibrationMapper::toProjectDTO)
                            .collect(Collectors.toList());
                }

            CalibrationAll calibrationAll = calibrationMapper.toCalibrationAll(
                    standard,
                    geralMapper.equipmentDTO(standard.getCalibrationStandard().getEquipament()),
                    branchNames,
                    contractNames,
                    projectNames,
                    lastCalibration != null ? lastCalibration.getSituation() : null,
                    lastCalibration != null ? lastCalibration.getCalibrationDate() : null,
                    lastCalibration != null ? lastCalibration.getStatus() : null
            );

            calibrationAllList.add(calibrationAll);
        }
    }

}