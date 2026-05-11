package com.indux.core.presentation;

import com.indux.core.application.dto.auth.*;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.domain.service.auth.AuthService;
import com.indux.core.domain.service.auth.ResetPasswordService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(AuthService authService, ResetPasswordService resetPasswordService) {
        this.authService = authService;
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest body) throws MessagingException {
        AuthResponse response = authService.login(body);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest body) throws MessagingException {
        AuthResponse response = authService.register(body);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_DESENVOLVEDOR')")
    @PostMapping("/register/batch")
    public ResponseEntity<RegisterBatchResponse> registerBatch(@RequestBody RegisterBatchRequest body) {
        RegisterBatchResponse response = authService.registerBatch(body);
        return ResponseEntity.ok().body(response);
    }

    //Troca de senha para usuário logado.
    @PostMapping("/changePassword")
    public ResponseEntity<GenericMessage> changePassword(@RequestBody ChangePasswordDTO body) {
        GenericMessage response = authService.changePassword(body);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/forgetPassword/request/{cpf}")
    public ResponseEntity<GenericMessage> requestForgetPassword(@PathVariable String cpf) throws MessagingException {
        resetPasswordService.requestReset(cpf);
        return ResponseEntity.ok().body(new GenericMessage("Solicitação da senha feita com sucesso.", 200));
    }

    @PostMapping("/resetPassword/{token}")
    public ResponseEntity<GenericMessage> resetPassword(@RequestBody ChangePasswordDTO password, @PathVariable int token) throws MessagingException {
        GenericMessage response = resetPasswordService.resetPassword(token, password.newPassword());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/resetPassword/validate/{token}")
    public ResponseEntity<GenericMessage> validateToken(@PathVariable int token) throws MessagingException {
        GenericMessage response = resetPasswordService.validateToken(token);
        return ResponseEntity.ok().body(response);
    }

}
