package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Properties;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import com.indux.modules.calibration.domain.repository.mongo.PropertiesRepository;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertiesService {
    private final PropertiesRepository propertiesRepository;
    private final GeralMapper geralMapper;

    public PropertiesService(PropertiesRepository propertiesRepository, GeralMapper geralMapper) {
        this.propertiesRepository = propertiesRepository;
        this.geralMapper = geralMapper;
    }

    public void createProperties(Properties properties) {
        var entity = geralMapper.propertiesEntity(properties);
        propertiesRepository.save(entity);
    }

    public Page<Properties> getAllProperties(
            Boolean status,
            Pageable pageable) {
        Page<PropertiesEntity> properties;
        if(status != null){
            properties = propertiesRepository.findByStatus(status, pageable);
        }else {
            properties = propertiesRepository.findAll(pageable);
        }
        List<Properties> list = geralMapper.propertiesPageable(properties.getContent());
        return new PageImpl<>(list, pageable, properties.getTotalElements());
    }

    public Page<Properties> searchProperties(Pageable pageable, Object search, Boolean status) {
        Page<PropertiesEntity> properties;
        if (status != null) {
            properties = propertiesRepository.findBySearchTermAndStatus(search.toString(), status, pageable);
        } else {
            properties = propertiesRepository.findBySearchTerm(search.toString(), pageable);
        }
        List<Properties> list = geralMapper.propertiesPageable(properties.getContent());
        return new PageImpl<>(list, pageable, properties.getTotalElements());
    }

    public void editProperties(String id, Properties properties) {
        propertiesRepository.findById(id).orElseThrow(() -> new ModuleFailure("Propriedade não existe."));
        var entity = geralMapper.propertiesEntity(properties);
        entity.setId(id);
        propertiesRepository.save(entity);
    }
}