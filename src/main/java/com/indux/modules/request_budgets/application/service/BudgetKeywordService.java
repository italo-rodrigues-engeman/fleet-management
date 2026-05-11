package com.indux.modules.request_budgets.application.service;

import com.indux.modules.request_budgets.application.dto.CreateKeywordsRequest;
import com.indux.modules.request_budgets.application.dto.KeywordResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetKeyword;
import com.indux.modules.request_budgets.domain.repository.BudgetKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetKeywordService {

    private final BudgetKeywordRepository budgetKeywordRepository;

    public List<KeywordResponseDTO> addKeywords(CreateKeywordsRequest request, String userId) {
        if (request == null || request.getKeywords() == null) {
            return List.of();
        }

        // normalizar: trim, remover vazios, distinct (case-insensitive conservando forma original)
        List<String> normalized = request.getKeywords().stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (normalized.isEmpty()) {
            return List.of();
        }

        // verificar existentes
        Set<String> existing = budgetKeywordRepository.findByValueIn(normalized).stream()
                .map(BudgetKeyword::getValue)
                .collect(Collectors.toSet());

        List<BudgetKeyword> toCreate = new ArrayList<>();
        for (String value : normalized) {
            if (!existing.contains(value)) {
                toCreate.add(BudgetKeyword.builder()
                        .value(value)
                        .createdAt(LocalDateTime.now())
                        .createdBy(parseUuidOrNull(userId))
                        .build());
            }
        }

        List<BudgetKeyword> saved = toCreate.isEmpty() ? List.of() : budgetKeywordRepository.saveAll(toCreate);

        // resposta inclui também as existentes, retornando a lista completa final
        List<BudgetKeyword> all = new ArrayList<>();
        all.addAll(saved);
        if (!existing.isEmpty()) {
            all.addAll(budgetKeywordRepository.findByValueIn(existing));
        }

        return all.stream()
                .map(k -> KeywordResponseDTO.builder()
                        .id(k.getId())
                        .value(k.getValue())
                        .createdAt(k.getCreatedAt())
                        .build())
                .sorted((a, b) -> a.getValue().compareToIgnoreCase(b.getValue()))
                .collect(Collectors.toList());
    }

    public List<KeywordResponseDTO> listKeywords() {
        return budgetKeywordRepository.findAll().stream()
                .map(k -> KeywordResponseDTO.builder()
                        .id(k.getId())
                        .value(k.getValue())
                        .createdAt(k.getCreatedAt())
                        .build())
                .sorted((a, b) -> a.getValue().compareToIgnoreCase(b.getValue()))
                .collect(Collectors.toList());
    }

    private UUID parseUuidOrNull(String raw) {
        try {
            return raw == null ? null : UUID.fromString(raw);
        } catch (Exception e) {
            return null;
        }
    }
}


