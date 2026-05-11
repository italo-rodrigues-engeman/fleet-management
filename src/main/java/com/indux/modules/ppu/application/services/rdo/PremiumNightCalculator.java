package com.indux.modules.ppu.application.services.rdo;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Component
public class PremiumNightCalculator {

    private static final int MIN_PER_DAY = 24 * 60;
    private static final int NIGHT_START = 22 * 60;
    private static final int NIGHT_END = 5 * 60;

    public static Duration calculateNightHoursNoturno(LocalDateTime start, LocalDateTime end) {
        return calculateNightHoursNoturno(start.toLocalTime(), end.toLocalTime());
    }

    public static Duration calculateNightHoursDiurno(LocalDateTime start, LocalDateTime end) {
        return calculateNightHoursDiurno(start.toLocalTime(), end.toLocalTime());
    }

    public static Duration calculateNightHoursNoturno(LocalTime start, LocalTime end) {
        int s = toMin(start);
        int e = toMin(end);
        if (e <= s) e += MIN_PER_DAY;
        if (e <= NIGHT_START) return Duration.ZERO;
        int minutes = e - Math.max(s, NIGHT_START);
        return Duration.ofMinutes(minutes);
    }

    public static Duration calculateNightHoursExtendedFrom22(LocalTime shiftStart, LocalTime shiftEnd, Collection<TimeRange> extras) {
        final int SEC_PER_DAY = 24*3600, NIGHT_START_SEC = 22*3600;
        int base = toSec(shiftStart);
        List<int[]> ranges = new ArrayList<>();
        ranges.add(toAlignedRange(shiftStart, shiftEnd, base));
        if (extras != null) for (TimeRange tr : extras) if (tr != null && tr.start()!=null && tr.end()!=null) ranges.add(toAlignedRange(tr.start(), tr.end(), base));
        ranges.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] r : ranges) {
            if (merged.isEmpty()) merged.add(r);
            else { int[] last = merged.get(merged.size()-1); if (r[0] <= last[1]) last[1] = Math.max(last[1], r[1]); else merged.add(r); }
        }
        if (merged.isEmpty()) return Duration.ZERO;
        int nightStart = Math.max(NIGHT_START_SEC, merged.get(0)[0]);
        int lastEnd = merged.get(merged.size()-1)[1];
        long seconds = 0;
        for (int[] m : merged) {
            int s = Math.max(m[0], nightStart);
            int e = Math.min(m[1], lastEnd);
            if (e > s) seconds += (e - s);
        }
        return Duration.ofSeconds(seconds);
    }

    public static Duration calculateNightHoursDiurno(LocalTime start, LocalTime end) {
        int s = toMin(start);
        int e = toMin(end);

        if (e <= s) e += MIN_PER_DAY;

        int nightStart = NIGHT_START;
        int nightEnd = MIN_PER_DAY + NIGHT_END;

        int minutes = overlap(s, e, nightStart, nightEnd);

        return Duration.ofMinutes(minutes);
    }

    public static Duration calculateNightHoursDiurno(LocalTime shiftStart, LocalTime shiftEnd, Collection<TimeRange> extras) {
        final int SEC_PER_DAY     = 24 * 3600;
        final int NIGHT_START_SEC = 22 * 3600;
        final int NIGHT_END_SEC   = 5  * 3600;

        int base = toSec(shiftStart);
        List<int[]> ranges = new ArrayList<>();
        ranges.add(toAlignedRange(shiftStart, shiftEnd, base));

        if (extras != null) {
            for (TimeRange tr : extras) {
                if (tr == null || tr.start() == null || tr.end() == null) continue;
                ranges.add(toAlignedRange(tr.start(), tr.end(), base));
            }
        }

        ranges.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] r : ranges) {
            if (merged.isEmpty()) {
                merged.add(r);
            } else {
                int[] last = merged.get(merged.size() - 1);
                if (r[0] <= last[1]) {
                    last[1] = Math.max(last[1], r[1]);
                } else {
                    merged.add(r);
                }
            }
        }



        long seconds = 0;
        for (int[] m : merged) {
            seconds += overlapSec(m[0], m[1], NIGHT_START_SEC, SEC_PER_DAY);
            seconds += overlapSec(m[0], m[1], 0, NIGHT_END_SEC);
            seconds += overlapSec(m[0], m[1], SEC_PER_DAY, SEC_PER_DAY + NIGHT_END_SEC);
        }
        return Duration.ofSeconds(seconds);
    }

    private static int[] toAlignedRange(LocalTime sT, LocalTime eT, int base) {
        final int SEC_PER_DAY = 24 * 3600;
        int s = toSec(sT);
        int e = toSec(eT);
        if (s < base) s += SEC_PER_DAY;
        if (e < s) e += SEC_PER_DAY;
        return new int[]{s, e};
    }

    private static int toSec(LocalTime t) {
        return t.getHour() * 3600 + t.getMinute() * 60 + t.getSecond();
    }

    private static int overlapSec(int aStart, int aEnd, int bStart, int bEnd) {
        int start = Math.max(aStart, bStart);
        int end = Math.min(aEnd, bEnd);
        return Math.max(0, end - start);
    }


    public static Duration calculateNightHoursDiurno(LocalDateTime shiftStart, LocalDateTime shiftEnd, Collection<TimeRange> extras) {
        return calculateNightHoursDiurno(shiftStart.toLocalTime(), shiftEnd.toLocalTime(), extras);
    }

    public record TimeRange(LocalTime start, LocalTime end) {}

    private static int[] toRange(LocalTime sT, LocalTime eT) {
        int s = toMin(sT);
        int e = toMin(eT);
        if (e <= s) e += MIN_PER_DAY;
        return new int[]{s, e};
    }

    private static int toMin(LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private static int overlap(int aStart, int aEnd, int bStart, int bEnd) {
        int start = Math.max(aStart, bStart);
        int end = Math.min(aEnd, bEnd);
        return Math.max(0, end - start);
    }


}