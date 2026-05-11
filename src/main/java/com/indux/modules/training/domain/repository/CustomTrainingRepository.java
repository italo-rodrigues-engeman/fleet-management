package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.TrainingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomTrainingRepository {
    Page<TrainingEntity> findTrainingsWithFilters(List<String> filialHCMList, String search, String mandatory, String institutionId, Pageable pageable);
}