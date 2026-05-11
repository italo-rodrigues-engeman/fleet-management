package com.indux.modules.ppu.domain.services;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;

import java.time.Duration;
import java.util.*;


public class UpdateCheckDiff {

    public static Snapshot snapshotByRegistration(List<RDOServiceEntity> services) {
        Map<String, TimeTotals> totalsByRegistration = new HashMap<>();
        if (services == null || services.isEmpty()) return new Snapshot(totalsByRegistration);

        for (RDOServiceEntity service : services) {
            if (service == null) continue;

            String registration = normalize(service.getRegistration());
            if (registration.isEmpty()) continue;

            TimeTotals currentTotals = totalsByRegistration.getOrDefault(registration, TimeTotals.ZERO);
            TimeTotals serviceTotals = TimeTotals.of(service.getOvertimeHourTotais(), service.getNightShiftPremium());

            totalsByRegistration.put(registration, currentTotals.plus(serviceTotals));
        }

        return new Snapshot(totalsByRegistration);
    }


    public static List<Change> diff(Snapshot before, Snapshot after) {
        Map<String, TimeTotals> totalsBefore =
                before == null ? Map.of() : before.totalsByRegistration();

        Map<String, TimeTotals> totalsAfter =
                after == null ? Map.of() : after.totalsByRegistration();

        Set<String> registrations = unionKeys(totalsBefore, totalsAfter);

        List<Change> changes = new ArrayList<>();
        for (String registration : registrations) {
            TimeTotals beforeTotals = totalsBefore.getOrDefault(registration, TimeTotals.ZERO);
            TimeTotals afterTotals  = totalsAfter.getOrDefault(registration, TimeTotals.ZERO);

            TimeTotals delta = afterTotals.minus(beforeTotals);
            if (!delta.isZero()) {
                changes.add(new Change(registration, beforeTotals, afterTotals, delta));
            }
        }

        changes.sort(Comparator.comparing(Change::registration));
        return changes;
    }

    private static Set<String> unionKeys(Map<String, ?> left, Map<String, ?> right) {
        Set<String> keys = new HashSet<>();
        keys.addAll(left.keySet());
        keys.addAll(right.keySet());
        return keys;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    public record Snapshot(Map<String, TimeTotals> totalsByRegistration) {}

    public record Change(String registration, TimeTotals before, TimeTotals after, TimeTotals delta) {}

    public record TimeTotals(long overtimeMinutes, long nightMinutes) {
        static final TimeTotals ZERO = new TimeTotals(0, 0);

        static TimeTotals of(Duration overtime, Duration night) {
            return new TimeTotals(toMinutes(overtime), toMinutes(night));
        }

        TimeTotals plus(TimeTotals other) {
            return new TimeTotals(this.overtimeMinutes + other.overtimeMinutes, this.nightMinutes + other.nightMinutes);
        }

        TimeTotals minus(TimeTotals other) {
            return new TimeTotals(this.overtimeMinutes - other.overtimeMinutes, this.nightMinutes - other.nightMinutes);
        }

        boolean isZero() {
            return overtimeMinutes == 0 && nightMinutes == 0;
        }

        public Duration overtimeDuration() {
            return Duration.ofMinutes(overtimeMinutes);
        }

        public Duration nightDuration() {
            return Duration.ofMinutes(nightMinutes);
        }

        private static long toMinutes(Duration d) {
            return d == null ? 0 : d.toMinutes();
        }
    }
}
