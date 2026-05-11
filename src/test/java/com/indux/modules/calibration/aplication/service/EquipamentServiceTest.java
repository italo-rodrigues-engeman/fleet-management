package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Equipment;
import com.indux.modules.calibration.aplication.dtos.Manufacturer;
import com.indux.modules.calibration.aplication.dtos.Properties;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import com.indux.modules.calibration.domain.repository.mongo.EquipamentRepository;
import com.indux.modules.calibration.domain.repository.mongo.ManufacturerRepository;
import com.indux.modules.calibration.domain.repository.mongo.PropertiesRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipamentServiceTest {

    @Mock
    private EquipamentRepository equipmentRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private PropertiesRepository propertiesRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment equipmentDto;
    private EquipamentEntity equipmentEntity;
    private ManufacturerEntity manufacturerEntity;
    private PropertiesEntity propertiesEntity;

    @BeforeEach
    void setUp() {
        Manufacturer manufacturer = new Manufacturer("manufacturer-id", "Fabricante Teste", true, "url.com");
        Properties properties = new Properties("properties-id", "Proprietário Teste", true);
        
        equipmentDto = new Equipment("1", "Equipamento Teste", true, manufacturer, "manufacturer-id", List.of(properties), List.of("properties-id"));
        
        equipmentEntity = new EquipamentEntity();
        equipmentEntity.setId("1");
        equipmentEntity.setName("Equipamento Teste");
        equipmentEntity.setStatus(true);
        
        manufacturerEntity = new ManufacturerEntity();
        manufacturerEntity.setId("manufacturer-id");
        manufacturerEntity.setName("Fabricante Teste");
        manufacturerEntity.setStatus(true);
        manufacturerEntity.setUrl("url.com");
        
        propertiesEntity = new PropertiesEntity();
        propertiesEntity.setId("properties-id");
        propertiesEntity.setName("Proprietário Teste");
        propertiesEntity.setStatus(true);
        
        equipmentEntity.setManufacturer(manufacturerEntity);
        equipmentEntity.setProperties(List.of(propertiesEntity));
    }

    @Test
    @DisplayName("Should create equipment without manufacturer successfully")
    void shouldCreateEquipmentWithoutManufacturerSuccessfully() {
        Equipment equipmentWithoutManufacturer = new Equipment("1", "Equipamento Teste", true, null, null, null, null);
        when(geralMapper.equipmentEntity(any(Equipment.class))).thenReturn(equipmentEntity);

        equipmentService.createEquipment(equipmentWithoutManufacturer);

        verify(geralMapper).equipmentEntity(equipmentWithoutManufacturer);
        verify(manufacturerRepository, never()).findById(any());
        verify(propertiesRepository, never()).findById(any());
        verify(equipmentRepository).save(equipmentEntity);
    }

    @Test
    @DisplayName("Shold Create Equipament With Manufacturer Successfully")
    void shouldCreateEquipmentWithManufacturerSuccessfully() {
        when(geralMapper.equipmentEntity(any(Equipment.class))).thenReturn(equipmentEntity);
        when(manufacturerRepository.findById("manufacturer-id")).thenReturn(Optional.of(manufacturerEntity));
        when(propertiesRepository.findAllById(List.of("properties-id"))).thenReturn(List.of(propertiesEntity));

        equipmentService.createEquipment(equipmentDto);

        verify(geralMapper).equipmentEntity(equipmentDto);
        verify(manufacturerRepository).findById("manufacturer-id");
        verify(propertiesRepository).findAllById(List.of("properties-id"));
        verify(equipmentRepository).save(equipmentEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Manufacturer Not Found")
    void shouldThrowExceptionWhenManufacturerNotFound() {
        when(geralMapper.equipmentEntity(any(Equipment.class))).thenReturn(equipmentEntity);
        when(manufacturerRepository.findById("manufacturer-id")).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> equipmentService.createEquipment(equipmentDto));
        verify(geralMapper).equipmentEntity(equipmentDto);
        verify(manufacturerRepository).findById("manufacturer-id");
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should Get All Equipament With Filters")
    void shouldGetAllEquipmentsWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Boolean status = true;
        String manufacturerId = "manufacturer-id";
        String propertiesId = "properties-id";
        String searchTerm = "teste";
        
        Page<EquipamentEntity> entityPage = new PageImpl<>(List.of(equipmentEntity), pageable, 1);

        when(equipmentRepository.findByFilters(status, manufacturerId, propertiesId, searchTerm, pageable)).thenReturn(entityPage);
        when(geralMapper.equipmentPageable(anyList())).thenReturn(List.of(equipmentDto));

        Page<Equipment> result = equipmentService.getEquipments(status, manufacturerId, propertiesId, searchTerm, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Equipamento Teste");
        verify(equipmentRepository).findByFilters(status, manufacturerId, propertiesId, searchTerm, pageable);
        verify(geralMapper).equipmentPageable(anyList());
    }

    @Test
    @DisplayName("Should Edit Equipment Successfully")
    void shouldEditEquipmentSuccessfully() {
        String id = "1";
        when(equipmentRepository.findById(id)).thenReturn(Optional.of(equipmentEntity));
        when(geralMapper.equipmentEntity(any(Equipment.class))).thenReturn(equipmentEntity);
        when(manufacturerRepository.findById("manufacturer-id")).thenReturn(Optional.of(manufacturerEntity));
        when(propertiesRepository.findAllById(List.of("properties-id"))).thenReturn(List.of(propertiesEntity));

        equipmentService.editEquipment(id, equipmentDto);

        verify(equipmentRepository).findById(id);
        verify(geralMapper).equipmentEntity(equipmentDto);
        verify(manufacturerRepository).findById("manufacturer-id");
        verify(propertiesRepository).findAllById(List.of("properties-id"));
        verify(equipmentRepository).save(equipmentEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Editing Non Existent Equipament")
    void shouldThrowExceptionWhenEditingNonExistentEquipment() {
        String id = "non-existent-id";
        when(equipmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> equipmentService.editEquipment(id, equipmentDto));
        verify(equipmentRepository).findById(id);
        verify(equipmentRepository, never()).save(any());
    }
}