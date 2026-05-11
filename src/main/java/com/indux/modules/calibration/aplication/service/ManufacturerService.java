package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Manufacturer;
import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import com.indux.modules.calibration.domain.repository.mongo.ManufacturerRepository;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManufacturerService {
    private final ManufacturerRepository manufacturerRepository;
    private final GeralMapper geralMapper;

    public ManufacturerService(ManufacturerRepository manufacturerRepository, GeralMapper geralMapper) {
        this.manufacturerRepository = manufacturerRepository;
        this.geralMapper = geralMapper;
    }

    public void createManufacturer(Manufacturer manufacturer) {
        var entity = geralMapper.manufacturerEntity(manufacturer);
        manufacturerRepository.save(entity);
    }

    public Page<Manufacturer> getAllManufacturers(
            Boolean status,
            Pageable pageable) {
        Page<ManufacturerEntity> manufacturers;
        if(status != null){
            manufacturers = manufacturerRepository.findByStatus(status, pageable);
        }else {
            manufacturers = manufacturerRepository.findAll(pageable);
        }
        List<Manufacturer> list = geralMapper.manufacturerPageable(manufacturers.getContent());
        return new PageImpl<>(list, pageable, manufacturers.getTotalElements());
    }

    public Page<Manufacturer> searchManufacturers(Pageable pageable, Object search, Boolean status) {
        Page<ManufacturerEntity> manufacturers;
        if (status != null) {
            manufacturers = manufacturerRepository.findBySearchTermAndStatus(search.toString(), status, pageable);
        } else {
            manufacturers = manufacturerRepository.findBySearchTerm(search.toString(), pageable);
        }
        List<Manufacturer> list = geralMapper.manufacturerPageable(manufacturers.getContent());
        return new PageImpl<>(list, pageable, manufacturers.getTotalElements());
    }

    public void editManufacturer(String id,Manufacturer manufacturer) {
        manufacturerRepository.findById(id).orElseThrow(() -> new ModuleFailure("Fabricante não existe."));
        var entity = geralMapper.manufacturerEntity(manufacturer);
        entity.setId(id);
        manufacturerRepository.save(entity);
    }
}