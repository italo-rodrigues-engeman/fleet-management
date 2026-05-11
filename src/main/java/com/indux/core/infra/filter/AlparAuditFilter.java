package com.indux.core.infra.filter;

import com.indux.core.infra.config.IpResolver;
import com.indux.modules.alpar.application.service.AlparAuditService;
import com.indux.modules.alpar.persistence.model.AlparAuditLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class AlparAuditFilter extends OncePerRequestFilter {

    private static final String PATH_PREFIX = "/api/funcionarios";
    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    private final AlparAuditService auditService;

    public AlparAuditFilter(AlparAuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_HEADER))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString());
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            String clientId = Optional.ofNullable(request.getAttribute(AlparApiKeyFilter.CLIENT_ID_ATTRIBUTE))
                    .map(Object::toString)
                    .orElse("unknown");

            auditService.record(AlparAuditLog.builder()
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .method(request.getMethod())
                    .httpStatus(response.getStatus())
                    .responseTimeMs(System.currentTimeMillis() - start)
                    .clientId(clientId)
                    .ip(IpResolver.resolve(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .correlationId(correlationId)
                    .result(response.getStatus() < 400 ? "ALLOWED" : "DENIED")
                    .build());
        }
    }
}
