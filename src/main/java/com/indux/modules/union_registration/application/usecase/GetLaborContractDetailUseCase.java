package com.indux.modules.union_registration.application.usecase;

import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLaborContractDetailUseCase {
    
    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository laborContractAddendumRepository;
    private final LaborContractMapper laborContractMapper;
    
    public LaborContractResponseDTO execute(String laborContractId) {
        LaborContract contract = laborContractRepository.findById(laborContractId)
                .orElseThrow(() -> new RuntimeException("Contrato trabalhista não encontrado"));
        
        List<LaborContractAddendum> addendums = laborContractAddendumRepository
                .findByContratoTrabalhistaIdAndStatusRegistro(laborContractId, "ATIVO");
        
        return laborContractMapper.toResponseDTOWithAddendums(contract, addendums);
    }
}
