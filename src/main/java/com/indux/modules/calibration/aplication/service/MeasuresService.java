package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Measures;
import com.indux.modules.calibration.domain.entities.mongo.MeasuresEntity;
import com.indux.modules.calibration.domain.repository.mongo.MeasuresRepository;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeasuresService {
    private final MeasuresRepository measuresRepository;
    private final GeralMapper geralMapper;

    public MeasuresService(MeasuresRepository measuresRepository, GeralMapper geralMapper) {
        this.measuresRepository = measuresRepository;
        this.geralMapper = geralMapper;
    }

    public void createMeasures(Measures measures) {
        var entity = geralMapper.measuresEntity(measures);
        measuresRepository.save(entity);
    }

    public Page<Measures> getAllMeasures(
            Boolean status,
            Pageable pageable) {
        Page<MeasuresEntity> measures;
        if(status != null){
            measures = measuresRepository.findByStatus(status, pageable);
        }else {
            measures = measuresRepository.findAll(pageable);
        }
        List<Measures> list = geralMapper.measuresPageable(measures.getContent());
        return new PageImpl<>(list, pageable, measures.getTotalElements());
    }

    public Page<Measures> searchMeasures(Pageable pageable, Object search, Boolean status) {
        Page<MeasuresEntity> measures;
        if (status != null) {
            measures = measuresRepository.findBySearchTermAndStatus(search.toString(), status, pageable);
        } else {
            measures = measuresRepository.findBySearchTerm(search.toString(), pageable);
        }
        List<Measures> list = geralMapper.measuresPageable(measures.getContent());
        return new PageImpl<>(list, pageable, measures.getTotalElements());
    }

    public void editMeasures(String id, Measures measures) {
        measuresRepository.findById(id).orElseThrow(() -> new ModuleFailure("Medida não existe."));
        var entity = geralMapper.measuresEntity(measures);
        entity.setId(id);
        measuresRepository.save(entity);
    }
}