package com.indux.core.presentation;

import com.indux.core.application.dto.auth.MobileLoginRequestDTO;
import com.indux.core.application.dto.auth.MobileLoginResponseDTO;
import com.indux.core.application.service.auth.MobileLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mobile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MobileAuthController {

    private final MobileLoginService mobileLoginService;

    @PostMapping("/login")
    public ResponseEntity<MobileLoginResponseDTO> login(@Valid @RequestBody MobileLoginRequestDTO request) {
        MobileLoginResponseDTO response = mobileLoginService.authenticate(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
