package com.indux.modules.calibration.aplication.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.calibration.aplication.dtos.*;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.calibration.domain.repository.mongo.CalibrationRepository;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepository;
import com.indux.modules.calibration.infra.mappers.CalibrationMapper;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalibrationServiceTest {

    @Mock
    private CalibrationRepository calibrationRepository;

    @Mock
    private CalibrationMapper calibrationMapper;

    @Mock
    private NiOrganizationRepository niOrganizationRepository;

    @Mock
    private GeralMapper geralMapper;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private CalibrationService calibrationService;

    private CalibrationCreate calibrationCreateDto;
    private CalibrationEntity calibrationEntity;
    private NiOrganizationEntity niOrganizationEntity;

    @BeforeEach
    void setUp() {
        List<MultipartFile> certifications = new ArrayList<>();
        List<MultipartFile> pictures = new ArrayList<>();
        List<MultipartFile> certificationStandards = new ArrayList<>();
        
        calibrationCreateDto = new CalibrationCreate();
        calibrationCreateDto.setId("calibration-id");
        calibrationCreateDto.setIdOrganization("organization-id");
        calibrationCreateDto.setCalibrationDate(LocalDate.now());
        calibrationCreateDto.setNCertification("certificate-number");
        calibrationCreateDto.setCertification(certifications);
        calibrationCreateDto.setPicture(pictures);
        calibrationCreateDto.setCertificationStandard(certificationStandards);
        
        Periodicity periodicity = new Periodicity();
        periodicity.setMonths(12);
        
        calibrationEntity = new CalibrationEntity();
        calibrationEntity.setId("calibration-id");
        calibrationEntity.setCalibrationDate(LocalDate.now());
        calibrationEntity.setNCertification("certificate-number");
        calibrationEntity.setPeriodicity(periodicity);

        niOrganizationEntity = new NiOrganizationEntity();
        niOrganizationEntity.setId("organization-id");
        niOrganizationEntity.setCalibration(new ArrayList<>());
    }

    @Test
    @DisplayName("Should Create Calibration Successfully")
    void shouldCreateCalibrationSuccessfully() {
        when(niOrganizationRepository.findById("organization-id"))
            .thenReturn(Optional.of(niOrganizationEntity));
        when(calibrationMapper.toEntity(any(CalibrationCreate.class)))
            .thenReturn(calibrationEntity);
        when(attachmentService.createAttachmentsFromMultipartFiles(anyList(), anyString()))
            .thenReturn(new ArrayList<>()); 
        when(calibrationRepository.save(any(CalibrationEntity.class)))
            .thenReturn(calibrationEntity);

        calibrationService.createCalibration(calibrationCreateDto);

        verify(niOrganizationRepository).findById("organization-id");
        verify(calibrationMapper).toEntity(calibrationCreateDto);
        verify(attachmentService, times(3)).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(calibrationRepository).save(calibrationEntity);
        verify(niOrganizationRepository).save(niOrganizationEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Organization Does Not Exist")
    void shouldThrowExceptionWhenOrganizationDoesNotExist() {
        when(niOrganizationRepository.findById("organization-id"))
            .thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> calibrationService.createCalibration(calibrationCreateDto));
        verify(niOrganizationRepository).findById("organization-id");
        verify(calibrationRepository, never()).save(any());
        verify(niOrganizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Get All Calibration")
    void shouldGetAllCalibration() {
        Pageable pageable = PageRequest.of(0, 10);
        CalibrationFilter filter = CalibrationFilter.builder().build();
        
        CalibrationStandardEntity calibrationStandard =
            new CalibrationStandardEntity();
        
        EquipamentEntity equipament =
            new EquipamentEntity();
        equipament.setId("equipament-id");
        equipament.setName("Test Equipament");
        
        calibrationStandard.setEquipament(equipament);
        
        NiOrganizationEntity niOrgWithStandard = new NiOrganizationEntity();
        niOrgWithStandard.setId("organization-id");
        niOrgWithStandard.setHeritage("Test Heritage");
        niOrgWithStandard.setCalibrationStandard(calibrationStandard);
        niOrgWithStandard.setCalibration(new ArrayList<>());
        niOrgWithStandard.setBranchIds(new ArrayList<>());
        niOrgWithStandard.setContractIds(new ArrayList<>());
        niOrgWithStandard.setProjectIds(new ArrayList<>());
        
        Page<NiOrganizationEntity> organizationPage = new PageImpl<>(
            List.of(niOrgWithStandard), pageable, 1);
            
        Equipment equipmentDto =
            new Equipment(
                "equipament-id", 
                "Test Equipament",
                true,
                null,
                null,
                null,
                null
            );
            
        CalibrationAll calibrationAll = new CalibrationAll(
            "organization-id",
            1L,
            "Test Heritage",
            equipmentDto,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            null,
            null,
            null,
            null
        );
            
        when(niOrganizationRepository.findAllFilter(any(Pageable.class), any(CalibrationFilter.class)))
            .thenReturn(organizationPage);
        when(geralMapper.equipmentDTO(any(EquipamentEntity.class))).thenReturn(equipmentDto);
       doReturn(calibrationAll).when(calibrationMapper).toCalibrationAll(
            any(NiOrganizationEntity.class),
            any(com.indux.modules.calibration.aplication.dtos.Equipment.class),
            anyList(),
            anyList(),
            anyList(),
            any(),
            any(),
            any()
        );

        Page<CalibrationAll> result = calibrationService.getAllCalibration(pageable, filter);

        assertThat(result).isNotNull();
        verify(niOrganizationRepository).findAllFilter(any(Pageable.class), any(CalibrationFilter.class));
        verify(geralMapper).equipmentDTO(any(EquipamentEntity.class));
    }

    @Test
    @DisplayName("Should Update Calibration Successfully")
    void shouldUpdateCalibrationSuccessfully() {
        String id = "calibration-id";
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.of(calibrationEntity));
        when(niOrganizationRepository.findByCalibrationId(id))
            .thenReturn(Optional.of(niOrganizationEntity));
        when(calibrationMapper.toEntity(any(CalibrationCreate.class)))
            .thenReturn(calibrationEntity);
        when(attachmentService.createAttachmentsFromMultipartFiles(anyList(), anyString()))
            .thenReturn(new ArrayList<>()); 

        calibrationService.updateCalibration(id, calibrationCreateDto);

        verify(calibrationRepository).findById(id);
        verify(niOrganizationRepository).findByCalibrationId(id);
        verify(calibrationMapper).toEntity(calibrationCreateDto);
        verify(attachmentService, times(3)).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(calibrationRepository).save(calibrationEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Updating Non Existent Calibration")
    void shouldThrowExceptionWhenUpdatingNonExistentCalibration() {
        String id = "non-existent-id";
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () ->
            calibrationService.updateCalibration(id, calibrationCreateDto));
        verify(calibrationRepository).findById(id);
        verify(calibrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Delete Calibration Successfully")
    void shouldDeleteCalibrationSuccessfully() {
        String id = "calibration-id";
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.of(calibrationEntity));

        calibrationService.deleteCalibration(id, "test-user");

        verify(calibrationRepository).findById(id);
        verify(calibrationRepository).save(calibrationEntity);
        assertThat(calibrationEntity.getDataLogs()).isNotNull();
        assertThat(calibrationEntity.getDataLogs()).hasSize(1);
        DataLog dataLog = calibrationEntity.getDataLogs().get(0);
        assertThat(dataLog.getName()).isEqualTo("test-user");
        assertThat(dataLog.getAction()).isEqualTo("Deletado");
    }

    @Test
    @DisplayName("Should Throw Exception When Deleting Non Existent Calibration")
    void shouldThrowExceptionWhenDeletingNonExistentCalibration() {
        String id = "non-existent-id";
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () ->
            calibrationService.deleteCalibration(id, "test-user"));
        verify(calibrationRepository).findById(id);
        verify(calibrationRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should Add DataLog When Deleting Calibration And Initialize List If Null")
    void shouldAddDataLogWhenDeletingCalibrationAndInitializeListIfNull() {
        String id = "calibration-id";
        calibrationEntity.setDataLogs(null);
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.of(calibrationEntity));

        calibrationService.deleteCalibration(id, "test-user");

        verify(calibrationRepository).findById(id);
        verify(calibrationRepository).save(calibrationEntity);
        assertThat(calibrationEntity.getDataLogs()).isNotNull();
        assertThat(calibrationEntity.getDataLogs()).hasSize(1);
        DataLog dataLog = calibrationEntity.getDataLogs().get(0);
        assertThat(dataLog.getName()).isEqualTo("test-user");
        assertThat(dataLog.getAction()).isEqualTo("Deletado");
        assertThat(dataLog.getDate()).isNotNull();
    }
    
    @Test
    @DisplayName("Should Update Calibration With Files")
    void shouldUpdateCalibrationWithFiles() {
        String id = "calibration-id";
        List<MultipartFile> newCertifications = new ArrayList<>();
        List<MultipartFile> newPictures = new ArrayList<>();
        List<MultipartFile> newCertificationStandards = new ArrayList<>();
        
        CalibrationCreate updateDto = new CalibrationCreate();
        updateDto.setCertification(newCertifications);
        updateDto.setPicture(newPictures);
        updateDto.setCertificationStandard(newCertificationStandards);
        
        CalibrationEntity oldCalibration = new CalibrationEntity();
        oldCalibration.setId(id);
        oldCalibration.setCertification(new ArrayList<>());
        oldCalibration.setPicture(new ArrayList<>());
        oldCalibration.setCertificationStandard(new ArrayList<>());
        
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.of(calibrationEntity));
        when(niOrganizationRepository.findByCalibrationId(id))
            .thenReturn(Optional.of(niOrganizationEntity));
        when(calibrationMapper.toEntity(any(CalibrationCreate.class)))
            .thenReturn(calibrationEntity);
        when(attachmentService.createAttachmentsFromMultipartFiles(anyList(), anyString()))
            .thenReturn(new ArrayList<>()); 

        calibrationService.updateCalibration(id, updateDto);

        verify(calibrationRepository).findById(id);
        verify(niOrganizationRepository).findByCalibrationId(id);
        verify(calibrationMapper).toEntity(updateDto);
        verify(attachmentService, times(3)).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(calibrationRepository).save(calibrationEntity);
    }
    
    @Test
    @DisplayName("Should Calculate Next Calibration Data When Updating")
    void shouldCalculateNextCalibrationDateWhenUpdating() {
        String id = "calibration-id";
        Periodicity periodicity = new Periodicity();
        periodicity.setMonths(6);
        calibrationEntity.setPeriodicity(periodicity);
        calibrationEntity.setCalibrationDate(LocalDate.of(2023, 1, 15));
        
        when(calibrationRepository.findById(id))
            .thenReturn(Optional.of(calibrationEntity));
            
        NiOrganizationEntity niOrgWithCalibration = new NiOrganizationEntity();
        niOrgWithCalibration.setId("organization-id");
        List<CalibrationEntity> calibrations = new ArrayList<>();
        calibrations.add(calibrationEntity);
        niOrgWithCalibration.setCalibration(calibrations);
        
        when(niOrganizationRepository.findByCalibrationId(id))
            .thenReturn(Optional.of(niOrgWithCalibration));
        when(calibrationMapper.toEntity(any(CalibrationCreate.class)))
            .thenReturn(calibrationEntity);
        when(attachmentService.createAttachmentsFromMultipartFiles(anyList(), anyString()))
            .thenReturn(new ArrayList<>()); 

        calibrationService.updateCalibration(id, calibrationCreateDto);

        verify(calibrationRepository).findById(id);
        verify(niOrganizationRepository).findByCalibrationId(id);
        verify(calibrationMapper).toEntity(calibrationCreateDto);
        verify(attachmentService, times(3)).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(calibrationRepository).save(calibrationEntity);
    }
}