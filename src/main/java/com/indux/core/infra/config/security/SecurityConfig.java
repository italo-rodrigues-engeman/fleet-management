package com.indux.core.infra.config.security;

import com.indux.core.infra.exception.user.CustomAccessDeniedHandler;
import com.indux.core.infra.exception.user.CustomAuthenticationException;
import com.indux.core.infra.filter.AlparApiKeyFilter;
import com.indux.core.infra.filter.AlparAuditFilter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;
    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;
    // private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationException authenticationException;
    private final AlparApiKeyFilter AlparApiKeyFilter;
    private final AlparAuditFilter AlparAuditFilter;

    public SecurityConfig(
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomAuthenticationException authenticationException,
            AlparApiKeyFilter AlparApiKeyFilter,
            AlparAuditFilter AlparAuditFilter) {
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationException = authenticationException;
        this.AlparApiKeyFilter = AlparApiKeyFilter;
        this.AlparAuditFilter = AlparAuditFilter;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/simple/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/mobile/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/simple/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ticket-santander/simple").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ticket-santander/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cdi/create").permitAll()
                        .requestMatchers("/api/ticket-santander/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/resetPassword/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgetPassword/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/whatsapp-media/***").permitAll()
                        .requestMatchers(HttpMethod.GET, "/whatsapp", "/whatsapp/**").permitAll()
                        .requestMatchers("/api/funcionarios/**")
                        .hasAnyRole("ALPAR_READ", "DESENVOLVEDOR", "ADMINISTRADOR")
                        .requestMatchers("/api/mobile/**").hasRole("MOBILE_ACCESS")
                        .anyRequest().hasAnyRole("ADMINISTRADOR", "USUARIO", "DESENVOLVEDOR" , "RH"))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationException))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationException))
                .addFilterBefore(AlparApiKeyFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(AlparAuditFilter, AlparApiKeyFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://192.168.0.10:3000",
                "http://187.103.40.118:1000",
                "http://187.103.40.118:3001",
                "http://app.kogni.com.br",
                "http://187.103.40.118:9601",
                "https://app.kogni.com.br",
                "http://192.168.0.185:3001"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<AlparApiKeyFilter> AlparApiKeyFilterRegistration() {
        FilterRegistrationBean<AlparApiKeyFilter> registration = new FilterRegistrationBean<>(AlparApiKeyFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AlparAuditFilter> AlparAuditFilterRegistration() {
        FilterRegistrationBean<AlparAuditFilter> registration = new FilterRegistrationBean<>(AlparAuditFilter);
        registration.setEnabled(false);
        return registration;
    }

}
