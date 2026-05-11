package com.indux.modules.request_budgets.application.service;

import com.indux.modules.request_budgets.application.dto.BudgetScoringConfigResponseDTO;
import com.indux.modules.request_budgets.application.dto.ScoringEntryDTO;
import com.indux.modules.request_budgets.application.dto.UpsertScoringRequest;
import com.indux.modules.request_budgets.domain.model.BudgetScoringConfig;
import com.indux.modules.request_budgets.domain.repository.BudgetScoringConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetScoringService {

    private static final String DEFAULT_ID = "default";

    private final BudgetScoringConfigRepository scoringRepository;

    public BudgetScoringConfigResponseDTO upsertScoring(UpsertScoringRequest request, String userId) {
        BudgetScoringConfig config = scoringRepository.findById(DEFAULT_ID)
                .orElseGet(() -> BudgetScoringConfig.builder()
                        .id(DEFAULT_ID)
                        .createdAt(LocalDateTime.now())
                        .createdBy(parseUuidOrNull(userId))
                        .build());

        // Mescla por categoria apenas se enviada (merge/overwrite por key)
        if (request.getKeywords() != null) {
            config.setKeywordScores(merge(config.getKeywordScores(), toMap(request.getKeywords())));
        }
        if (request.getMercado() != null) {
            config.setMercadoScores(merge(config.getMercadoScores(), toMap(request.getMercado())));
        }
        if (request.getSetor() != null) {
            config.setSetorScores(merge(config.getSetorScores(), toMap(request.getSetor())));
        }
        if (request.getTempoContrato() != null) {
            config.setTempoContratoScores(merge(config.getTempoContratoScores(), toMap(request.getTempoContrato())));
        }
        if (request.getPorteEstimado() != null) {
            config.setPorteEstimadoScores(merge(config.getPorteEstimadoScores(), toMap(request.getPorteEstimado())));
        }
        if (request.getModalidadeConcorrencia() != null) {
            config.setModalidadeConcorrenciaScores(merge(config.getModalidadeConcorrenciaScores(), toMap(request.getModalidadeConcorrencia())));
        }
        if (request.getTipo() != null) {
            config.setTipoScores(merge(config.getTipoScores(), toMap(request.getTipo())));
        }
        if (request.getCaracteristica() != null) {
            config.setCaracteristicaScores(merge(config.getCaracteristicaScores(), toMap(request.getCaracteristica())));
        }
        if (request.getEstados() != null) {
            config.setEstadoScores(merge(config.getEstadoScores(), toMap(request.getEstados())));
        }
        if (request.getPontuacaoReferencia() != null) {
            config.setPontuacaoReferencia(request.getPontuacaoReferencia());
        }

        config.setUpdatedBy(parseUuidOrNull(userId));
        config.setUpdatedAt(LocalDateTime.now());

        BudgetScoringConfig saved = scoringRepository.save(config);
        return toResponse(saved);
    }

    public BudgetScoringConfigResponseDTO getScoring() {
        return scoringRepository.findById(DEFAULT_ID)
                .map(this::toResponse)
                .orElseGet(() -> BudgetScoringConfigResponseDTO.builder()
                        .id(DEFAULT_ID)
                        .build());
    }

    public Map<String, List<String>> getAvailableFields() {
        Map<String, List<String>> fields = new HashMap<>();
        
        fields.put("mercado", List.of("Público", "Privado"));
        
        fields.put("setor", List.of(
            "Siderurgia e Metalurgica",
            "Mineração",
            "Papel e Celulose",
            "Óleo e Gás",
            "Química, Petroquímica e fertilizantes",
            "Estaleiros, Terminais Portuários",
            "Energia",
            "Saneamento",
            "Ferrovias",
            "Aeroportos",
            "Alimentos",
            "Bebidas",
            "Automobilístico",
            "Farmacoquímico",
            "Outros"
        ));
        
        fields.put("tempoContrato", List.of(
            "Menor que 12 meses",
            "Entre 12 meses e 24 meses",
            "Entre 24 meses e 36 meses",
            "Maior que 36 meses"
        ));
        
        fields.put("porteEstimado", List.of("Pequeno", "Médio", "Grande"));
        
        fields.put("modalidadeConcorrencia", List.of(
            "Menor Preço",
            "Leilão",
            "Maior Desconto",
            "Outros"
        ));
        
        fields.put("tipo", List.of(
            "Rotina-Continuado",
            "Rotina-Sob Demanda",
            "Spot",
            "Parada de Manutenção",
            "Guarda Chuva",
            "Outros"
        ));
        
        fields.put("caracteristica", List.of(
            "Venda de Mão de Obra (Diária / Mensal)",
            "Fornecimento de serviços",
            "Revenda de Peças e Sobressalentes",
            "Facilities",
            "Outros"
        ));
        
        fields.put("estados", List.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", 
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", 
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        ));
        
        return fields;
    }

    private Map<String, Integer> toMap(List<ScoringEntryDTO> entries) {
        return entries.stream()
                .filter(e -> e.getValue() != null && e.getScore() != null)
                .collect(Collectors.toMap(
                        e -> e.getValue().trim(),
                        ScoringEntryDTO::getScore,
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    private Map<String, Integer> merge(Map<String, Integer> base, Map<String, Integer> update) {
        Map<String, Integer> result = base == null ? new HashMap<>() : new HashMap<>(base);
        result.putAll(update);
        return result;
    }

    private BudgetScoringConfigResponseDTO toResponse(BudgetScoringConfig cfg) {
        return BudgetScoringConfigResponseDTO.builder()
                .id(cfg.getId())
                .keywordScores(cfg.getKeywordScores())
                .mercadoScores(cfg.getMercadoScores())
                .setorScores(cfg.getSetorScores())
                .tempoContratoScores(cfg.getTempoContratoScores())
                .porteEstimadoScores(cfg.getPorteEstimadoScores())
                .modalidadeConcorrenciaScores(cfg.getModalidadeConcorrenciaScores())
                .tipoScores(cfg.getTipoScores())
                .caracteristicaScores(cfg.getCaracteristicaScores())
                .estadoScores(cfg.getEstadoScores())
                .pontuacaoReferencia(cfg.getPontuacaoReferencia())
                .createdAt(cfg.getCreatedAt())
                .updatedAt(cfg.getUpdatedAt())
                .build();
    }

    private UUID parseUuidOrNull(String raw) {
        try {
            return raw == null ? null : UUID.fromString(raw);
        } catch (Exception e) {
            return null;
        }
    }
}


