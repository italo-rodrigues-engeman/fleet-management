package com.indux.modules.union_registration.application.mapper;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.union_registration.application.dto.*;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LaborContractMapper.class})
public interface UnionMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "attachments", target = "documentosAnexos")
    @Mapping(target = "codeID", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Union toEntity(CreateUnionRequestDTO dto, List<AttachmentEntity> attachments, Integer codeID);
    
    UnionResponseDTO toResponseDTO(Union union);
    
    UnionSimpleResponseDTO toSimpleResponseDTO(Union union);
    
    @Mapping(source = "laborContracts", target = "laborContracts", ignore = true)
    UnionDetailedResponseDTO toDetailedResponseDTO(Union union, List<LaborContract> laborContracts);
    
    default UnionDetailedResponseDTO toDetailedResponseDTOWithSummary(Union union, List<LaborContract> laborContracts) {
        UnionDetailedResponseDTO dto = toDetailedResponseDTO(union, laborContracts);
        
        // Converter para dados resumidos
        List<LaborContractSummaryDTO> summaryContracts = laborContracts.stream()
                .map(contract -> new LaborContractSummaryDTO(
                        contract.getId(),
                        contract.getNumeroRegistro(),
                        contract.getDataInicioVigencia(),
                        contract.getDataFimVigencia(),
                        contract.getStatusRegistro(),
                        contract.getTipoInstrumento().toString()
                ))
                .collect(java.util.stream.Collectors.toList());
        
        dto.setLaborContracts(summaryContracts);
        return dto;
    }
    
    default UnionDetailedResponseDTO toDetailedResponseDTOWithAddendums(Union union, List<LaborContract> laborContracts, 
                                                                      LaborContractAddendumRepository laborContractAddendumRepository,
                                                                      LaborContractMapper laborContractMapper) {
        // Criar DTO detalhado com dados resumidos (mesmo comportamento do endpoint detailed)
        UnionDetailedResponseDTO dto = toDetailedResponseDTOWithSummary(union, laborContracts);
        
        return dto;
    }
}
