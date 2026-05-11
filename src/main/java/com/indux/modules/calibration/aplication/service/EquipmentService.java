package com.indux.modules.calibration.aplication.service;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.calibration.aplication.dtos.Equipment;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.ManufacturerEntity;
import com.indux.modules.calibration.domain.entities.mongo.PropertiesEntity;
import com.indux.modules.calibration.domain.repository.mongo.EquipamentRepository;
import com.indux.modules.calibration.domain.repository.mongo.ManufacturerRepository;
import com.indux.modules.calibration.domain.repository.mongo.PropertiesRepository;
import com.indux.modules.calibration.infra.mappers.GeralMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {
    private final EquipamentRepository equipmentRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final PropertiesRepository propertiesRepository;
    private final GeralMapper geralMapper;

    public EquipmentService(EquipamentRepository equipmentRepository, ManufacturerRepository manufacturerRepository, PropertiesRepository propertiesRepository, GeralMapper geralMapper) {
        this.equipmentRepository = equipmentRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.propertiesRepository = propertiesRepository;
        this.geralMapper = geralMapper;
    }

    public void createEquipment(Equipment equipment) {
        var entity = geralMapper.equipmentEntity(equipment);
        if (equipment.manufacturerId() != null) {
            ManufacturerEntity manufacturer = manufacturerRepository.findById(equipment.manufacturerId())
                    .orElseThrow(() -> new ModuleFailure("Fabricante não existe."));
            entity.setManufacturer(manufacturer);
        }
        if (equipment.propertiesIds() != null && !equipment.propertiesIds().isEmpty()) {
            List<PropertiesEntity> propertiesEntities = propertiesRepository.findAllById(equipment.propertiesIds());
            entity.setProperties(propertiesEntities);
        }
        equipmentRepository.save(entity);
    }

    public Page<Equipment> getEquipments(
            Boolean status,
            String manufacturerId,
            String propertiesId,
            String search,
            Pageable pageable) {

        Page<EquipamentEntity> equipments = equipmentRepository.findByFilters(
                status, manufacturerId, propertiesId, search, pageable);
        
        List<Equipment> list = geralMapper.equipmentPageable(equipments.getContent());
        return new PageImpl<>(list, pageable, equipments.getTotalElements());
    }

    public void editEquipment(String id, Equipment equipment) {
        var entity = equipmentRepository.findById(id).orElseThrow(() -> new ModuleFailure("Equipamento não existe."));
        var updatedEntity = geralMapper.equipmentEntity(equipment);
        updatedEntity.setId(id);
        if (equipment.manufacturerId() != null) {
            ManufacturerEntity manufacturer = manufacturerRepository.findById(equipment.manufacturerId())
                    .orElseThrow(() -> new ModuleFailure("Fabricante não existe."));
            updatedEntity.setManufacturer(manufacturer);
        } else {
            updatedEntity.setManufacturer(entity.getManufacturer());
        }
        
        if (equipment.propertiesIds() != null && !equipment.propertiesIds().isEmpty()) {
            List<PropertiesEntity> propertiesEntities = propertiesRepository.findAllById(equipment.propertiesIds());
            entity.setProperties(propertiesEntities);
        } else {
            updatedEntity.setProperties(entity.getProperties());
        }
        equipmentRepository.save(updatedEntity);
    }
}