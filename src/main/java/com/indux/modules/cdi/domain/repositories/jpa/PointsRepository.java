package com.indux.modules.cdi.domain.repositories.jpa;

import com.indux.modules.cdi.domain.entities.jpa.PointsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends JpaRepository<PointsEntity, Long> {
}
