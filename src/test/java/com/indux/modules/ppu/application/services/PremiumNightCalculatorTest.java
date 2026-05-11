package com.indux.modules.ppu.application.services;

import com.indux.modules.ppu.application.services.rdo.PremiumNightCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Premium Night Calculator")
class PremiumNightCalculatorTest {

    @Test
    @DisplayName("Should count 12h of night bonus for a shift from 19:00 to 10:00")
    void calculateNightHoursNoturno() {
        var date = LocalDate.of(2025,9,12);
        var start = LocalDateTime.of(date ,LocalTime.of(19, 0));
        var end = LocalDateTime.of(date, LocalTime.of(10, 0));

        var response = PremiumNightCalculator.calculateNightHoursNoturno(start, end);
        assertEquals(Duration.ofHours(12), response);
    }

    @Test
    @DisplayName("Should count 2h of night bonus for a shift from 07:00 to 00:00 and 6h for 07:00 to 04:00")
    void calculateNightHoursDiurno() {
        var date = LocalDate.of(2025,9,12);
        var start = LocalDateTime.of(date ,LocalTime.of(7, 0));
        var end = LocalDateTime.of(date, LocalTime.of(0, 0));


        var response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(2), response);

        end = LocalDateTime.of(date, LocalTime.of(4,0));
        response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(6), response);
    }

    @Test
    @DisplayName("Should not count extended night bonus for a diurnal shift")
    void calculateNightHoursWithoutExtendedDiurno() {
        var date = LocalDate.of(2025,9,12);
        var start = LocalDateTime.of(date ,LocalTime.of(7, 0));
        var end = LocalDateTime.of(date, LocalTime.of(19, 0));


        var response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(0), response);

        end = LocalDateTime.of(date, LocalTime.of(0,0));
        response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(2), response);

        end = LocalDateTime.of(date, LocalTime.of(4,0));
        response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(6), response);

        end = LocalDateTime.of(date, LocalTime.of(6,0));
        response = PremiumNightCalculator.calculateNightHoursDiurno(start, end);
        assertEquals(Duration.ofHours(7), response);
    }

    @Test
    @DisplayName("Should count 2h when Daytime  07–19 + extra 19–00")
    void diurnoComExtraPosTurno() {
        var total = PremiumNightCalculator.calculateNightHoursDiurno(
                LocalTime.of(7,0), LocalTime.of(19,0),
                List.of(new PremiumNightCalculator.TimeRange(LocalTime.of(19,0), LocalTime.of(0,0)))
        );
        assertEquals(Duration.ofHours(2), total);
    }

    @Test
    @DisplayName("Should count 2h when Daytime 06–19 + extras 06–07 and 19–00")
    void diurnoComExtrasAntesEDepois() {
        var total = PremiumNightCalculator.calculateNightHoursDiurno(
                LocalTime.of(6,0), LocalTime.of(19,0),
                List.of(
                        new PremiumNightCalculator.TimeRange(LocalTime.of(6,0), LocalTime.of(7,0)),
                        new PremiumNightCalculator.TimeRange(LocalTime.of(19,0), LocalTime.of(0,0))
                )
        );
        assertEquals(Duration.ofHours(2), total);
    }

    @Test
    @DisplayName("Should be no double counting when extra overlaps the shift")
    void semDuplaContagemComSobreposicao() {
        var total = PremiumNightCalculator.calculateNightHoursDiurno(
                LocalTime.of(7,0), LocalTime.of(0,0),
                List.of(new PremiumNightCalculator.TimeRange(LocalTime.of(19,0), LocalTime.of(0,0)))
        );
        assertEquals(Duration.ofHours(2), total);
    }

}