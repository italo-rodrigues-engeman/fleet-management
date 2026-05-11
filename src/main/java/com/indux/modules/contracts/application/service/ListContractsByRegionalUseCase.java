package com.indux.modules.contracts.application.service;

import com.indux.modules.contracts.domain.model.Contract;
import com.indux.modules.contracts.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListContractsByRegionalUseCase {

    private final ContractRepository contractRepository;

    public Page<Contract> execute(Long regionalId, Pageable pageable) {
        return contractRepository.findByRegionalId(regionalId, pageable);
    }

    public Page<Contract> execute(java.util.List<Long> regionalIds, Pageable pageable) {
        return contractRepository.findByRegionalIds(regionalIds, pageable);
    }

    public Page<Contract> execute(String regionalName, Pageable pageable) {
        return contractRepository.findByRegionalName(regionalName, pageable);
    }
}
