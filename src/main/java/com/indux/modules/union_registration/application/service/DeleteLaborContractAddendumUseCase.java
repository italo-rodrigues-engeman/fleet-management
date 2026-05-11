package com.indux.modules.union_registration.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteLaborContractAddendumUseCase {

    private final LaborContractAddendumRepository addendumRepository;

    public GenericMessage execute(String addendumId) {
        LaborContractAddendum addendum = addendumRepository.findById(addendumId)
                .orElseThrow(() -> new RuntimeException("Aditivo não encontrado"));

        addendumRepository.delete(addendum);
        return new GenericMessage("Aditivo excluído com sucesso", 200);
    }

    public GenericMessage softDelete(String addendumId) {
        LaborContractAddendum addendum = addendumRepository.findById(addendumId)
                .orElseThrow(() -> new RuntimeException("Aditivo não encontrado"));

        addendum.setStatusRegistro("INATIVO");
        addendumRepository.save(addendum);
        return new GenericMessage("Aditivo desativado com sucesso", 200);
    }
}






