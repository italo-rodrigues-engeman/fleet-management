package com.indux.modules.training.domain.repository;

import com.indux.modules.training.domain.entity.ClassEntity;

import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;

@NoRepositoryBean
public interface ClassRepositoryCustom {
    List<ClassEntity> findClassesFilter(List<String> trainingIds, String registration);
}