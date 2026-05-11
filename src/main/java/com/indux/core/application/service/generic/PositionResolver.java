package com.indux.core.application.service.generic;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionResolver {
    private final EmployeePositionRepository repository;

    @Cacheable(cacheNames = "positions.byNames", key = "T(java.util.Objects).hash(#names)")
    public List<EmployeePosition> resolvePositions(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        var uniq = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniq.isEmpty()) return List.of();
        var lower = uniq.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        var all = repository.findAllByNamesIgnoreCase(lower);
        var byLower = all.stream()
                .collect(Collectors.groupingBy(p -> p.getName().toLowerCase(Locale.ROOT), LinkedHashMap::new, Collectors.toList()));
        return uniq.stream()
                .map(n -> byLower.getOrDefault(n.toLowerCase(Locale.ROOT), List.<EmployeePosition>of()))
                .flatMap(Collection::stream)
                .toList();
    }
}