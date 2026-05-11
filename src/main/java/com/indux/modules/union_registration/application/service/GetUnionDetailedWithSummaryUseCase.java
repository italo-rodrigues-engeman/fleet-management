package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.LaborContractSummaryDTO;
import com.indux.modules.union_registration.application.dto.UnionDetailedWithSummaryDTO;
import com.indux.modules.union_registration.application.usecase.GetLaborContractsSummaryUseCase;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class GetUnionDetailedWithSummaryUseCase {
    
    private final UnionRepository unionRepository;
    private final GetLaborContractsSummaryUseCase getLaborContractsSummaryUseCase;
    
    public UnionDetailedWithSummaryDTO execute(String unionId) {
        try {
            // Buscar sindicato
            Union union = unionRepository.findById(unionId)
                    .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + unionId));
            
            // Buscar ACTs/CCTs resumidas do sindicato
            var laborContractsSummary = getLaborContractsSummaryUseCase.execute(unionId);
            
            // Converter para DTO detalhado com resumo
            UnionDetailedWithSummaryDTO result = convertToDetailedWithSummaryDTO(union, laborContractsSummary);
            
            return result;
            
        } catch (UnionNotFoundException e) {
            log.warn("Sindicato não encontrado com ID: {}", unionId);
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar sindicato detalhado com resumo para ID: {}", unionId, e);
            throw new RuntimeException("Erro ao buscar sindicato detalhado: " + e.getMessage(), e);
        }
    }
    
    private UnionDetailedWithSummaryDTO convertToDetailedWithSummaryDTO(Union union, java.util.List<LaborContractSummaryDTO> laborContracts) {
        return new UnionDetailedWithSummaryDTO(
                union.getId(),
                union.getCodeID(),
                union.getNomeCompletoSindicato(),
                union.getCnpj(),
                union.getCodigoCnes(),
                union.getTipo(),
                union.getCategoriaRepresentada(),
                union.getAbrangenciaTerritorial(),
                union.getUfSede(),
                union.getMunicipioSede(),
                union.getUfsAtendidas(),
                union.getMunicipiosAtendidos(),
                union.getObservacoesTerritoriais(),
                union.getLogradouro(),
                union.getNumero(),
                union.getUf(),
                union.getCep(),
                union.getCidade(),
                union.getTelefonePrincipal(),
                union.getTelefone2(),
                union.getEmailInstitucional(),
                union.getEmail2(),
                union.getSite(),
                union.getRedeSocial(),
                union.getSituacaoMte(),
                union.getDataUltimaAtualizacaoMte(),
                union.getPresidenteAtual(),
                union.getMandatoInicio(),
                union.getMandatoFim(),
                union.getObservacoes(),
                union.getUsuarioCriacao(),
                union.getStatusRegistro(),
                laborContracts
        );
    }
}
