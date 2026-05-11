package com.indux.modules.cdi.aplication.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.cdi.aplication.dtos.PointsDTO;
import com.indux.modules.cdi.aplication.dtos.UpdatePointsDTO;
import com.indux.modules.cdi.domain.repositories.jpa.PointsRepository;
import com.indux.modules.cdi.infra.mappers.PointsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointsService {
    private final PointsRepository pointsRepository;
    private final PointsMapper pointsMapper;

    public PointsService(PointsRepository pointsRepository, PointsMapper pointsMapper) {
        this.pointsRepository = pointsRepository;
        this.pointsMapper = pointsMapper;
    }

    public List<PointsDTO> getAllPoints() {
        var  points = pointsRepository.findAll();
        return pointsMapper.toDto(points);
    }

    public void updatePoints(Long id, UpdatePointsDTO pointsDTO) {
        var points = pointsRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Pontuação não existe."));
        var newPoints = pointsMapper.toEntity(pointsDTO);
        newPoints.setId(points.getId());
        pointsRepository.save(newPoints);
    }
}
