package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.InstituinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomInstituinRepository {
    Page<InstituinEntity> findBySearchAndTrainingIds(String search, List<String> trainingIds, Pageable pageable);
}