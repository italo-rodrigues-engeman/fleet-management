package com.indux.modules.training.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import com.indux.modules.training.application.dto.HeadquartesDTO;
import com.indux.modules.training.domain.repository.HeadquartersRepository;
import com.indux.modules.training.infra.mappers.HeadquartersMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HeadquartersService {

    private final HeadquartersMapper headquartersMapper;
    private final HeadquartersRepository  headquartersRepository;
    private final ClientRepository clientRepository;
    private final OrganizationFilialRepository  organizationFilialRepository;
    private final FilialService filialService;

    public HeadquartersService(HeadquartersMapper headquartersMapper, HeadquartersRepository headquartersRepository, ClientRepository clientRepository, OrganizationFilialRepository organizationFilialRepository, FilialService filialService) {
        this.headquartersMapper = headquartersMapper;
        this.headquartersRepository = headquartersRepository;
        this.clientRepository = clientRepository;
        this.organizationFilialRepository = organizationFilialRepository;
        this.filialService = filialService;
    }

    public void createHeadquarters(HeadquartesDTO dto){
        headquartersRepository.save(headquartersMapper.toEntity(dto));
    }

    public HeadquartesDTO findHeadquartersById(String id){
        var entity = headquartersRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Matriz não encontrado"));
        Client client = null;
        Map<String, Object> filialOrganizationData = null;
        if(entity.getClient() != null) {
            client = clientRepository.findById(Long.parseLong(entity.getClient())).orElse(null);
        }
        if(entity.getFilialHCM() != null) {
            List<Map<String, Object>> filialOrganizations = filialService.filialOrganization(List.of(Integer.parseInt(entity.getFilialHCM())));
            if (filialOrganizations != null && !filialOrganizations.isEmpty()) {
                filialOrganizationData = filialOrganizations.get(0);
            }
        }
        return headquartersMapper.toDto(entity, client, filialOrganizationData);
    }

    public Page<HeadquartesDTO> findHeadquarters(Pageable pageable){
        var entity = headquartersRepository.findAll(pageable);

        var dto = entity.getContent().stream().map(headquartersEntity -> {
            Client client = null;
            Map<String, Object> filialOrganizationData = null;
            if(headquartersEntity.getClient() != null && !headquartersEntity.getClient().isEmpty()) {
                client = clientRepository.findById(Long.parseLong(headquartersEntity.getClient())).orElse(null);
            }
            if(headquartersEntity.getFilialHCM() != null) {
                FilialHcmEntity filialHcmEntity = organizationFilialRepository.findById(Integer.parseInt(headquartersEntity.getFilialHCM())).orElse(null);
                if (filialHcmEntity != null) {
                    filialOrganizationData = Map.of("nomeFilial", filialHcmEntity.getNomeFilial());
                }
            }
            return headquartersMapper.toDto(headquartersEntity, client, filialOrganizationData);
        }).toList();
        
        return new PageImpl<>(dto,pageable,entity.getTotalElements());
    }

    public void updateHeadquarters(String id,HeadquartesDTO dto){
        headquartersRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Matriz não encontrado"));
        var updateEntity = headquartersMapper.toEntity(dto);
        updateEntity.setId(id);
        headquartersRepository.save(updateEntity);
    }

}