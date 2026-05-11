package com.indux.modules.calibration.aplication.service;

import com.indux.modules.calibration.aplication.dtos.Measures;
import com.indux.modules.calibration.aplication.dtos.UnitMeasures;
import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import com.indux.modules.calibration.domain.entities.mongo.UnitMeasuresEntity;
import com.indux.modules.calibration.domain.repository.mongo.MeasuresRepository;
import com.indux.modules.calibration.domain.repository.mongo.UnitRepository;
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
class UnitMeasuresServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private MeasuresRepository measuresRepository;

    @Mock
    private GeralMapper geralMapper;

    @InjectMocks
    private UnitMeasuresService unitMeasuresService;

    private UnitMeasures unitMeasuresDto;
    private UnitMeasuresEntity unitMeasuresEntity;

    @BeforeEach
    void setUp() {
        unitMeasuresDto = new UnitMeasures("1", "Unidade Teste", true, "measure-id", null, "UT");
        unitMeasuresEntity = new UnitMeasuresEntity();
        unitMeasuresEntity.setId("1");
        unitMeasuresEntity.setName("Unidade Teste");
        unitMeasuresEntity.setStatus(true);
        unitMeasuresEntity.setAbbreviation("UT");
        
        MeasuresEntity measuresEntity = new MeasuresEntity();
        measuresEntity.setId("measure-id");
        measuresEntity.setName("Test Measure");
        unitMeasuresEntity.setMeasures(measuresEntity);
    }

    @Test
    @DisplayName("Should Create Unit Measures Sucessfully")
    void shouldCreateUnitMeasuresSuccessfully() {
        MeasuresEntity measuresEntity = new MeasuresEntity();
        measuresEntity.setId("measure-id");
        
        when(geralMapper.unitMeasuresEntity(any(UnitMeasures.class))).thenReturn(unitMeasuresEntity);
        when(measuresRepository.findById("measure-id")).thenReturn(Optional.of(measuresEntity));

        unitMeasuresService.createUnitMeasures(unitMeasuresDto);

        verify(geralMapper).unitMeasuresEntity(unitMeasuresDto);
        verify(measuresRepository).findById("measure-id");
        verify(unitRepository).save(unitMeasuresEntity);
    }


    @Test
    @DisplayName("Should Search Unit Measures With Measure Id Filter")
    void shouldSearchUnitMeasuresWithMeasureIdFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "teste";
        String measureId = "measure-123";
        Page<UnitMeasuresEntity> entityPage = new PageImpl<>(List.of(unitMeasuresEntity), pageable, 1);

        when(unitRepository.findByFilters(isNull(), eq(measureId), eq(searchTerm), eq(pageable))).thenReturn(entityPage);
        when(geralMapper.unitMeasuresPageable(anyList())).thenReturn(List.of(unitMeasuresDto));

        Page<UnitMeasures> result = unitMeasuresService.searchUnitMeasures(pageable, searchTerm, null, measureId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(unitRepository).findByFilters(isNull(), eq(measureId), eq(searchTerm), eq(pageable));
        verify(geralMapper).unitMeasuresPageable(anyList());
    }

    @Test
    @DisplayName("Should Edit Unit Measures Successfully")
    void shouldEditUnitMeasuresSuccessfully() {
        String id = "1";
        MeasuresEntity measuresEntity = new MeasuresEntity();
        measuresEntity.setId("measure-id");
        
        when(unitRepository.findById(id)).thenReturn(Optional.of(unitMeasuresEntity));
        when(geralMapper.unitMeasuresEntity(any(UnitMeasures.class))).thenReturn(unitMeasuresEntity);
        when(measuresRepository.findById("measure-id")).thenReturn(Optional.of(measuresEntity));

        unitMeasuresService.editUnitMeasures(id, unitMeasuresDto);

        verify(unitRepository).findById(id);
        verify(geralMapper).unitMeasuresEntity(unitMeasuresDto);
        verify(measuresRepository).findById("measure-id");
        verify(unitRepository).save(unitMeasuresEntity);
    }

    @Test
    @DisplayName("Should Throw Exception When Editting Non Existent Unit Measures")
    void shouldThrowExceptionWhenEditingNonExistentUnitMeasures() {
        // Arrange
        String id = "non-existent-id";
        when(unitRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> unitMeasuresService.editUnitMeasures(id, unitMeasuresDto));
        verify(unitRepository).findById(id);
        verify(unitRepository, never()).save(any());
    }
}