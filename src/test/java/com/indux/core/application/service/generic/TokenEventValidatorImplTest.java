package com.indux.core.application.service.generic;

import com.indux.core.domain.model.generic.TokenEvent;
import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.repository.generic.TokenEventRepository;
import com.indux.core.domain.repository.generic.TokenEventTypeRepository;
import com.indux.core.infra.exception.user.TokenEventFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenEventValidatorImplTest {
    private TokenEventRepository repository;
    private TokenEventTypeRepository typeRepository;
    private TokenEventValidatorImpl validator;

    @BeforeEach
    void setUp() {
        repository = mock(TokenEventRepository.class);
        typeRepository = mock(TokenEventTypeRepository.class);
        validator = new TokenEventValidatorImpl(repository, typeRepository);
    }

    @Test
    @DisplayName("Should create event successfully")
    void shouldCreateEventSuccessfully() {
        UUID userId = UUID.randomUUID();
        TokenEventType type = mock(TokenEventType.class);
        when(type.quantityDate()).thenReturn(Instant.now().plusSeconds(3600));
        when(typeRepository.findByName("TEST")).thenReturn(type);

        TokenEvent event = TokenEvent.builder()
                .userID(userId)
                .type(type)
                .token(123456)
                .expireAt(Instant.now().plusSeconds(3600))
                .build();

        when(repository.save(any(TokenEvent.class))).thenReturn(event);

        TokenEvent result = validator.createEvent(userId, "TEST");

        assertNotNull(result);
        assertEquals(userId, result.getUserID());
    }

    @Test
    @DisplayName("Should throw an error when type is invalid")
    void shouldThrowWhenTypeNotFound() {
        UUID userId = UUID.randomUUID();
        when(typeRepository.findByName("INVALID")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> validator.createEvent(userId, "INVALID"));
    }

    @Test
    @DisplayName("Should retry create when token already exist")
    void shouldRetryOnDuplicateToken() {
        UUID userId = UUID.randomUUID();
        TokenEventType type = mock(TokenEventType.class);
        when(type.quantityDate()).thenReturn(Instant.now().plusSeconds(3600));
        when(typeRepository.findByName("TEST")).thenReturn(type);

        when(repository.save(any(TokenEvent.class)))
                .thenThrow(new RuntimeException("duplicate constraint token"))
                .thenThrow(new RuntimeException("duplicate constraint token"))
                .thenReturn(TokenEvent.builder().userID(userId).token(123456).type(type).expireAt(Instant.now().plusSeconds(3600)).build());

        TokenEvent result = validator.createEvent(userId, "TEST");

        assertNotNull(result);
        assertEquals(userId, result.getUserID());
    }

    @Test
    @DisplayName("Should throw an error when try 5 times and not create")
    void shouldFailAfterMaxRetries() {
        UUID userId = UUID.randomUUID();
        TokenEventType type = mock(TokenEventType.class);
        when(type.quantityDate()).thenReturn(Instant.now().plusSeconds(3600));
        when(typeRepository.findByName("TEST")).thenReturn(type);

        when(repository.save(any(TokenEvent.class)))
                .thenThrow(new RuntimeException("duplicate constraint token"));

        assertThrows(RuntimeException.class, () -> validator.createEvent(userId, "TEST"));
    }

    @Test
    @DisplayName("Should validate token")
    void shouldValidateTokenSuccessfully() {
        TokenEvent event = mock(TokenEvent.class);
        when(event.getExpireAt()).thenReturn(Instant.now().plusSeconds(3600));

        when(repository.findByToken(123456)).thenReturn(Optional.of(event));

        boolean valid = validator.validateToken(123456);

        assertTrue(valid);
    }

    @Test
    @DisplayName("Should invalidate expired token")
    void shouldInvalidateExpiredToken() {
        TokenEvent event = mock(TokenEvent.class);
        when(event.getExpireAt()).thenReturn(Instant.now().minusSeconds(3600));

        when(repository.findByToken(123456)).thenReturn(Optional.of(event));

        boolean valid = validator.validateToken(123456);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Should throw an error when the token is invalid")
    void shouldThrowOnFinalizeInvalidToken() {
        when(repository.findByToken(123456)).thenReturn(Optional.empty());

        assertThrows(TokenEventFailure.class, () -> validator.finalizeToken(123456));
    }

    @Test
    @DisplayName("Should finalize and delete token successfully")
    void shouldFinalizeTokenSuccessfully() {
        TokenEvent event = mock(TokenEvent.class);
        when(repository.findByToken(123456)).thenReturn(Optional.of(event));

        validator.finalizeToken(123456);

        verify(repository, times(1)).delete(event);
    }

    @Test
    @DisplayName("Should return a user by token")
    void shouldGetUserTokenSuccessfully() {
        UUID userId = UUID.randomUUID();
        TokenEvent event = mock(TokenEvent.class);
        when(event.getUserID()).thenReturn(userId);
        when(repository.findByToken(123456)).thenReturn(Optional.of(event));

        UUID result = validator.getUserToken(123456);

        assertEquals(userId, result);
    }

    @Test
    @DisplayName("Should throw an error when the user not exist")
    void shouldThrowWhenUserTokenNotFound() {
        when(repository.findByToken(123456)).thenReturn(Optional.empty());

        assertThrows(TokenEventFailure.class, () -> validator.getUserToken(123456));
    }
}