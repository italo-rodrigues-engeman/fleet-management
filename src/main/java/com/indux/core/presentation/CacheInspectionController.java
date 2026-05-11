package com.indux.core.presentation;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache")
public class CacheInspectionController {
    private final CacheManager cacheManager;

    public CacheInspectionController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/{cacheName}/{key}")
    public ResponseEntity<?> getFromCache(
            @PathVariable String cacheName,
            @PathVariable String key
    ) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        Object value = cache.get(key, Object.class);
        return value != null
                ? ResponseEntity.ok(value)
                : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<?> evictFromCache(
            @PathVariable String cacheName,
            @PathVariable String key
    ) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        cache.evict(key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<?> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        cache.clear();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok().build();
    }
}
