package com.indux.modules.contracts.application.service;

import com.indux.modules.contracts.domain.model.Contract;
import com.indux.modules.contracts.domain.repository.ContractCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListContractsUseCase {

    private final ContractCustomRepository contractCustomRepository;

    @Transactional(readOnly = true)
    public Page<Contract> execute(
            String cliente,
            String nomeProjeto,
            String regional,
            Boolean ativo,
            String os,
            String codSap,
            String gestorInterno,
            String gestorCliente,
            Pageable pageable) {

        return contractCustomRepository.findAllWithFilters(
                cliente,
                nomeProjeto,
                regional,
                ativo,
                os,
                codSap,
                gestorInterno,
                gestorCliente,
                pageable
        );
    }
} 