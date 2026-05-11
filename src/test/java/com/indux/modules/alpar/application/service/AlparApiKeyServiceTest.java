package com.indux.modules.alpar.application.service;

import com.indux.modules.alpar.domain.ApiKeyHasher;
import com.indux.modules.alpar.persistence.model.AlparApiKey;
import com.indux.modules.alpar.persistence.repository.AlparApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlparApiKeyServiceTest {

    private static final String SECRET = "test-secret";
    private static final String RAW_KEY = "raw-key";
    private static final String HASH = ApiKeyHasher.hash(RAW_KEY, SECRET);

    @Mock
    private AlparApiKeyRepository repository;

    private AlparApiKeyService service;

    @BeforeEach
    void setUp() {
        service = new AlparApiKeyService(repository, SECRET);
    }

    @Test
    void shouldReturnEmptyWhenKeyIsNull() {
        Optional<AlparApiKey> result = service.validate(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenKeyIsBlank() {
        Optional<AlparApiKey> result = service.validate("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenHashNotFoundInRepository() {
        when(repository.findByKeyHashAndStatus(anyString(), eq("ACTIVE")))
                .thenReturn(Optional.empty());

        Optional<AlparApiKey> result = service.validate(RAW_KEY);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnKeyWhenValidAndNotExpired() {
        AlparApiKey key = AlparApiKey.builder()
                .clientId("client-a")
                .keyHash(HASH)
                .status("ACTIVE")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(repository.findByKeyHashAndStatus(HASH, "ACTIVE"))
                .thenReturn(Optional.of(key));

        Optional<AlparApiKey> result = service.validate(RAW_KEY);

        assertThat(result).isPresent();
        assertThat(result.get().getClientId()).isEqualTo("client-a");
    }

    @Test
    void shouldReturnKeyWhenValidAndNoExpiration() {
        AlparApiKey key = AlparApiKey.builder()
                .clientId("client-b")
                .keyHash(HASH)
                .status("ACTIVE")
                .expiresAt(null)
                .build();

        when(repository.findByKeyHashAndStatus(HASH, "ACTIVE"))
                .thenReturn(Optional.of(key));

        Optional<AlparApiKey> result = service.validate(RAW_KEY);

        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenKeyIsExpired() {
        AlparApiKey expiredKey = AlparApiKey.builder()
                .clientId("client-c")
                .keyHash(HASH)
                .status("ACTIVE")
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        when(repository.findByKeyHashAndStatus(HASH, "ACTIVE"))
                .thenReturn(Optional.of(expiredKey));

        Optional<AlparApiKey> result = service.validate(RAW_KEY);

        assertThat(result).isEmpty();
    }
}
