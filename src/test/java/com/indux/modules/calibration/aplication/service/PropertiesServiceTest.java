package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Properties;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertiesServiceTest {

    @Mock
    private PropertiesRepository propertiesRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private PropertiesService propertiesService;

    private Properties propertiesDto;
    private PropertiesEntity propertiesEntity;

    @BeforeEach
    void setUp() {
        propertiesDto = new Properties("1", "Propriedade Teste", true);
        propertiesEntity = new PropertiesEntity();
        propertiesEntity.setId("1");
        propertiesEntity.setName("Propriedade Teste");
        propertiesEntity.setStatus(true);
    }

    @Test
    @DisplayName("Should Create Properties Successfully")
    void shouldCreatePropertiesSuccessfully() {
        when(geralMapper.propertiesEntity(any(Properties.class))).thenReturn(propertiesEntity);

        propertiesService.createProperties(propertiesDto);

        verify(geralMapper).propertiesEntity(propertiesDto);
        verify(propertiesRepository).save(propertiesEntity);
    }

    @Test
    @DisplayName("Should Get All Properties With Status Filter")
    void shouldGetAllPropertiesWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PropertiesEntity> entityPage = new PageImpl<>(List.of(propertiesEntity), pageable, 1);
        Page<Properties> dtoPage = new PageImpl<>(List.of(propertiesDto), pageable, 1);

        when(propertiesRepository.findByStatus(true, pageable)).thenReturn(entityPage);
        when(geralMapper.propertiesPageable(anyList())).thenReturn(List.of(propertiesDto));

        Page<Properties> result = propertiesService.getAllProperties(true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Propriedade Teste");
        verify(propertiesRepository).findByStatus(true, pageable);
        verify(geralMapper).propertiesPageable(anyList());
    }

    @Test
    @DisplayName("Should Get All Properties Without Status filter")
    void shouldGetAllPropertiesWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PropertiesEntity> entityPage = new PageImpl<>(List.of(propertiesEntity), pageable, 1);
        Page<Properties> dtoPage = new PageImpl<>(List.of(propertiesDto), pageable, 1);

        when(propertiesRepository.findAll(pageable)).thenReturn(entityPage);
        when(geralMapper.propertiesPageable(anyList())).thenReturn(List.of(propertiesDto));

        Page<Properties> result = propertiesService.getAllProperties(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(propertiesRepository).findAll(pageable);
        verify(geralMapper).propertiesPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Properties With Status Filter")
    void shouldSearchPropertiesWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Page<PropertiesEntity> entityPage = new PageImpl<>(List.of(propertiesEntity), pageable, 1);

        when(propertiesRepository.findBySearchTermAndStatus(searchTerm, true, pageable)).thenReturn(entityPage);
        when(geralMapper.propertiesPageable(anyList())).thenReturn(List.of(propertiesDto));

        Page<Properties> result = propertiesService.searchProperties(pageable, searchTerm, true);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(propertiesRepository).findBySearchTermAndStatus(searchTerm, true, pageable);
        verify(geralMapper).propertiesPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Properties Witout Status Filter")
    void shouldSearchPropertiesWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Page<PropertiesEntity> entityPage = new PageImpl<>(List.of(propertiesEntity), pageable, 1);

        when(propertiesRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(entityPage);
        when(geralMapper.propertiesPageable(anyList())).thenReturn(List.of(propertiesDto));

        Page<Properties> result = propertiesService.searchProperties(pageable, searchTerm, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(propertiesRepository).findBySearchTerm(searchTerm, pageable);
        verify(geralMapper).propertiesPageable(anyList());
    }

    @Test
    @DisplayName("Should Edit Properties Successfully")
    void shouldEditPropertiesSuccessfully() {
        String id = "1";
        when(propertiesRepository.findById(id)).thenReturn(Optional.of(propertiesEntity));
        when(geralMapper.propertiesEntity(any(Properties.class))).thenReturn(propertiesEntity);

        propertiesService.editProperties(id, propertiesDto);

        verify(propertiesRepository).findById(id);
        verify(geralMapper).propertiesEntity(propertiesDto);
        verify(propertiesRepository).save(propertiesEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Editing Non Existent Properties")
    void shouldThrowExceptionWhenEditingNonExistentProperties() {
        String id = "non-existent-id";
        when(propertiesRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> propertiesService.editProperties(id, propertiesDto));
        verify(propertiesRepository).findById(id);
        verify(propertiesRepository, never()).save(any());
    }
}