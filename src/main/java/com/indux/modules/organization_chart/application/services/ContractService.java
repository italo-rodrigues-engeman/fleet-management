package com.indux.modules.organization_chart.application.services;

import com.indux.core.domain.repository.generic.BranchRepository;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.organization_chart.application.dtos.ContractDTO;
import com.indux.modules.organization_chart.application.dtos.CreateContractDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
import com.indux.modules.organization_chart.infra.mapper.ContractMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {
    private final OrganizationContractRepository organizationContractRepository;
    private final ContractMapper contractMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationProjectRepository organizationProjectRepository;

    public ContractService(OrganizationContractRepository organizationContractRepository, ContractMapper contractMapper,
            OrganizationRepository organizationRepository,
            OrganizationProjectRepository organizationProjectRepository) {
        this.organizationContractRepository = organizationContractRepository;
        this.contractMapper = contractMapper;
        this.organizationRepository = organizationRepository;
        this.organizationProjectRepository = organizationProjectRepository;
    }

    public void createContract(CreateContractDTO dto) {
        var subordinate = organizationRepository.findById(dto.subordinado())
                .orElseThrow(() -> new ModuleNotFoundFailure("Subordinado não existe."));
        var contract = contractMapper.toEntity(dto, subordinate);
        organizationContractRepository.save(contract);
    }

    public Page<ContractDTO> searchContracts(Pageable pageable, String search, List<Long> organizationIds,
            ContractType tipo) {
        Page<ContractEntity> contracts = organizationContractRepository.findAllWithFilters(search, organizationIds,
                tipo, pageable);
        List<ContractDTO> dtos = contracts.stream()
                .map(contractMapper::toDto)
                .toList();
        return new PageImpl<>(dtos, pageable, contracts.getTotalElements());
    }

    public void updateContract(Long id, CreateContractDTO dto) {
        var subordinate = organizationRepository.findById(dto.subordinado())
                .orElseThrow(() -> new ModuleFailure("Subordinado não existe."));
        var contract = organizationContractRepository.findByIdWithoutBranch(id)
                .orElseThrow(() -> new ModuleFailure("Contrato não encontrado"));
        var newContract = contractMapper.toEntity(dto, subordinate);
        newContract.setId(contract.getId());
        organizationContractRepository.save(newContract);
    }

    public void deleteContract(Long id) {
        if (!organizationContractRepository.existsById(id))
            throw new ModuleFailure("Contrato não encontrado");
        var projects = organizationProjectRepository.findByContractId(id);
        if (!projects.isEmpty())
            throw new ModuleFailure("Contrato está vinculado a um ou mais projetos.");
        organizationContractRepository.deleteById(id);
    }
}
