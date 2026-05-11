package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Manufacturer;
import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import com.indux.modules.calibration.domain.repository.mongo.ManufacturerRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceTest {

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private ManufacturerService manufacturerService;

    private Manufacturer manufacturerDto;
    private ManufacturerEntity manufacturerEntity;

    @BeforeEach
    void setUp() {
        manufacturerDto = new Manufacturer("1", "Fabricante Teste", true, "url.com");
        manufacturerEntity = new ManufacturerEntity();
        manufacturerEntity.setId("1");
        manufacturerEntity.setName("Fabricante Teste");
        manufacturerEntity.setStatus(true);
        manufacturerEntity.setUrl("url.com");
    }

    @Test
    @DisplayName("Should Create Manufacturer Successfully")
    void shouldCreateManufacturerSuccessfully() {
        when(geralMapper.manufacturerEntity(any(Manufacturer.class))).thenReturn(manufacturerEntity);

        manufacturerService.createManufacturer(manufacturerDto);

        verify(geralMapper).manufacturerEntity(manufacturerDto);
        verify(manufacturerRepository).save(manufacturerEntity);
    }

    @Test
    @DisplayName("Should Get All Manufacturer With Status Filter")
    void shouldGetAllManufacturersWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Boolean status = true;
        Page<ManufacturerEntity> entityPage = new PageImpl<>(List.of(manufacturerEntity), pageable, 1);

        when(manufacturerRepository.findByStatus(status, pageable)).thenReturn(entityPage);
        when(geralMapper.manufacturerPageable(anyList())).thenReturn(List.of(manufacturerDto));

        Page<Manufacturer> result = manufacturerService.getAllManufacturers(status, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Fabricante Teste");
        verify(manufacturerRepository).findByStatus(status, pageable);
        verify(geralMapper).manufacturerPageable(anyList());
    }

    @Test
    @DisplayName("Should Get All Manufacturers Without Status Filter")
    void shouldGetAllManufacturersWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ManufacturerEntity> entityPage = new PageImpl<>(List.of(manufacturerEntity), pageable, 1);

        when(manufacturerRepository.findAll(pageable)).thenReturn(entityPage);
        when(geralMapper.manufacturerPageable(anyList())).thenReturn(List.of(manufacturerDto));

        Page<Manufacturer> result = manufacturerService.getAllManufacturers(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(manufacturerRepository).findAll(pageable);
        verify(geralMapper).manufacturerPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Manufactures With Status Filter")
    void shouldSearchManufacturersWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Boolean status = true;
        Page<ManufacturerEntity> entityPage = new PageImpl<>(List.of(manufacturerEntity), pageable, 1);

        when(manufacturerRepository.findBySearchTermAndStatus(searchTerm, status, pageable)).thenReturn(entityPage);
        when(geralMapper.manufacturerPageable(anyList())).thenReturn(List.of(manufacturerDto));

        Page<Manufacturer> result = manufacturerService.searchManufacturers(pageable, searchTerm, status);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(manufacturerRepository).findBySearchTermAndStatus(searchTerm, status, pageable);
        verify(geralMapper).manufacturerPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Manufactureres Without Status Filter")
    void shouldSearchManufacturersWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Page<ManufacturerEntity> entityPage = new PageImpl<>(List.of(manufacturerEntity), pageable, 1);

        when(manufacturerRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(entityPage);
        when(geralMapper.manufacturerPageable(anyList())).thenReturn(List.of(manufacturerDto));

        Page<Manufacturer> result = manufacturerService.searchManufacturers(pageable, searchTerm, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(manufacturerRepository).findBySearchTerm(searchTerm, pageable);
        verify(geralMapper).manufacturerPageable(anyList());
    }

    @Test
    @DisplayName("Should Edit Manufacturer Successfully")
    void shouldEditManufacturerSuccessfully() {
        String id = "1";
        when(manufacturerRepository.findById(id)).thenReturn(Optional.of(manufacturerEntity));
        when(geralMapper.manufacturerEntity(any(Manufacturer.class))).thenReturn(manufacturerEntity);

        manufacturerService.editManufacturer(id, manufacturerDto);

        verify(manufacturerRepository).findById(id);
        verify(geralMapper).manufacturerEntity(manufacturerDto);
        verify(manufacturerRepository).save(manufacturerEntity);
    }

    @Test
    @DisplayName("Should Thorw Exception When Editing Non Existent Manufacturer")
    void shouldThrowExceptionWhenEditingNonExistentManufacturer() {
        String id = "non-existent-id";
        when(manufacturerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> manufacturerService.editManufacturer(id, manufacturerDto));
        verify(manufacturerRepository).findById(id);
        verify(manufacturerRepository, never()).save(any());
    }
}