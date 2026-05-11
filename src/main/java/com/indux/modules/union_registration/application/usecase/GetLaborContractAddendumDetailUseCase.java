package com.indux.modules.union_registration.application.usecase;

import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractAddendumMapper;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLaborContractAddendumDetailUseCase {

    private final LaborContractAddendumRepository addendumRepository;
    private final LaborContractAddendumMapper addendumMapper;

    public LaborContractAddendumResponseDTO execute(String addendumId) {
        LaborContractAddendum addendum = addendumRepository.findById(addendumId)
                .orElseThrow(() -> new RuntimeException("Aditivo não encontrado"));
        return addendumMapper.toResponseDTO(addendum);
    }
}






