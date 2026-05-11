package com.indux.modules.ppu.domain.services.bm.audit;

import com.indux.core.domain.model.employee.Platform;
import com.indux.core.domain.repository.employee.PlatformRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class PlatformAliasResolver {

    private final PlatformRepository platformRepository;

    private final Map<Key, String> byContract = new HashMap<>();
    private final Map<String, String> global = new HashMap<>();

    public PlatformAliasResolver(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @PostConstruct
    public void init() {
        loadPlatforms();
    }

    private void loadPlatforms() {
        List<Platform> platforms = platformRepository.findAll();

        for (Platform platform : platforms) {
            String sigla = platform.getSigla();
            if (sigla == null || sigla.isBlank()) continue;

            var contractId = platform.getContract() != null ? platform.getContract().getId() : null;

            put(contractId, normalizeRawPlatform(sigla), sigla);

            String apelido = platform.getApelido();
            if (apelido != null && !apelido.isBlank()) {
                put(contractId, normalizeRawPlatform(apelido), sigla);
            }

            String nome = platform.getNomePlataforma();
            if (nome != null && !nome.isBlank()) {
                put(contractId, normalizeRawPlatform(nome), sigla);
            }
        }
    }

    private void put(Integer contractId, String alias, String sigla) {
        if (alias.isBlank()) return;

        String existingSigla = global.get(alias);
        if (existingSigla == null) {
            global.put(alias, sigla);
        } else {
            String existingCanonical = normalizeRawPlatform(existingSigla);
            String newCanonical = normalizeRawPlatform(sigla);
            if (!existingCanonical.equals(newCanonical)) {
                log.warn("Conflito de alias global detectado: alias '{}' já mapeado para sigla '{}' (canônico: '{}'), tentando mapear para '{}' (canônico: '{}'). " +
                        "Mantendo mapeamento existente (critério determinístico).", alias, existingSigla, existingCanonical, sigla, newCanonical);
            }
        }

        if (contractId != null) {
            byContract.putIfAbsent(new Key(contractId, alias), sigla);
        }
    }

    public String normalizeRawPlatform(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String s = raw.trim();

        String upper = s.toUpperCase(Locale.ROOT);
        if (upper.startsWith("PLATAFORMA ")) {
            s = s.substring("PLATAFORMA ".length()).trim();
        }

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{M}", "");
        s = s.toUpperCase(Locale.ROOT);
        s = s.replaceAll("[^A-Z0-9]", "");

        return s;
    }

    public String toSigla(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String normalized = normalizeRawPlatform(raw);
        return global.getOrDefault(normalized, normalized);
    }

    public String toSigla(String raw, Integer contractId) {
        if (raw == null || raw.isBlank()) return "";
        String normalized = normalizeRawPlatform(raw);

        if (contractId != null) {
            String v = byContract.get(new Key(contractId, normalized));
            if (v != null) return v;
        }

        return global.getOrDefault(normalized, normalized);
    }

    public String toCanonical(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String sigla = toSigla(raw);
        if (sigla.isBlank()) return "";
        return normalizeRawPlatform(sigla);
    }

    public String toCanonical(String raw, Integer contractId) {
        if (raw == null || raw.isBlank()) return "";
        String sigla = toSigla(raw, contractId);
        if (sigla.isBlank()) return "";
        return normalizeRawPlatform(sigla);
    }

    private record Key(Integer contractId, String alias) {}
}
