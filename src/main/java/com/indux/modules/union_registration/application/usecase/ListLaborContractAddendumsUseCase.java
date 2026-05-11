package com.indux.modules.union_registration.application.usecase;

import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractAddendumMapper;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListLaborContractAddendumsUseCase {

    private final LaborContractAddendumRepository addendumRepository;
    private final LaborContractAddendumMapper addendumMapper;

    public List<LaborContractAddendumResponseDTO> execute(String contractId) {
        // Buscar aditivos ordenados por sequência decrescente (do maior para o menor)
        List<LaborContractAddendum> addendums = addendumRepository
                .findByContratoTrabalhistaIdOrderBySequenciaDesc(contractId);
        
        return addendums.stream()
                .map(addendumMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}






