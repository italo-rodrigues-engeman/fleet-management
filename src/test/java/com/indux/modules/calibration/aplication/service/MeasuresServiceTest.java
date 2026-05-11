package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Measures;
import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import com.indux.modules.calibration.domain.repository.mongo.MeasuresRepository;
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
class MeasuresServiceTest {

    @Mock
    private MeasuresRepository measuresRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private MeasuresService measuresService;

    private Measures measuresDto;
    private MeasuresEntity measuresEntity;

    @BeforeEach
    void setUp() {
        measuresDto = new Measures("1", "10.5", true, "Descrição Teste");
        measuresEntity = new MeasuresEntity();
        measuresEntity.setId("1");
        measuresEntity.setName("10.5");
        measuresEntity.setStatus(true);
        measuresEntity.setDescription("Descrição Teste");
    }

    @Test
    @DisplayName("Should Create Measures Successfully")
    void shouldCreateMeasuresSuccessfully() {
        when(geralMapper.measuresEntity(any(Measures.class))).thenReturn(measuresEntity);

        measuresService.createMeasures(measuresDto);

        verify(geralMapper).measuresEntity(measuresDto);
        verify(measuresRepository).save(measuresEntity);
    }

    @Test
    @DisplayName("Should Get All Measures With Status Filter")
    void shouldGetAllMeasuresWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MeasuresEntity> entityPage = new PageImpl<>(List.of(measuresEntity), pageable, 1);
        Page<Measures> dtoPage = new PageImpl<>(List.of(measuresDto), pageable, 1);

        when(measuresRepository.findByStatus(true, pageable)).thenReturn(entityPage);
        when(geralMapper.measuresPageable(anyList())).thenReturn(List.of(measuresDto));

        Page<Measures> result = measuresService.getAllMeasures(true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("10.5");
        verify(measuresRepository).findByStatus(true, pageable);
        verify(geralMapper).measuresPageable(anyList());
    }

    @Test
    @DisplayName("Should Get All Measures Without Status Filter")
    void shouldGetAllMeasuresWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MeasuresEntity> entityPage = new PageImpl<>(List.of(measuresEntity), pageable, 1);
        Page<Measures> dtoPage = new PageImpl<>(List.of(measuresDto), pageable, 1);

        when(measuresRepository.findAll(pageable)).thenReturn(entityPage);
        when(geralMapper.measuresPageable(anyList())).thenReturn(List.of(measuresDto));

        Page<Measures> result = measuresService.getAllMeasures(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(measuresRepository).findAll(pageable);
        verify(geralMapper).measuresPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Measures With Status Filter")
    void shouldSearchMeasuresWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Page<MeasuresEntity> entityPage = new PageImpl<>(List.of(measuresEntity), pageable, 1);

        when(measuresRepository.findBySearchTermAndStatus(searchTerm, true, pageable)).thenReturn(entityPage);
        when(geralMapper.measuresPageable(anyList())).thenReturn(List.of(measuresDto));

        Page<Measures> result = measuresService.searchMeasures(pageable, searchTerm, true);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(measuresRepository).findBySearchTermAndStatus(searchTerm, true, pageable);
        verify(geralMapper).measuresPageable(anyList());
    }

    @Test
    @DisplayName("Should Search Measures Without Status Filter")
    void shouldSearchMeasuresWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        Page<MeasuresEntity> entityPage = new PageImpl<>(List.of(measuresEntity), pageable, 1);

        when(measuresRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(entityPage);
        when(geralMapper.measuresPageable(anyList())).thenReturn(List.of(measuresDto));

        Page<Measures> result = measuresService.searchMeasures(pageable, searchTerm, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(measuresRepository).findBySearchTerm(searchTerm, pageable);
        verify(geralMapper).measuresPageable(anyList());
    }

    @Test
    @DisplayName("Should Edit Measures Successfully")
    void shouldEditMeasuresSuccessfully() {
        String id = "1";
        when(measuresRepository.findById(id)).thenReturn(Optional.of(measuresEntity));
        when(geralMapper.measuresEntity(any(Measures.class))).thenReturn(measuresEntity);

        measuresService.editMeasures(id, measuresDto);

        verify(measuresRepository).findById(id);
        verify(geralMapper).measuresEntity(measuresDto);
        verify(measuresRepository).save(measuresEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Editing Non Existent Measures")
    void shouldThrowExceptionWhenEditingNonExistentMeasures() {
        String id = "non-existent-id";
        when(measuresRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleFailure.class, () -> measuresService.editMeasures(id, measuresDto));
        verify(measuresRepository).findById(id);
        verify(measuresRepository, never()).save(any());
    }
}