package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.AuthResponse;
import com.indux.core.application.dto.auth.ChangePasswordDTO;
import com.indux.core.application.dto.auth.LoginRequest;
import com.indux.core.application.dto.auth.RegisterRequest;
import com.indux.core.application.dto.module.ModulePermissionInput;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.service.fixtures.Fixture;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.repository.user.UserRoleRepository;
import com.indux.core.domain.service.auth.TokenEventValidator;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.infra.config.security.TokenProvider;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.exception.user.UserBadCredentials;
import com.indux.core.infra.exception.user.UserDisabledException;
import com.indux.core.infra.exception.user.ValidatePasswordFailure;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock  private UserRepository repository;
    @Mock  private TokenProvider tokenProvider;
    @Mock  private BCryptPasswordEncoder passwordEncoder;
    @Mock  private TokenEventValidator tokenEvent;
    @Mock  private ModuleUserService module;
    @Mock  private NotificationService notification;
    @Mock  private UserRoleRepository roleRepository;

    @InjectMocks
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() throws MessagingException {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(Fixture.defaultUserRole()));
        when(repository.save(any(User.class))).thenReturn(Fixture.validUser());
        when(tokenEvent.createEvent(any(), anyString())).thenReturn(Fixture.validTokenEvent());

        RegisterRequest request = Fixture.validRegisterRequest();
        AuthResponse response = service.register(request);

        assertNotNull(response);
        verify(notification, times(1)).sendToAll(any(), anySet());
    }

    @Test
    @DisplayName("Should register with default role when role is not found")
    void shouldRegisterWithDefaultRoleWhenRoleNotFound() throws MessagingException {
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(repository.save(userCaptor.capture())).thenReturn(Fixture.validUser());
        when(tokenEvent.createEvent(any(), anyString())).thenReturn(Fixture.validTokenEvent());

        RegisterRequest request = Fixture.validRegisterRequest();
        AuthResponse response = service.register(request);

        assertNotNull(response);
        verify(repository, times(1)).save(any(User.class));
        
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getRole());
        assertEquals(1L, savedUser.getRole().getId());
        assertEquals("USUÁRIO", savedUser.getRole().getName());
        verify(notification, times(1)).sendToAll(any(), anySet());
    }

    @Test
    @DisplayName("Should register user with modules successfully")
    void shouldRegisterWithModulesSuccessfully() throws MessagingException {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(Fixture.defaultUserRole()));
        when(repository.save(any(User.class))).thenReturn(Fixture.validUser());
        when(tokenEvent.createEvent(any(), anyString())).thenReturn(Fixture.validTokenEvent());

        RegisterRequest request = new RegisterRequest(
                "123.456.789-00",
                "email@test.com",
                false,
                java.util.Set.of(new ModulePermissionInput(
                        java.util.UUID.randomUUID(), 
                        null, 
                        false, 
                        java.util.Set.of(), 
                        java.util.Set.of(), 
                        java.util.Set.of(), 
                        java.util.Set.of()
                ),
                        new ModulePermissionInput(
                                java.util.UUID.randomUUID(),
                                null,
                                false,
                                java.util.Set.of(),
                                java.util.Set.of(),
                                java.util.Set.of(),
                                java.util.Set.of()
                        )),
                "Teste Usuario",
                "81999999999",
                1L
        );
        AuthResponse response = service.register(request);

        assertNotNull(response);
        verify(module, times(1)).addPermissionsBatch(any());
        verify(notification, times(1)).sendToAll(any(), anySet());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        LoginRequest loginRequest = Fixture.validLoginRequest();
        User user = mock(User.class);

        when(repository.findByCpf(loginRequest.cpf())).thenReturn(user);
        when(user.isCorretLogin(loginRequest.password(), passwordEncoder)).thenReturn(true);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = service.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(user, times(1)).setLastLogin(any());
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw an error when login failed")
    void shouldThrowWhenLoginFails() {
        LoginRequest loginRequest = Fixture.validLoginRequest();
        when(repository.findByCpf(loginRequest.cpf())).thenReturn(null);

        assertThrows(NotFoundEmployee.class, () -> service.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw an error if password is invalid")
    void shouldThrowWhenInvalidPasswordOnLogin() {
        LoginRequest loginRequest = Fixture.validLoginRequest();
        User user = mock(User.class);

        when(repository.findByCpf(loginRequest.cpf())).thenReturn(user);
        when(user.isCorretLogin(loginRequest.password(), passwordEncoder)).thenReturn(false);

        assertThrows(UserBadCredentials.class, () -> service.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw an error if user is disabled")
    void shouldThrowWhenUserIsDisabled () {
        LoginRequest loginRequest = Fixture.validLoginRequest();
        User user = mock(User.class);

        when(repository.findByCpf(loginRequest.cpf())).thenReturn(user);
        when(user.isCorretLogin(loginRequest.password(), passwordEncoder)).thenReturn(true);
        when(user.isDisable()).thenReturn(true);

        assertThrows(UserDisabledException.class, () -> service.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw an error if password isn't equals")
    void shouldThrowWhenPasswordsDontMatchOnChange() {
        ChangePasswordDTO dto = Fixture.invalidConfirmPasswordDTO();

        assertThrows(UserBadCredentials.class, () -> service.changePassword(dto));
    }

    @Test
    @DisplayName("Should throw an error if validate failed")
    void shouldThrowWhenNewPasswordInvalid() {
        ChangePasswordDTO dto = Fixture.weakPasswordDTO();

        try (var mocked = mockStatic(AuthenticationValidator.class)) {
            mocked.when(() -> AuthenticationValidator.validatePassword(anyString()))
                    .thenReturn(List.of("Senha muito fraca"));

            assertThrows(ValidatePasswordFailure.class, () -> service.changePassword(dto));
        }
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        ChangePasswordDTO dto = Fixture.validChangePasswordDTO();
        User user = mock(User.class);

        when(repository.findByCpf(dto.cpf())).thenReturn(user);
        when(user.isCorretLogin(dto.oldPassword(), passwordEncoder)).thenReturn(true);
        when(passwordEncoder.encode(dto.newPassword())).thenReturn("hashed-password");

        GenericMessage message = service.changePassword(dto);

        assertNotNull(message);
        assertEquals("Senha alterada com sucesso", message.message());
        verify(repository, times(1)).save(user);

        verify(user, times(1)).setPassword("hashed-password");
    }

    @Test
    @DisplayName("Should error when user not found")
    void shouldThrowWhenUserNotFoundOnChangePassword() {
        ChangePasswordDTO dto = Fixture.validChangePasswordDTO();

        when(repository.findByCpf(dto.cpf())).thenReturn(null);

        assertThrows(NotFoundEmployee.class, () -> service.changePassword(dto));
    }

    @Test
    @DisplayName("Should throw an error if old password is incorrect")
    void shouldThrowWhenOldPasswordIncorrect() {
        ChangePasswordDTO dto = Fixture.validChangePasswordDTO();
        User user = mock(User.class);

        when(repository.findByCpf(dto.cpf())).thenReturn(user);
        when(user.isCorretLogin(dto.oldPassword(), passwordEncoder)).thenReturn(false);

        assertThrows(UserBadCredentials.class, () -> service.changePassword(dto));
    }
}
