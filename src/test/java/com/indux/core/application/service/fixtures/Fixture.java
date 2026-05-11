package com.indux.core.application.service.fixtures;

import com.indux.core.application.dto.auth.ChangePasswordDTO;
import com.indux.core.application.dto.auth.LoginRequest;
import com.indux.core.application.dto.auth.RegisterRequest;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.model.generic.TokenEvent;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Fixture {
    public static RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                "123.456.789-00",
                "email@test.com",
                false,
                Set.of(),
                "Teste Usuario",
                "81999999999",
                1L
        );
    }

    public static LoginRequest validLoginRequest() {
        return new LoginRequest("123.456.789-00", "password");
    }

    public static ChangePasswordDTO invalidConfirmPasswordDTO() {
        return new ChangePasswordDTO("123.456.789-00", "oldPass", "newPass", "otherNewPass");
    }

    public static ChangePasswordDTO weakPasswordDTO() {
        return new ChangePasswordDTO("123.456.789-00", "oldPass", "123", "123");
    }

    public static ChangePasswordDTO validChangePasswordDTO() {
        return new ChangePasswordDTO("12345678900", "oldPass", "new@Pass123", "new@Pass123");
    }

    public static UserRole defaultUserRole() {
        return new UserRole(1L, "USUARIO");
    }

    public static User validUser() {
        User user = new User(
                "Teste Usuario",
                "12345678900",
                "email@test.com",
                "hashedPassword",
                false,
                defaultUserRole(),
                81999999999L
        );
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    public static User anotherValidUser() {
        User user = new User(
                "Teste Usuario1",
                "12345678901",
                "email@teste.com",
                "hashedPasswords",
                false,
                defaultUserRole(),
                819999992229L
        );
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    public static List<User> userList() {
        return List.of(validUser(), anotherValidUser());
    }

    public static TokenEvent validTokenEvent() {
        return TokenEvent.builder()
                .token(123456)
                .build();
    }
}
