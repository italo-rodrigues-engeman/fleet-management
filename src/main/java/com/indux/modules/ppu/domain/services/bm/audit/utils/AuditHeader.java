package com.indux.modules.ppu.domain.services.bm.audit.utils;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;

public class AuditHeader {

    public static Optional<ContractMismatch> findContractMismatch(
            String expectedContract,
            List<RDOEntity> rdos
    ) {
        if (expectedContract == null || expectedContract.isBlank() || rdos == null || rdos.isEmpty()) {
            return Optional.empty();
        }

        String normalizedExpected = normalize(expectedContract);

        for (RDOEntity rdo : rdos) {
            Map<String, Object> map = rdo.getContract();
            String actual = map == null ? null : String.valueOf(map.get("codeSap"));
            if (!Objects.equals(normalizedExpected, normalize(actual))) {
                return Optional.of(new ContractMismatch(
                        rdo.getId(),
                        normalizedExpected,
                        actual,
                        rdo.getDate(),
                        rdo.getPlatform()
                ));
            }
        }
        return Optional.empty();
    }

    public static Optional<PlatformMismatch> findPlatformMismatch(
            Set<String> samcPlatforms,
            Set<String> rdoPlatforms
    ) {
        samcPlatforms = samcPlatforms == null ? Set.of() : new LinkedHashSet<>(samcPlatforms);
        rdoPlatforms = rdoPlatforms == null ? Set.of() : new LinkedHashSet<>(rdoPlatforms);

        if (samcPlatforms.equals(rdoPlatforms)) {
            return Optional.empty();
        }

        var inSamcNotInRdo = new LinkedHashSet<>(samcPlatforms);
        inSamcNotInRdo.removeAll(rdoPlatforms);

        var inRdoNotInSamc = new LinkedHashSet<>(rdoPlatforms);
        inRdoNotInSamc.removeAll(samcPlatforms);

        return Optional.of(new PlatformMismatch(
                Set.copyOf(samcPlatforms),
                Set.copyOf(rdoPlatforms),
                Set.copyOf(inSamcNotInRdo),
                Set.copyOf(inRdoNotInSamc)
        ));
    }

    public static Optional<DateRangeMismatch> findDateRangeMismatch(
            LocalDate expectedStart,
            LocalDate expectedEnd,
            Collection<LocalDate> samcDates
    ) {
        if (expectedStart == null || expectedEnd == null || samcDates == null || samcDates.isEmpty()) {
            return Optional.empty();
        }

        LocalDate min = null;
        LocalDate max = null;

        for (LocalDate d : samcDates) {
            if (d == null) continue;
            if (min == null || d.isBefore(min)) min = d;
            if (max == null || d.isAfter(max)) max = d;
        }

        if (min == null || max == null) {
            return Optional.empty();
        }

        if (!min.isBefore(expectedStart) && !max.isAfter(expectedEnd)) {
            return Optional.empty();
        }

        return Optional.of(new DateRangeMismatch(expectedStart, expectedEnd, min, max));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    public static String buildPlatformMessage(Set<String> missingInRdo, Set<String> missingInSamc) {
        return getString(missingInRdo, missingInSamc);
    }

    @NotNull
    public static String getString(Set<String> missingInRdo, Set<String> missingInSamc) {
        String a = missingInRdo.isEmpty() ? "" : "Ausentes no RDO: " + String.join(", ", missingInRdo);
        String b = missingInSamc.isEmpty() ? "" : "Ausentes no SAMC: " + String.join(", ", missingInSamc);
        return ("Plataformas divergentes entre SAMC e RDO. " + a + (a.isEmpty() || b.isEmpty() ? "" : " | ") + b).trim();
    }

    public record ContractMismatch(
            String rdoId,
            String expected,
            String actual,
            LocalDate date,
            String platform
    ) {
    }

    public record PlatformMismatch(
            Set<String> samcPlatforms,
            Set<String> rdoPlatforms,
            Set<String> inSamcNotInRdo,
            Set<String> inRdoNotInSamc
    ) {
    }

    public record DateRangeMismatch(
            LocalDate expectedStart,
            LocalDate expectedEnd,
            LocalDate actualStart,
            LocalDate actualEnd
    ) {
    }


}
