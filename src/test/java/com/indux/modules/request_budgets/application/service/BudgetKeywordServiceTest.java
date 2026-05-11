package com.indux.modules.request_budgets.application.service;

import com.indux.modules.request_budgets.application.dto.CreateKeywordsRequest;
import com.indux.modules.request_budgets.application.dto.KeywordResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetKeyword;
import com.indux.modules.request_budgets.domain.repository.BudgetKeywordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetKeywordServiceTest {

    @Mock
    private BudgetKeywordRepository repository;

    @InjectMocks
    private BudgetKeywordService service;

    @Test
    void addKeywords_returnsEmpty_whenNullOrEmpty() {
        assertThat(service.addKeywords(null, "user")).isEmpty();
        assertThat(service.addKeywords(new CreateKeywordsRequest(null), "user")).isEmpty();
        assertThat(service.addKeywords(new CreateKeywordsRequest(List.of()), "user")).isEmpty();
    }

    @Test
    void addKeywords_normalizesAndDeduplicates_andMergesWithExisting() {
        CreateKeywordsRequest req = new CreateKeywordsRequest(List.of("  Siderurgia  ", "manutenção", "Siderurgia"));

        when(repository.findByValueIn(any())).thenReturn(List.of(
                BudgetKeyword.builder().id("e1").value("manutenção").createdAt(LocalDateTime.now()).build()
        ));

        ArgumentCaptor<List<BudgetKeyword>> toSaveCaptor = ArgumentCaptor.forClass(List.class);
        when(repository.saveAll(toSaveCaptor.capture())).then(inv -> {
            List<BudgetKeyword> list = toSaveCaptor.getValue();
            // simulate saved entities with ids
            return List.of(BudgetKeyword.builder()
                    .id("n1").value(list.get(0).getValue()).createdAt(list.get(0).getCreatedAt()).build());
        });

        when(repository.findByValueIn(Set.of("manutenção"))).thenReturn(List.of(
                BudgetKeyword.builder().id("e1").value("manutenção").createdAt(LocalDateTime.now()).build()
        ));

        List<KeywordResponseDTO> result = service.addKeywords(req, "550e8400-e29b-41d4-a716-446655440000");

        // Should save only "Siderurgia" (trimmed) because "manutenção" exists; duplicates removed
        List<BudgetKeyword> saved = toSaveCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getValue()).isEqualTo("Siderurgia");

        // Result should include both existing and newly saved, sorted by value
        assertThat(result).extracting(KeywordResponseDTO::getValue).isSortedAccordingTo(String::compareToIgnoreCase);
        assertThat(result).extracting(KeywordResponseDTO::getValue).containsExactly("manutenção", "Siderurgia");
    }
}


