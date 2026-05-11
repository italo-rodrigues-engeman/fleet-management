package com.indux.modules.contracts.domain.repository;

import com.indux.modules.contracts.domain.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractCustomRepository {
    
    Page<Contract> findAllWithFilters(
            String cliente,
            String nomeProjeto,
            String regional,
            Boolean ativo,
            String os,
            String codSap,
            String gestorInterno,
            String gestorCliente,
            Pageable pageable);
    
    Contract findById(Long id);
} 