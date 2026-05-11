package com.indux.modules.training.infra.mappers;

import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.training.application.dto.HeadquartesDTO;
import com.indux.modules.training.domain.entity.HeadquartersEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface HeadquartersMapper {
        HeadquartersEntity toEntity(HeadquartesDTO dto);
        
        default HeadquartesDTO toDto(HeadquartersEntity entity, Client client, Map<String, Object> filialOrganizationData) {
            String id = entity != null ? entity.getId() : null;
            String name = entity != null ? entity.getName() : null;
            String ac = entity != null ? entity.getAc() : null;
            String filialHCM = entity != null ? entity.getFilialHCM() : null;
            String nomeFilial = null;
            String environment = entity != null ? entity.getEnvironment() : null;
            String entityClient = entity != null ? entity.getClient() : null;
            
            Long idProjeto = null;
            String nomeProjeto = null;
            Long idContrato = null;
            String nomeContrato = null;
            Long idRegional = null;
            String nomeRegional = null;
            
            if (filialOrganizationData != null) {
                Object projetoIdObj = filialOrganizationData.get("idProjeto");
                if (projetoIdObj instanceof Long) {
                    idProjeto = (Long) projetoIdObj;
                } else if (projetoIdObj instanceof Number) {
                    idProjeto = ((Number) projetoIdObj).longValue();
                } else if (projetoIdObj != null) {
                    try {
                        idProjeto = Long.valueOf(projetoIdObj.toString());
                    } catch (NumberFormatException e) {
                        idProjeto = null;
                    }
                }
                
                Object projetoNomeObj = filialOrganizationData.get("nomeProjeto");
                nomeProjeto = projetoNomeObj != null ? projetoNomeObj.toString() : null;
                
                Object contratoIdObj = filialOrganizationData.get("idContrato");
                if (contratoIdObj instanceof Long) {
                    idContrato = (Long) contratoIdObj;
                } else if (contratoIdObj instanceof Number) {
                    idContrato = ((Number) contratoIdObj).longValue();
                } else if (contratoIdObj != null) {
                    try {
                        idContrato = Long.valueOf(contratoIdObj.toString());
                    } catch (NumberFormatException e) {
                        idContrato = null;
                    }
                }
                
                Object contratoNomeObj = filialOrganizationData.get("nomeContrato");
                nomeContrato = contratoNomeObj != null ? contratoNomeObj.toString() : null;
                
                Object regionalIdObj = filialOrganizationData.get("idRegional");
                if (regionalIdObj instanceof Long) {
                    idRegional = (Long) regionalIdObj;
                } else if (regionalIdObj instanceof Number) {
                    idRegional = ((Number) regionalIdObj).longValue();
                } else if (regionalIdObj != null) {
                    try {
                        idRegional = Long.valueOf(regionalIdObj.toString());
                    } catch (NumberFormatException e) {
                        idRegional = null;
                    }
                }
                
                Object regionalNomeObj = filialOrganizationData.get("nomeRegional");
                nomeRegional = regionalNomeObj != null ? regionalNomeObj.toString() : null;
                
                // Pega o nome da filial do filialOrganizationData
                Object filialNomeObj = filialOrganizationData.get("nomeFilial");
                nomeFilial = filialNomeObj != null ? filialNomeObj.toString() : null;
            }
            
            return new HeadquartesDTO(
                id,
                name,
                ac,
                filialHCM,
                nomeFilial,
                environment,
                entityClient,
                client,
                idProjeto,
                nomeProjeto,
                idContrato,
                nomeContrato,
                idRegional,
                nomeRegional
            );
        }
        
        default List<HeadquartesDTO> toDtos(List<HeadquartersEntity> entities) {
            if (entities == null) {
                return null;
            }
            List<HeadquartesDTO> result = new java.util.ArrayList<>(entities.size());
            for (HeadquartersEntity entity : entities) {
                result.add(toDto(entity, null, null)); // For list method, no filial data
            }
            return result;
        }
}