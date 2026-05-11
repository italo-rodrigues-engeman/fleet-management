package com.indux.modules.union_registration.application.usecase;

import com.indux.modules.union_registration.application.dto.LaborContractSummaryDTO;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLaborContractsSummaryUseCase {
    
    private final LaborContractRepository laborContractRepository;
    
    public List<LaborContractSummaryDTO> execute(String unionId) {
        return laborContractRepository.findBySindicatoTrabalhadoresId(unionId)
                .stream()
                .map(laborContract -> new LaborContractSummaryDTO(
                        laborContract.getId(),
                        laborContract.getNumeroRegistro(),
                        laborContract.getDataInicioVigencia(),
                        laborContract.getDataFimVigencia(),
                        laborContract.getStatusRegistro(),
                        laborContract.getTipoInstrumento().toString()
                ))
                .toList();
    }
}
