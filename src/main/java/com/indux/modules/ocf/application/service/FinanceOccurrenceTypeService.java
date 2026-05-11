package com.indux.modules.ocf.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ocf.application.dto.FinanceOccurrenceTypeRecord;
import com.indux.modules.ocf.domain.model.FinanceOccurrenceComplaintType;
import com.indux.modules.ocf.domain.model.FinanceOccurrenceType;
import com.indux.modules.ocf.domain.repository.FinanceOccurrenceComplaintTypeRepository;
import com.indux.modules.ocf.domain.repository.FinanceOccurrenceTypeRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FinanceOccurrenceTypeService {
    private final FinanceOccurrenceComplaintTypeRepository repository;
    private final FinanceOccurrenceTypeRepository typeRepository;

    public FinanceOccurrenceTypeService(FinanceOccurrenceComplaintTypeRepository repository, FinanceOccurrenceTypeRepository typeRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
    }

    /**
     * Retorna uma lista de tipos de reclamações e seus tipos de ocorrência.
     * @return Lista de reclamação com os tipos relacionados.
     */
    public List<FinanceOccurrenceComplaintType> fetchAllComplaintType() {
        return repository.findAll();
    }

    public List<FinanceOccurrenceType> fetchAllOccurrenceType() {
        return typeRepository.findAll();
    }

    /**
     * Cria um tipo de reclamação no banco de dados.
     * @param dto FinanceOccurrenceTypeRecord para a criação do tipo de ocorrência
     */
    public void createComplaintType(FinanceOccurrenceTypeRecord dto) {
        var complaintType = FinanceOccurrenceComplaintType.builder()
                .name(dto.nome())
                .occurrences(List.of())
                .build();
        repository.save(complaintType);
    }

    /**
     * Cria um tipo de ocorrência no banco de dados. Relaciona com um tipo de reclamação.
     * @param dto FinanceOccurrenceTypeRecord para a criação do tipo de ocorrência
     */
    public void createOccurrenceType(FinanceOccurrenceTypeRecord dto) {
        if (dto.ref() == null)
            throw new IllegalArgumentException("Não é possível criar um tipo sem referênciar a um tipo-pai.");
        var complaintType = repository.findById(dto.ref()).orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrado nenhum tipo-pai para associar ao tipo."));

        var financeType = FinanceOccurrenceType.builder()
                .name(dto.nome())
                .build();
        var financeTypeSaved = typeRepository.save(financeType);

        complaintType.getOccurrences().add(financeTypeSaved);
        repository.save(complaintType);
    }

    /**
     * Deleta um tipo de reclamação dentro do banco de dados. Exclui toda a cadeia de relação dessa reclamação.
     * @param id id do tipo de reclamação
     */
    public void deleteComplaintType(Long id) {
        if (id == null) throw new IllegalArgumentException("Você deve setar o tipo que deseja deletar.");
        var complaintType = repository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Não é possível deletar um tipo inexistente."));
        Set<FinanceOccurrenceType> children = new HashSet<>(complaintType.getOccurrences());

        complaintType.getOccurrences().clear();
        repository.save(complaintType);

        repository.deleteById(complaintType.getId());

        for (FinanceOccurrenceType child : children) {
            long refs = repository.countByOccurrencesContains(child);
            if (refs == 0) {
                typeRepository.deleteById(child.getId());
            }
        }
    }

    /**
     * Deleta um tipo de ocorrência via ID.
     * @param occurrenceTypeID id do tipo de ocorrência
     */
    public void deleteOccurrenceType(Long occurrenceTypeID) {
        var child = typeRepository.findById(occurrenceTypeID)
                .orElseThrow(() -> new ModuleNotFoundFailure(
                        "Tipo de ocorrência não encontrado"));

        List<FinanceOccurrenceComplaintType> parents =
                repository.findByOccurrencesContains(child);

        for (FinanceOccurrenceComplaintType p : parents) {
            p.getOccurrences().remove(child);
            repository.save(p);
        }

        typeRepository.deleteById(occurrenceTypeID);
    }

}
