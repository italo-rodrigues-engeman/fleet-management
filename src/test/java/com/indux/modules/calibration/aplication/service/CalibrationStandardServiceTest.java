package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardCreate;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardAll;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardReturn;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.repository.mongo.*;
import com.indux.modules.calibration.infra.mappers.CalibrationStandardMapper;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalibrationStandardServiceTest {

    @Mock
    private CalibrationStandardRepository calibrationStandardRepository;

    @Mock
    private CalibrationStandardMapper calibrationStandardMapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationContractRepository contractRepository;

    @Mock
    private OrganizationProjectRepository projectRepository;

    @Mock
    private CounterService counterService;

    @Mock
    private StorageService storageService;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private EquipamentRepository equipamentRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private MeasuresRepository measuresRepository;

    @Mock
    private PropertiesRepository propertiesRepository;

    @Mock
    private ToleranceRepository toleranceRepository;

    @Mock
    private RangeRepository rangeRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private CalibrationStandardService calibrationStandardService;

    private CalibrationStandardCreate calibrationStandardCreateDto;
    private CalibrationStandardEntity calibrationStandardEntity;
    private PropertiesEntity propertiesEntity;
    private EquipamentEntity equipamentEntity;

    @BeforeEach
    void setUp() {
        calibrationStandardCreateDto = new CalibrationStandardCreate();
        calibrationStandardCreateDto.setProprietiesId("properties-id");
        calibrationStandardCreateDto.setEquipmentId("equipment-id");
        calibrationStandardCreateDto.setModels(new ArrayList<>());
        calibrationStandardCreateDto.setRange(new ArrayList<>());
        calibrationStandardCreateDto.setMessageApproval("Approved");
        calibrationStandardCreateDto.setMessageDisapproval("Disapproved");
        calibrationStandardCreateDto.setStatus(true);
        calibrationStandardCreateDto.setDataLog(new ArrayList<>());

        calibrationStandardEntity = new CalibrationStandardEntity();
        calibrationStandardEntity.setId("standard-id");
        calibrationStandardEntity.setMessageApproval("Approved");
        calibrationStandardEntity.setMessageDisapproval("Disapproved");
        calibrationStandardEntity.setStatus(true);
        
        propertiesEntity = new PropertiesEntity();
        propertiesEntity.setId("properties-id");
        propertiesEntity.setName("Test Property");
        propertiesEntity.setStatus(true);
        
        equipamentEntity = new EquipamentEntity();
        equipamentEntity.setId("equipment-id");
        equipamentEntity.setName("Test Equipment");
        equipamentEntity.setStatus(true);
    }

    @Test
    @DisplayName("Should Create CalibrationStandard Successfully")
    void shouldCreateCalibrationStandardSuccessfully() {
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(calibrationStandardMapper.toEntity(any(CalibrationStandardCreate.class)))
            .thenReturn(calibrationStandardEntity);
        when(propertiesRepository.findById("properties-id")).thenReturn(Optional.of(propertiesEntity));
        when(equipamentRepository.findById("equipment-id")).thenReturn(Optional.of(equipamentEntity));
        when(calibrationStandardRepository.save(any(CalibrationStandardEntity.class)))
            .thenReturn(calibrationStandardEntity);

        calibrationStandardService.createCalibrationStandard(calibrationStandardCreateDto);

        verify(counterService).getNextSequence("calibration_standard_sequence");
        verify(calibrationStandardMapper).toEntity(calibrationStandardCreateDto);
        verify(propertiesRepository).findById("properties-id");
        verify(equipamentRepository).findById("equipment-id");
        verify(calibrationStandardRepository).save(calibrationStandardEntity);
    }

    @Test
    @DisplayName("Should Throw Excption When Property Not Found")
    void shouldThrowExceptionWhenPropertyNotFound() {
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(calibrationStandardMapper.toEntity(any(CalibrationStandardCreate.class)))
            .thenReturn(calibrationStandardEntity);
        when(propertiesRepository.findById("properties-id")).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () ->
            calibrationStandardService.createCalibrationStandard(calibrationStandardCreateDto));
        verify(propertiesRepository).findById("properties-id");
        verify(calibrationStandardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Throw Exception When Equipment Not Found")
    void shouldThrowExceptionWhenEquipmentNotFound() {
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(calibrationStandardMapper.toEntity(any(CalibrationStandardCreate.class)))
            .thenReturn(calibrationStandardEntity);
        when(propertiesRepository.findById("properties-id")).thenReturn(Optional.of(propertiesEntity));
        when(equipamentRepository.findById("equipment-id")).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () ->
            calibrationStandardService.createCalibrationStandard(calibrationStandardCreateDto));
        verify(propertiesRepository).findById("properties-id");
        verify(equipamentRepository).findById("equipment-id");
        verify(calibrationStandardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Create CalibrationStandardard Without Property")
    void shouldCreateCalibrationStandardWithoutProperty() {
        CalibrationStandardCreate dtoWithoutProperty = new CalibrationStandardCreate();
        dtoWithoutProperty.setEquipmentId("equipment-id");
        dtoWithoutProperty.setModels(new ArrayList<>());
        dtoWithoutProperty.setRange(new ArrayList<>());
        dtoWithoutProperty.setMessageApproval("Approved");
        dtoWithoutProperty.setMessageDisapproval("Disapproved");
        dtoWithoutProperty.setStatus(true);
        dtoWithoutProperty.setDataLog(new ArrayList<>());
        
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(calibrationStandardMapper.toEntity(any(CalibrationStandardCreate.class)))
            .thenReturn(calibrationStandardEntity);
        when(equipamentRepository.findById("equipment-id")).thenReturn(Optional.of(equipamentEntity));
        when(calibrationStandardRepository.save(any(CalibrationStandardEntity.class)))
            .thenReturn(calibrationStandardEntity);

        calibrationStandardService.createCalibrationStandard(dtoWithoutProperty);

        verify(counterService).getNextSequence("calibration_standard_sequence");
        verify(calibrationStandardMapper).toEntity(dtoWithoutProperty);
        verify(propertiesRepository, never()).findById(any());
        verify(equipamentRepository).findById("equipment-id");
        verify(calibrationStandardRepository).save(calibrationStandardEntity);
    }

    @Test
    @DisplayName("Should Create CalibrationStandard Withou Equipment")
    void shouldCreateCalibrationStandardWithoutEquipment() {
        CalibrationStandardCreate dtoWithoutEquipment = new CalibrationStandardCreate();
        dtoWithoutEquipment.setProprietiesId("properties-id");
        dtoWithoutEquipment.setModels(new ArrayList<>());
        dtoWithoutEquipment.setRange(new ArrayList<>());
        dtoWithoutEquipment.setMessageApproval("Approved");
        dtoWithoutEquipment.setMessageDisapproval("Disapproved");
        dtoWithoutEquipment.setStatus(true);
        dtoWithoutEquipment.setDataLog(new ArrayList<>());
        
        when(counterService.getNextSequence(anyString())).thenReturn(1L);
        when(calibrationStandardMapper.toEntity(any(CalibrationStandardCreate.class)))
            .thenReturn(calibrationStandardEntity);
        when(propertiesRepository.findById("properties-id")).thenReturn(Optional.of(propertiesEntity));
        when(calibrationStandardRepository.save(any(CalibrationStandardEntity.class)))
            .thenReturn(calibrationStandardEntity);

        calibrationStandardService.createCalibrationStandard(dtoWithoutEquipment);

        verify(counterService).getNextSequence("calibration_standard_sequence");
        verify(calibrationStandardMapper).toEntity(dtoWithoutEquipment);
        verify(propertiesRepository).findById("properties-id");
        verify(equipamentRepository, never()).findById(any());
        verify(calibrationStandardRepository).save(calibrationStandardEntity);
    }

    @Test
    @DisplayName("Should Get All CalibrationStandards")
    void shouldGetAllCalibrationStandards() {
        Pageable pageable = PageRequest.of(0, 10);
        CalibrationStandardFilter filter = new CalibrationStandardFilter(null, null, null, null, null, null, null, null);
        
        Page<CalibrationStandardEntity> standardPage = new PageImpl<>(
            List.of(calibrationStandardEntity), pageable, 1);
        
        List<CalibrationStandardAll> standardAllList = new ArrayList<>();
        
        when(calibrationStandardRepository.findAllFilter(any(Pageable.class), any(CalibrationStandardFilter.class)))
            .thenReturn(standardPage);
        when(calibrationStandardMapper.toDTOAll(anyList()))
            .thenReturn(standardAllList);

        Page<CalibrationStandardAll> result =
            calibrationStandardService.getAllCalibrationStandards(pageable, filter);

        assertThat(result).isNotNull();
        verify(calibrationStandardRepository).findAllFilter(any(Pageable.class), any(CalibrationStandardFilter.class));
        verify(calibrationStandardMapper).toDTOAll(anyList());
    }

    @Test
    @DisplayName("Shuold get CalibrationStandard ById Successfully")
    void shouldGetCalibrationStandardByIdSuccessfully() {
        String id = "standard-id";
        when(calibrationStandardRepository.findById(id))
            .thenReturn(Optional.of(calibrationStandardEntity));
        when(calibrationStandardMapper.toDTOReturn(any(CalibrationStandardEntity.class)))
            .thenReturn(new CalibrationStandardReturn(id, 1L, null, null, null, null, "Approved", "Disapproved", true, null));

        CalibrationStandardReturn result =
            calibrationStandardService.getCalibrationStandardsById(id);

        assertThat(result).isNotNull();
        verify(calibrationStandardRepository).findById(id);
        verify(calibrationStandardMapper).toDTOReturn(calibrationStandardEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When CalibrationStandard Not Found")
    void shouldThrowExceptionWhenCalibrationStandardNotFound() {
        String id = "non-existent-id";
        when(calibrationStandardRepository.findById(id))
            .thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () ->
            calibrationStandardService.getCalibrationStandardsById(id));
        verify(calibrationStandardRepository).findById(id);
    }
}