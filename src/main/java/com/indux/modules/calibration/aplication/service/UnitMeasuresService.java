package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.UnitMeasures;
import com.indux.modules.calibration.domain.entities.mongo.UnitMeasuresEntity;
import com.indux.modules.calibration.domain.repository.mongo.MeasuresRepository;
import com.indux.modules.calibration.domain.repository.mongo.UnitRepository;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnitMeasuresService {
    private final UnitRepository unitRepository;
    private final GeralMapper geralMapper;
    private final MeasuresRepository measuresRepository;

    public UnitMeasuresService(UnitRepository unitRepository, GeralMapper geralMapper, MeasuresRepository measuresRepository) {
        this.unitRepository = unitRepository;
        this.geralMapper = geralMapper;
        this.measuresRepository = measuresRepository;
    }

    public void createUnitMeasures(UnitMeasures unitMeasures) {
        var entity = geralMapper.unitMeasuresEntity(unitMeasures);
        var measure = measuresRepository.findById(unitMeasures.measureId()).orElseThrow(() -> new ModuleFailure("Medida não existe."));
        entity.setMeasures(measure);
        unitRepository.save(entity);
    }


    public Page<UnitMeasures> searchUnitMeasures(Pageable pageable, Object search, Boolean status, String measureId) {
        String searchTerm = search != null ? search.toString() : null;
        Page<UnitMeasuresEntity> unitMeasures = unitRepository.findByFilters(status, measureId, searchTerm, pageable);
        List<UnitMeasures> list = geralMapper.unitMeasuresPageable(unitMeasures.getContent());
        return new PageImpl<>(list, pageable, unitMeasures.getTotalElements());
    }

    public void editUnitMeasures(String id, UnitMeasures unitMeasures) {
        unitRepository.findById(id).orElseThrow(() -> new ModuleFailure("Unidade de medida não existe."));
        var entity = geralMapper.unitMeasuresEntity(unitMeasures);
        var measure = measuresRepository.findById(unitMeasures.measureId()).orElseThrow(() -> new ModuleFailure("Medida não existe."));
        entity.setMeasures(measure);
        entity.setId(id);
        unitRepository.save(entity);
    }
}