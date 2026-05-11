package com.indux.modules.ppu.application.services.rdo.helper;

import java.time.Duration;
import java.util.Locale;

public abstract class RDODurationHelper {
    protected Duration parseDuration(String str) {
        if (str == null || str.isBlank() || "0,0".equals(str)) {
            return Duration.ZERO;
        }
        String[] parts = str.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato inválido: " + str);
        }
        long h = Long.parseLong(parts[0]);
        long m = Long.parseLong(parts[1]);
        return Duration.ofHours(h).plusMinutes(m);
    }

    protected String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) return "0,00";

        double totalMinutes = duration.toMinutes();
        double hoursDecimal = totalMinutes / 60.0;

        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", hoursDecimal);
    }
}
