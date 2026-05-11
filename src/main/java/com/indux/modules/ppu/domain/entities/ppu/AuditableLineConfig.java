package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditableLineConfig {

    @JsonProperty("label")
    private String label;

    @JsonProperty("plataformas")
    private List<String> platforms;

    public boolean isDefault() {
        return platforms == null || platforms.isEmpty();
    }

    public boolean appliesTo(String platform) {
        if (platform == null || isDefault()) return false;
        return platforms.contains(platform);
    }
}
