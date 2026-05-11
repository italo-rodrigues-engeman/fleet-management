package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class GetUnionByIdUseCase {
    
    private final UnionRepository unionRepository;
    private final UnionMapper unionMapper;
    
    public UnionResponseDTO execute(String unionId) {
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + unionId));
        
        return unionMapper.toResponseDTO(union);
    }
}
