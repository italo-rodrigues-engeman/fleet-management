package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.modules.ppu.application.services.fixtures.CompetenceFixture;
import com.indux.modules.ppu.domain.entities.mongo.ClosedCompetenceRDO;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MissingCompetenceServiceTest {
    @Mock
    private ClosedCompetenceRDORepository repository;
    @InjectMocks
    private MissingCompetenceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return the missing competence in the year.")
    void execute() {
        var competences = CompetenceFixture.fakeList;
        var totalCompetenceYear = 12;
        var result = totalCompetenceYear - competences.size();
        int currentYear = Year.now().getValue();
        var project = 10L;

        when(repository.findAllByYearAndProject(currentYear, project)).thenReturn(competences);
        var response = service.execute(project);

        assertEquals(result, response.size());
        assertFalse(response.contains(YearMonth.of(currentYear, 2)));
    }

    @Test
    @DisplayName("Should return all 12 months when no competencies exist for the year.")
    void execute_returnsAllMonthsWhenNoneExist() {
        int currentYear = Year.now().getValue();
        long project = 10L;

        when(repository.findAllByYearAndProject(eq(currentYear), eq(project)))
                .thenReturn(Collections.emptyList());

        var response = service.execute(project);

        assertEquals(12, response.size());
        IntStream.rangeClosed(1, 12)
                .mapToObj(m -> YearMonth.of(currentYear, m))
                .forEach(expected -> assertTrue(response.contains(expected)));
    }


    @Test
    @DisplayName("Should return an empty list when all 12 months already exist for the year.")
    void execute_returnsEmptyListWhenAllMonthsExist() {
        int currentYear = Year.now().getValue();
        long project = 10L;

        List<ClosedCompetenceRDO> all = IntStream.rangeClosed(1, 12)
                .mapToObj(m -> ClosedCompetenceRDO.builder().year(currentYear).month(Month.of(m)).build())
                .toList();

        when(repository.findAllByYearAndProject(eq(currentYear), eq(project))).thenReturn(all);

        var response = service.execute(project);
        assertTrue(response.isEmpty());
    }

}