package com.indux.modules.ppu.domain.services;

import com.indux.modules.ppu.domain.entities.ppu.LinePPU;

import java.util.List;

public final class PlatformFilter {


    public static <L extends LinePPU> List<L> filter(List<L> line, String platform) {
        return line.stream()
            .filter(l -> {
                List<String> platforms = l.getPlatforms();
                return platforms == null || platforms.isEmpty() || platforms.contains(platform);
            })
            .toList();
    }
}
