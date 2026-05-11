package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissingCompetenceService {
    private final ClosedCompetenceRDORepository repository;

    public MissingCompetenceService(ClosedCompetenceRDORepository repository) {
        this.repository = repository;
    }

    public List<YearMonth> execute(Long project) {
        int currentYear = Year.now().getValue();

        var competences = repository.findAllByYearAndProject(currentYear, project);

        Set<YearMonth> existing = competences.stream()
                .map(c -> YearMonth.of(c.getYear(), c.getMonth()))
                .collect(Collectors.toSet());

        List<YearMonth> fullYear = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> YearMonth.of(currentYear, month))
                .toList();

        return fullYear.stream()
                .filter(c -> !existing.contains(c))
                .toList();
    }


}
