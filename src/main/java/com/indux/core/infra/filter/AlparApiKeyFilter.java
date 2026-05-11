package com.indux.core.infra.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.modules.alpar.application.service.AlparApiKeyService;
import com.indux.modules.alpar.persistence.model.AlparApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AlparApiKeyFilter extends OncePerRequestFilter {

    static final String CLIENT_ID_ATTRIBUTE = "alpar.clientId";
    private static final String HEADER_NAME = "X-KOGNI-KEY";
    private static final String PATH_PREFIX = "/api/funcionarios";
    private static final String ROLE_ALPAR_READ = "ROLE_ALPAR_READ";

    private final AlparApiKeyService apiKeyService;
    private final ObjectMapper objectMapper;

    public AlparApiKeyFilter(AlparApiKeyService apiKeyService, ObjectMapper objectMapper) {
        this.apiKeyService = apiKeyService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String rawKey = request.getHeader(HEADER_NAME);
        Optional<AlparApiKey> validKey = apiKeyService.validate(rawKey);

        if (validKey.isEmpty()) {
            sendUnauthorized(response);
            return;
        }

        String clientId = validKey.get().getClientId();
        request.setAttribute(CLIENT_ID_ATTRIBUTE, clientId);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                clientId,
                null,
                List.of(new SimpleGrantedAuthority(ROLE_ALPAR_READ)));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("erro", "Não autorizado", "mensagem", "Chave de API inválida ou ausente"));
    }
}
