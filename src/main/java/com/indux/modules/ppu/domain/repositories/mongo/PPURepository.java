package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.modules.ppu.application.projection.PPUPlatforms;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.PPUProjection;
import com.indux.modules.ppu.infra.persistence.mongo.PPURepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PPURepository extends MongoRepository<PPUEntity, String>, PPURepositoryCustom {
    Optional<PPUEntity> findByProjectIdAndStatus(Long projectId, DocumentStatus status);
    Optional<PPUEntity> findByContractIdAndPlatformsInAndStatus(Long contractId, String platform, DocumentStatus status);
    Optional<PPUEntity> findByProjectId(Long projectId);
     Page<PPUProjection> findAllBy(Pageable pageable);
    List<PPUEntity> findAllByStatus(DocumentStatus status);
    PPUPlatforms getByProjectId(Long projectId);
    Page<PPUEntity> findByFilters(
            Long branch,
            Integer contractId,
            String platform,
            DocumentStatus status,
            String createdBy,
            Pageable pageable);
}
