package com.indux.core.infra.exception.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.application.dto.generic.GenericMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationException implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public CustomAuthenticationException(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        GenericMessage exception = new GenericMessage("Você precisa estar autenticado para acessar.", 401);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(exception));
    }
}
