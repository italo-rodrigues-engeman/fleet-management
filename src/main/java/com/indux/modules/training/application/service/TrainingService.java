package com.indux.modules.training.application.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.training.application.dto.FiliaisHCMRequest;
import com.indux.modules.training.application.dto.FiliaisHCMResponse;
import com.indux.modules.training.application.dto.TrainingRequest;
import com.indux.modules.training.application.dto.TrainingResponse;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.TrainingRepository;
import com.indux.modules.training.infra.mappers.TrainingMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.indux.core.domain.model.modules.AttachmentEntity;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final AttachmentService attachmentService;
    private final FilialService filialService;
    private final ClientRepository clientRepository;


    public TrainingService(TrainingRepository trainingRepository, TrainingMapper trainingMapper, AttachmentService attachmentService, FilialService filialService, ClientRepository clientRepository) {
        this.trainingRepository = trainingRepository;
        this.trainingMapper = trainingMapper;
        this.attachmentService = attachmentService;
        this.filialService = filialService;
        this.clientRepository = clientRepository;
    }

    public void createTraining(TrainingRequest dto) {
        var entity = trainingMapper.toEntity(dto);
        saveFile(dto, entity);
        trainingRepository.save(entity);
    }

    private void saveFile(TrainingRequest dto, TrainingEntity entity) {
        if (dto.filiais() != null && entity.getFiliais() != null) {
            List<FiliaisHCMResponse> updatedFiliais = new ArrayList<>();

            for (int i = 0; i < dto.filiais().size(); i++) {
                FiliaisHCMRequest filialRequest = dto.filiais().get(i);
                FiliaisHCMResponse filialResponse = entity.getFiliais().get(i);

                if (filialRequest.file() != null) {
                    List<AttachmentEntity> attachments = attachmentService.createAttachmentsFromMultipartFiles(
                        List.of(filialRequest.file()),
                        "training/documents"
                    );

                    if (!attachments.isEmpty()) {
                        FiliaisHCMResponse updatedFilial = trainingMapper.createResponse(filialResponse, attachments.getFirst());
                        updatedFiliais.add(updatedFilial);
                    } else {
                        updatedFiliais.add(filialResponse);
                    }
                } else {
                    updatedFiliais.add(filialResponse);
                }
            }
            entity.setFiliais(updatedFiliais);
        }
    }

    public TrainingResponse getTraining(String id) {
        TrainingEntity entity = trainingRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Treinamento não encontrado"));

        if (entity.getFiliais() == null || entity.getFiliais().isEmpty()) {
            return trainingMapper.toDto(entity);
        }

        List<Integer> allFilialIds = getAllFilialIds(entity);
        var filialList = filialService.filialOrganization(allFilialIds);
        List<FiliaisHCMResponse> updatedFiliais = new ArrayList<>();
        
        List<String> clientIds = entity.getFiliais().stream()
            .map(FiliaisHCMResponse::client)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        
        Map<String, Client> clientMap = new HashMap<>();
        if (!clientIds.isEmpty()) {
            List<Client> clients = clientRepository.findAllById(
                clientIds.stream()
                    .map(Long::parseLong)
                    .toList()
            );
            clientMap = clients.stream()
                .collect(Collectors.toMap(
                    client -> client.getId().toString(),
                    client -> client,
                    (existing, replacement) -> existing
                ));
        }
        
        for (FiliaisHCMResponse originalFilial : entity.getFiliais()) {
            List<Map<String, Object>> orgDataList = new ArrayList<>();
            
            if (originalFilial.filialHCM() != null) {
                for (String filialId : originalFilial.filialHCM()) {
                    Integer filialIdInt = Integer.valueOf(filialId);
                    Map<String, Object> orgData = filialList.stream()
                        .filter(org -> org.get("idFilial").equals(filialIdInt))
                        .findFirst()
                        .orElse(null);
                    if (orgData != null) {
                        orgDataList.add(orgData);
                    }
                }
            }
            
            List<Integer> filialIds = extractUniqueValues(orgDataList, "idFilial", Integer.class);
            List<String> filialNames = extractUniqueValues(orgDataList, "nomeFilial", String.class);
            List<Long> projectIds = extractUniqueValues(orgDataList, "idProjeto", Long.class);
            List<String> projectNames = extractUniqueValues(orgDataList, "nomeProjeto", String.class);
            List<Long> contractIds = extractUniqueValues(orgDataList, "idContrato", Long.class);
            List<String> contractNames = extractUniqueValues(orgDataList, "nomeContrato", String.class);
            List<Long> regionalIds = extractUniqueValues(orgDataList, "idRegional", Long.class);
            List<String> regionalNames = extractUniqueValues(orgDataList, "nomeRegional", String.class);
            
            Map<String, Object> combinedOrgData = new HashMap<>();
            combinedOrgData.put("idFilial", filialIds);
            combinedOrgData.put("nomeFilial", filialNames);
            combinedOrgData.put("idProjeto", projectIds);
            combinedOrgData.put("nomeProjeto", projectNames);
            combinedOrgData.put("idContrato", contractIds);
            combinedOrgData.put("nomeContrato", contractNames);
            combinedOrgData.put("idRegional", regionalIds);
            combinedOrgData.put("nomeRegional", regionalNames);
            
            FiliaisHCMResponse updatedFilial = trainingMapper.createResponseWithOrgData(originalFilial, combinedOrgData);
            
            if (originalFilial.client() != null) {
                Client client = clientMap.get(originalFilial.client());
                if (client != null) {
                    updatedFilial = trainingMapper.updateClientName(updatedFilial, client.getName());
                }
            }
            
            updatedFiliais.add(updatedFilial);
        }
        
        entity.setFiliais(updatedFiliais);

        return trainingMapper.toDto(entity); 
    }

    @NotNull
    private static List<Integer> getAllFilialIds(TrainingEntity entity) {
        List<Integer> allFilialIds = new ArrayList<>();
        for (FiliaisHCMResponse filial : entity.getFiliais()) {
            if (filial.filialHCM() != null) {
                for (String filialId : filial.filialHCM()) {
                    allFilialIds.add(Integer.valueOf(filialId));
                }
            }
        }
        return allFilialIds;
    }

    private <T> List<T> extractUniqueValues(List<Map<String, Object>> orgDataList, String key, Class<T> type) {
        List<T> uniqueValues = new ArrayList<>();
        for (Map<String, Object> orgData : orgDataList) {
            Object value = orgData.get(key);
            if (value != null && type.isInstance(value)) {
                T typedValue = type.cast(value);
                if (!uniqueValues.contains(typedValue)) {
                    uniqueValues.add(typedValue);
                }
            }
        }
        return uniqueValues;
    }

    public Page<TrainingResponse> getTrainingsWithFilters(List<String> filialHCMList, String search, String mandatory, String institutionId, Pageable pageable) {
        var entity = trainingRepository.findTrainingsWithFilters(filialHCMList, search, mandatory, institutionId, pageable);
        
        entity.getContent().forEach(trainingEntity -> {
            trainingEntity.setFiliais(null);
        });
        
        var dto = trainingMapper.toDtos(entity.getContent());
        return new PageImpl<>(dto, pageable, entity.getTotalElements());
    }

    public void updateTraining(String id, TrainingRequest dto) {
        var entity = trainingRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Treinamento não encontrado"));
        var updateEntity = trainingMapper.toEntity(dto);
        updateEntity.setId(id);
        setOldAttathements(entity, updateEntity);
        saveFile(dto, updateEntity);
        trainingRepository.save(updateEntity);
    }

    private void setOldAttathements(TrainingEntity entity, TrainingEntity updateEntity) {
        if (entity.getFiliais() != null && updateEntity.getFiliais() != null) {
            List<FiliaisHCMResponse> mergedFiliais = new ArrayList<>();

            for (int i = 0; i < updateEntity.getFiliais().size(); i++) {
                FiliaisHCMResponse updatedFilial = updateEntity.getFiliais().get(i);

                if (i < entity.getFiliais().size()) {
                    FiliaisHCMResponse existingFilial = entity.getFiliais().get(i);
                    FiliaisHCMResponse mergedFilial = trainingMapper.createResponse(updatedFilial, existingFilial.attachment());

                    mergedFiliais.add(mergedFilial);
                } else {
                    mergedFiliais.add(updatedFilial);
                }
            }

            updateEntity.setFiliais(mergedFiliais);
        }
    }
}
