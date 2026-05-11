package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.ListUnionsFilterDTO;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.dto.UnionSimpleResponseDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class ListUnionsUseCase {
    
    private final UnionRepository unionRepository;
    private final UnionMapper unionMapper;
    
    public Page<UnionResponseDTO> execute(@Valid ListUnionsFilterDTO filters, Pageable pageable) {
        Page<Union> unions = unionRepository.findAllWithFilters(filters, pageable);
        
        List<UnionResponseDTO> unionDTOs = unions.getContent().stream()
                .map(unionMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(unionDTOs, pageable, unions.getTotalElements());
    }
    
    public Page<UnionResponseDTO> listAll(Pageable pageable) {
        Page<Union> unions = unionRepository.findAll(pageable);
        
        List<UnionResponseDTO> unionDTOs = unions.getContent().stream()
                .map(unionMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(unionDTOs, pageable, unions.getTotalElements());
    }
    
    public Page<UnionSimpleResponseDTO> executeSimple(@Valid ListUnionsFilterDTO filters, Pageable pageable) {
        Page<Union> unions = unionRepository.findAllWithFilters(filters, pageable);
        
        List<UnionSimpleResponseDTO> unionDTOs = unions.getContent().stream()
                .map(unionMapper::toSimpleResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(unionDTOs, pageable, unions.getTotalElements());
    }
    
    public Page<UnionSimpleResponseDTO> listAllSimple(Pageable pageable) {
        Page<Union> unions = unionRepository.findAll(pageable);
        
        List<UnionSimpleResponseDTO> unionDTOs = unions.getContent().stream()
                .map(unionMapper::toSimpleResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(unionDTOs, pageable, unions.getTotalElements());
    }
}
