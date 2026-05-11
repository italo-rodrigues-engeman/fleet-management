package com.indux.modules.alpar.persistence.repository;

import com.indux.modules.alpar.persistence.model.AlparApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlparApiKeyRepository extends JpaRepository<AlparApiKey, UUID> {

    Optional<AlparApiKey> findByKeyHashAndStatus(String keyHash, String status);
}
