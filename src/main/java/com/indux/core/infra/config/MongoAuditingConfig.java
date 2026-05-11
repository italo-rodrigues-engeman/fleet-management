package com.indux.core.infra.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableMongoAuditing
@Configuration
public class MongoAuditingConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAware<>() {
            @NotNull
            @Override
            public Optional<String> getCurrentAuditor() {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                return Optional.ofNullable(auth != null ? auth.getName() : "system");
            }
        };
    }
}
