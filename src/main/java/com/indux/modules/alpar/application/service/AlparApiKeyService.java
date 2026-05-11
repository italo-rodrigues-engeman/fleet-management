package com.indux.modules.alpar.application.service;

import com.indux.modules.alpar.domain.ApiKeyHasher;
import com.indux.modules.alpar.persistence.model.AlparApiKey;
import com.indux.modules.alpar.persistence.repository.AlparApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AlparApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(AlparApiKeyService.class);
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AlparApiKeyRepository repository;
    private final String keySecret;

    public AlparApiKeyService(
            AlparApiKeyRepository repository,
            @Value("${alpar.key.secret}") String keySecret) {
        this.repository = repository;
        this.keySecret = keySecret;
    }

    public Optional<AlparApiKey> validate(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        String hash = ApiKeyHasher.hash(rawKey, keySecret);
        return repository.findByKeyHashAndStatus(hash, STATUS_ACTIVE)
                .filter(key -> key.getExpiresAt() == null || key.getExpiresAt().isAfter(Instant.now()));
    }
}
