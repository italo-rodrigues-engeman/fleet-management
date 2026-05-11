package com.indux.core.infra.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.modules.alpar.application.service.AlparApiKeyService;
import com.indux.modules.alpar.persistence.model.AlparApiKey;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlparApiKeyFilterTest {

    @Mock
    private AlparApiKeyService apiKeyService;

    @Mock
    private FilterChain filterChain;

    private AlparApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AlparApiKeyFilter(apiKeyService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipFilterForNonAlparPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/other");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(apiKeyService, never()).validate(any());
    }

    @Test
    void shouldReturn401WhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/funcionarios");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiKeyService.validate(null)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401WhenKeyIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/funcionarios");
        request.addHeader("X-KOGNI-KEY", "invalid-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(apiKeyService.validate("invalid-key")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldPopulateSecurityContextAndContinueChainWhenKeyIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/funcionarios");
        request.addHeader("X-KOGNI-KEY", "valid-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AlparApiKey validKey = AlparApiKey.builder()
                .clientId("client-x")
                .status("ACTIVE")
                .build();
        when(apiKeyService.validate("valid-key")).thenReturn(Optional.of(validKey));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isNotEqualTo(401);
        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("client-x");
        assertThat(auth.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALPAR_READ"));
    }

    @Test
    void shouldSetClientIdAttributeWhenKeyIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/funcionarios/filter");
        request.addHeader("X-KOGNI-KEY", "valid-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AlparApiKey validKey = AlparApiKey.builder()
                .clientId("client-y")
                .status("ACTIVE")
                .build();
        when(apiKeyService.validate("valid-key")).thenReturn(Optional.of(validKey));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute(AlparApiKeyFilter.CLIENT_ID_ATTRIBUTE))
                .isEqualTo("client-y");
    }
}
