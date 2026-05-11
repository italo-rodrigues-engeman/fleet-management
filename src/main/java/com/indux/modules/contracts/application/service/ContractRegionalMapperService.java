package com.indux.modules.contracts.application.service;

import com.indux.core.domain.model.employee.Filial;
import com.indux.core.domain.repository.generic.BranchRepository;
import com.indux.modules.contracts.application.dto.ContractRegionalResponseDTO;
import com.indux.modules.contracts.domain.model.Contract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContractRegionalMapperService {
    
    private final BranchRepository branchRepository;
    
    public ContractRegionalResponseDTO mapToResponseDTO(Contract contract) {
        ContractRegionalResponseDTO dto = new ContractRegionalResponseDTO();
        
        // Mapear campos básicos
        dto.setMega_id(contract.getMegaId());
        dto.setCliente(contract.getCliente());
        dto.setCentro_custo_nome(contract.getNomeCentroCustos());
        dto.setClientName(contract.getCliente()); // Assumindo que clientName é o mesmo que cliente
        dto.setRateio_id(contract.getRateioId());
        dto.setGestor_interno_contrato(contract.getGestorInternoContrato());
        dto.setId(contract.getId());
        dto.setNome_projeto(contract.getNomeProjeto());
        dto.setAtivo(contract.getAtivo());
        
        // Mapear filial
        if (contract.getFilialId() != null) {
            Optional<Filial> filialOpt = branchRepository.findById(contract.getFilialId());
            if (filialOpt.isPresent()) {
                Filial filial = filialOpt.get();
                ContractRegionalResponseDTO.FilialDTO filialDTO = new ContractRegionalResponseDTO.FilialDTO();
                filialDTO.setRazao_social(filial.getCorporateName());
                filialDTO.setCodigo_filial(filial.getBranchId());
                filialDTO.setNome_filial(filial.getBranchName());
                dto.setFilial(filialDTO);
            }
        }
        
        // Mapear coordenadores (assumindo que é uma lista vazia por enquanto)
        dto.setCoordenadores_contrato(new ArrayList<>());
        
        return dto;
    }
    
    public List<ContractRegionalResponseDTO> mapToResponseDTOList(List<Contract> contracts) {
        return contracts.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }
}
