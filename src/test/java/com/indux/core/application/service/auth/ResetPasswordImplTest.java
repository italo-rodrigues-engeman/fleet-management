package com.indux.core.application.service.auth;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.service.fixtures.Fixture;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.service.auth.TokenEventValidator;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.exception.user.TokenEventFailure;
import com.indux.core.infra.exception.user.ValidatePasswordFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResetPasswordImplTest {
    private UserRepository repository;
    private TokenEventValidator tokenEvent;
    private BCryptPasswordEncoder encoder;
    private ResetPasswordImpl service;
    private NotificationService notification;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        tokenEvent = mock(TokenEventValidator.class);
        encoder = mock(BCryptPasswordEncoder.class);
        notification = mock(NotificationService.class);
        service = new ResetPasswordImpl(repository, tokenEvent, encoder, notification);
    }

    @Test
    @DisplayName("Should create a request to change password")
    void shouldRequestResetSuccessfully() {
        User user = Fixture.validUser();
        user.setId(UUID.randomUUID());
        when(repository.findByCpf("12345678900")).thenReturn(user);
        when(tokenEvent.createEvent(user.getId(), TokenEventType.Values.FORGET_PASSWORD.name())).thenReturn(Fixture.validTokenEvent());
        assertDoesNotThrow(() -> service.requestReset("12345678900"));

        verify(tokenEvent, times(1)).createEvent(eq(user.getId()), anyString());
    }

    @Test
    @DisplayName("Should throw an error when the CPF is invalid")
    void shouldThrowWhenCpfNotFound() {
        when(repository.findByCpf(anyString())).thenReturn(null);

        assertThrows(NotFoundEmployee.class, () -> service.requestReset("123.456.789-00"));
    }

    @Test
    @DisplayName("Should valid token successfully")
    void shouldValidateTokenSuccessfully() {
        when(tokenEvent.validateToken(123456)).thenReturn(true);

        GenericMessage message = service.validateToken(123456);

        assertNotNull(message);
        assertEquals(200, message.status());
        assertEquals("Token válido", message.message());
    }

    @Test
    @DisplayName("Should throw an error when token is invalid")
    void shouldThrowWhenTokenInvalidOnValidation() {
        when(tokenEvent.validateToken(123456)).thenReturn(false);

        assertThrows(ValidatePasswordFailure.class, () -> service.validateToken(123456));
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(tokenEvent.validateToken(123456)).thenReturn(true);
        when(tokenEvent.getUserToken(123456)).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        GenericMessage message = service.resetPassword(123456, "StrongPassword123!");

        assertNotNull(message);
        assertEquals(200, message.status());
        assertEquals("Senha alterada com sucesso.", message.message());
        verify(tokenEvent, times(1)).finalizeToken(123456);
    }

    @Test
    @DisplayName("Should throw exception if invalid token on reset")
    void shouldThrowWhenTokenInvalidOnReset() {
        when(tokenEvent.validateToken(123456)).thenReturn(false);

        assertThrows(TokenEventFailure.class, () -> service.resetPassword(123456, "StrongPassword123!"));
    }

    @Test
    @DisplayName("Should throw an error if invalid password on reset")
    void shouldThrowWhenPasswordInvalidOnReset() {
        when(tokenEvent.validateToken(123456)).thenReturn(true);

        // Simula erro de senha inválida
        try (var mocked = mockStatic(AuthenticationValidator.class)) {
            mocked.when(() -> AuthenticationValidator.validatePassword(anyString()))
                    .thenReturn(List.of("Senha muito fraca"));

            assertThrows(ValidatePasswordFailure.class, () -> service.resetPassword(123456, "123"));
        }
    }

    @Test
    @DisplayName("Should throw an error if user not found on reset")
    void shouldThrowWhenUserNotFoundOnReset() {
        UUID userId = UUID.randomUUID();
        when(tokenEvent.validateToken(123456)).thenReturn(true);
        when(tokenEvent.getUserToken(123456)).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.resetPassword(123456, "StrongPassword123!"));
    }

    @Test
    @DisplayName("Should create UserProfile on first access and set firstAccess to false")
    void shouldCreateUserProfileOnFirstAccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFirstAcess(true);

        when(tokenEvent.validateToken(123456)).thenReturn(true);
        when(tokenEvent.getUserToken(123456)).thenReturn(userId);
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        GenericMessage message = service.resetPassword(123456, "StrongPassword123!");

        assertNotNull(message);
        assertEquals(200, message.status());
        assertEquals("Senha alterada com sucesso.", message.message());

        assertFalse(user.isFirstAcess(), "firstAcess should be set to false");
        assertNotNull(user.getProfile(), "UserProfile should be created");
        assertEquals(user, user.getProfile().getUser(), "UserProfile should be linked to the correct user");

        verify(tokenEvent, times(1)).finalizeToken(123456);
    }
}