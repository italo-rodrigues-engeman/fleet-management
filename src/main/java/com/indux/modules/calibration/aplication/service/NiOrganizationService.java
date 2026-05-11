package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.*;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import com.indux.modules.calibration.domain.entities.mongo.ToleranceEntity;
import com.indux.modules.calibration.domain.repository.mongo.CalibrationStandardRepository;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepository;
import com.indux.modules.calibration.domain.repository.mongo.PropertiesRepository;
import com.indux.modules.calibration.infra.mappers.CalibrationMapper;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class NiOrganizationService {

    private final NiOrganizationRepository niOrganizationRepository;
    private final GeralMapper geralMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationContractRepository contractRepository;
    private final OrganizationProjectRepository projectRepository;
    private final CalibrationMapper calibrationMapper;
    private final CalibrationStandardRepository calibrationStandardRepository;
    private final CalibrationStandardMapper  calibrationStandardMapper;
    private final CounterService  counterService;

    public NiOrganizationService(
            NiOrganizationRepository niOrganizationRepository,
            GeralMapper geralMapper,
            OrganizationRepository organizationRepository,
            OrganizationContractRepository contractRepository,
            OrganizationProjectRepository projectRepository,
            CalibrationStandardRepository calibrationStandardRepository,
            CalibrationMapper calibrationMapper, CalibrationStandardMapper calibrationStandardMapper, CounterService counterService) {
        this.niOrganizationRepository = niOrganizationRepository;
        this.geralMapper = geralMapper;
        this.organizationRepository = organizationRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.calibrationMapper = calibrationMapper;
        this.calibrationStandardRepository = calibrationStandardRepository;
        this.calibrationStandardMapper = calibrationStandardMapper;
        this.counterService = counterService;
    }

    public void createNiOrganization(NiOrganizationCreate createDto) {
        Long autoIncrementId = counterService.getNextSequence("calibration_organization_sequence");

        NiOrganizationEntity entity = geralMapper.niOrganizationEntity(createDto);

        if (createDto.standardId() != null) {
            CalibrationStandardEntity standard = calibrationStandardRepository.findById(createDto.standardId())
                    .orElseThrow(() -> new ModuleFailure("Modelo não existe."));
            entity.setCalibrationStandard(standard);
        }
        entity.setAssosiationDate(LocalDate.now());
        entity.setAutoIncrementId(autoIncrementId);
        niOrganizationRepository.save(entity);
    }

    public Page<NiOrganizationReturn> getAllNiOrganizations(Pageable pageable, String propriedadeId, String search) {
        Page<NiOrganizationEntity> findAll;
        if (propriedadeId == null) {
             findAll = niOrganizationRepository.findAll(pageable);
        }else{
            findAll = niOrganizationRepository.findBySearchTermAndCalibrationIdCustom(propriedadeId, search, pageable);
        }
        List<NiOrganizationReturn> list = new ArrayList<>();

        setBranchContractAndProject(findAll.getContent(), list);

        return new PageImpl<>(list, pageable, findAll.getTotalElements());
    }

    private void setBranchContractAndProject(List<NiOrganizationEntity> entities, List<NiOrganizationReturn> list) {
        for (NiOrganizationEntity entity : entities) {
            list.add(createNiOrganizationReturn(entity));
        }
    }

    private NiOrganizationReturn createNiOrganizationReturn(NiOrganizationEntity entity) {
        List<OrganizationDTO> branchNames = new ArrayList<>();
        setBranches(entity, branchNames);

        List<ContractDTO> contractNames = new ArrayList<>();
        setContracts(entity, contractNames);

        List<ProjectDTO> projectNames = new ArrayList<>();
        setProject(entity, projectNames);


        return new NiOrganizationReturn(
                entity.getId(),
                entity.getHeritage(),
                calibrationStandardMapper.toDTOReturn(entity.getCalibrationStandard()),
                branchNames,
                contractNames,
                projectNames,
                entity.getObservation(),
                null
        );
    }

    private void setBranches(NiOrganizationEntity entity, List<OrganizationDTO> branchNames) {
        if (entity.getBranchIds() != null && !entity.getBranchIds().isEmpty()) {
            List<OrganizationEntity> organizations = organizationRepository.findAllById(entity.getBranchIds());
            for (OrganizationEntity org : organizations) {
                OrganizationDTO orgDTO = calibrationMapper.toOrganizationDTO(org);
                branchNames.add(orgDTO);
            }
        }
    }

    private void setProject(NiOrganizationEntity entity, List<ProjectDTO> projectNames) {
        if (entity.getProjectIds() != null && !entity.getProjectIds().isEmpty()) {
            List<ProjectEntity> projects = projectRepository.findAllById(entity.getProjectIds());
            for (ProjectEntity project : projects) {
                ProjectDTO projectDTO = calibrationMapper.toProjectDTO(project);
                projectNames.add(projectDTO);
            }
        }
    }

    private void setContracts(NiOrganizationEntity entity, List<ContractDTO> contractNames) {
        if (entity.getContractIds() != null && !entity.getContractIds().isEmpty()) {
            List<ContractEntity> contracts = contractRepository.findAllById(entity.getContractIds());
            for (ContractEntity contract : contracts) {
                ContractDTO contractDTO = calibrationMapper.toContractDTO(contract);
                contractNames.add(contractDTO);
            }
        }
    }


    public void updateNiOrganization(String id, NiOrganizationCreate updateDto) {
        NiOrganizationEntity entity = niOrganizationRepository.findById(id)
                .orElseThrow(() -> new ModuleFailure("Relacionamento não encontrado."));

        NiOrganizationEntity updatedEntity = geralMapper.niOrganizationEntity(updateDto);
        updatedEntity.setId(entity.getId());

        if (updateDto.standardId() != null) {
            CalibrationStandardEntity standard = calibrationStandardRepository.findById(updateDto.standardId())
                    .orElseThrow(() -> new ModuleFailure("Modelo não existe."));
            updatedEntity.setCalibrationStandard(standard);
        }
        if (entity.getCalibration() != null && !entity.getCalibration().isEmpty()) {
            updatedEntity.setCalibration(entity.getCalibration());
            updatedEntity.setNextCalibration(entity.getNextCalibration());
        }
        updatedEntity.setAutoIncrementId(entity.getAutoIncrementId());
        updatedEntity.setAssosiationDate(LocalDate.now());
        niOrganizationRepository.save(updatedEntity);
    }

    public NiOrganizationReturn getHeritageById(String id) {
        var heritage = niOrganizationRepository.findById(id).orElseThrow(() -> new ModuleFailure("Patrimônio não existe."));

        setToleranceOrganograma(heritage);

        var dto = geralMapper.niOrganizationReturn(heritage);
        return dto;
    }

    private void setToleranceOrganograma(NiOrganizationEntity heritage) {
        if (heritage.getCalibrationStandard() != null &&
            heritage.getCalibrationStandard().getRange() != null) {

            for (var range : heritage.getCalibrationStandard().getRange()) {
                if (range.getTolerance() != null && !range.getTolerance().isEmpty()) {
                    for (var tolerance : range.getTolerance()) {
                        List<OrganizationDTO> branchNames = new ArrayList<>();
                        List<ContractDTO> contractNames = new ArrayList<>();
                        List<ProjectDTO> projectNames = new ArrayList<>();
                        if (tolerance.getBranchId() != null && !tolerance.getBranchId().isEmpty()) {
                            List<OrganizationEntity> organizations = organizationRepository.findAllById(tolerance.getBranchId());
                            for (OrganizationEntity org : organizations) {
                                OrganizationDTO orgDTO = calibrationMapper.toOrganizationDTO(org);
                                branchNames.add(orgDTO);
                            }
                        }

                        if (tolerance.getContractId() != null && !tolerance.getContractId().isEmpty()) {
                            List<ContractEntity> contracts = contractRepository.findAllById(tolerance.getContractId());
                            for (ContractEntity contract : contracts) {
                                ContractDTO contractDTO = calibrationMapper.toContractDTO(contract);
                                contractNames.add(contractDTO);
                            }
                        }

                        if (tolerance.getProjectId() != null && !tolerance.getProjectId().isEmpty()) {
                            if(tolerance.getProjectId().getFirst() != null && tolerance.getProjectId().getFirst() == 0){

                                ProjectDTO projectDTO = new ProjectDTO(
                                        0,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                projectNames.add(projectDTO);
                            }else {
                            List<ProjectEntity> projects = projectRepository.findAllById(tolerance.getProjectId());
                                for (ProjectEntity project : projects) {
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
                        tolerance.setBranch(branchNames);
                        tolerance.setContract(contractNames);
                        tolerance.setProject(projectNames);
                    }
                }
            }
        }
    }
}