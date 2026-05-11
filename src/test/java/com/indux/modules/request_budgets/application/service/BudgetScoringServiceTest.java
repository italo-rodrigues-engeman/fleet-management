package com.indux.modules.request_budgets.application.service;

import com.indux.modules.request_budgets.application.dto.BudgetScoringConfigResponseDTO;
import com.indux.modules.request_budgets.application.dto.ScoringEntryDTO;
import com.indux.modules.request_budgets.application.dto.UpsertScoringRequest;
import com.indux.modules.request_budgets.domain.model.BudgetScoringConfig;
import com.indux.modules.request_budgets.domain.repository.BudgetScoringConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetScoringServiceTest {

    @Mock
    private BudgetScoringConfigRepository repository;

    @InjectMocks
    private BudgetScoringService service;

    @Test
    void getScoring_returnsEmptyWhenNoConfig() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        BudgetScoringConfigResponseDTO dto = service.getScoring();
        assertThat(dto.getId()).isEqualTo("default");
        assertThat(dto.getKeywordScores()).isNull();
    }

    @Test
    void upsertScoring_createsOrMergesProvidedCategories_only() {
        // existing config with mercado score
        BudgetScoringConfig existing = BudgetScoringConfig.builder()
                .id("default")
                .mercadoScores(new HashMap<>(Map.of("Público", 3)))
                .build();

        when(repository.findById("default")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpsertScoringRequest req = UpsertScoringRequest.builder()
                .keywords(List.of(new ScoringEntryDTO("siderurgia", 10)))
                .mercado(List.of(new ScoringEntryDTO("Público", 5), new ScoringEntryDTO("Privado", 2)))
                .build();

        BudgetScoringConfigResponseDTO dto = service.upsertScoring(req, "550e8400-e29b-41d4-a716-446655440000");

        // keywords created
        assertThat(dto.getKeywordScores()).containsEntry("siderurgia", 10);
        // mercado merged: overwrite Público, add Privado
        assertThat(dto.getMercadoScores()).containsEntry("Público", 5).containsEntry("Privado", 2);
        // untouched categories remain null
        assertThat(dto.getSetorScores()).isNull();
    }
}


