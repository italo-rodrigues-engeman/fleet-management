package com.indux.core.infra.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CaffeineCacheManager cacheManager() {
        var manager = new CaffeineCacheManager("bankDetails", "contractProjects", "employees", "branches", "boardedEmployees", "platforms", "contractNames", "clientNames", "regionalNames", "positions.byNames");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .recordStats()
                        .initialCapacity(100)
                        .maximumSize(5_000)
                        .expireAfterWrite(Duration.ofDays(1))
        );
        return manager;
    }

}
