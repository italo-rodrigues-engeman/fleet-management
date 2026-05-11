package com.indux.modules.ppu.domain.entities.ppu;

import java.util.List;
import java.util.Optional;

public class AuditableLineResolver {

    private AuditableLineResolver() {}

    public static String resolve(List<AuditableLineConfig> configs, String platform) {
        if (configs == null || configs.isEmpty()) return null;

        if (platform != null) {
            Optional<String> specific = configs.stream()
                    .filter(c -> c.appliesTo(platform))
                    .map(AuditableLineConfig::getLabel)
                    .findFirst();
            if (specific.isPresent()) return specific.get();
        }

        return configs.stream()
                .filter(AuditableLineConfig::isDefault)
                .map(AuditableLineConfig::getLabel)
                .findFirst()
                .orElse(null);
    }

    public static String resolve(List<AuditableLineConfig> configs) {
        return resolve(configs, null);
    }
}
