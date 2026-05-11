package com.indux.modules.contracts.application.service;

import com.indux.modules.contracts.application.dto.ContractDTO;
import com.indux.modules.contracts.application.mapper.ContractMapper;
import com.indux.modules.contracts.domain.exception.ContractNotFoundException;
import com.indux.modules.contracts.domain.model.Contract;
import com.indux.modules.contracts.domain.repository.ContractCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetContractByIdUseCase {

    private final ContractCustomRepository contractCustomRepository;
    private final ContractMapper contractMapper;

    @Transactional(readOnly = true)
    public ContractDTO execute(Long id) {
        Contract contract = contractCustomRepository.findById(id);
        
        if (contract == null) {
            throw new ContractNotFoundException(id);
        }

        return contractMapper.toDTO(contract);
    }
} 